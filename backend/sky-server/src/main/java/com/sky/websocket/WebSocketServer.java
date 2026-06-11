package com.sky.websocket;

import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务类
 * 注解声明为 WebSocket 端点，路径中的 `{sid}` 是客户端连接时传入的唯一标识
 *
 * 【线程安全说明】
 * 本类的方法会被不同的线程并发调用：
 *   - onOpen / onClose：由 Tomcat 的 WebSocket 工作线程在连接建立/断开时回调
 *   - sendToAllClient：由 Spring 的 @Scheduled 定时任务线程（WebSocketTask）周期性调用
 * 因此 sessionMap 必须使用线程安全的容器，原实现使用 HashMap 存在以下风险：
 *   1. 并发 put/remove 可能破坏 HashMap 内部链表结构，极端情况下导致死循环
 *   2. sendToAllClient 遍历 values() 期间若有连接加入/断开，HashMap 迭代器会抛 ConcurrentModificationException
 * 改用 ConcurrentHashMap 后：
 *   - 读写操作通过分段锁（JDK8+ 为 CAS + synchronized）保证线程安全，无需全表加锁
 *   - 迭代器具有弱一致性（weakly consistent），遍历时允许并发修改，不会抛异常也不会死循环
 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    // 存放会话对象（使用 ConcurrentHashMap 保证多线程并发读写安全）
    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法（由 Tomcat WebSocket 线程回调）
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端：" + sid + "建立连接");
        sessionMap.put(sid, session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端：" + sid + "的信息:" + message);
    }

    /**
     * 连接关闭调用的方法（由 Tomcat WebSocket 线程回调）
     *
     * @param sid 客户端标识
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开:" + sid);
        sessionMap.remove(sid);
    }

    /**
     * 群发（广播）消息（由 @Scheduled 定时任务线程调用）
     * ConcurrentHashMap 的 values() 迭代器为弱一致性，
     * 遍历期间允许其他线程对 map 进行 put/remove，不会抛异常
     *
     * @param message 要广播的消息内容
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
