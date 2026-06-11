FROM eclipse-temurin:17-jre

WORKDIR /app

COPY app.jar /app/app.jar

EXPOSE 8080

# JVM 调优参数说明（面向 2 核 2G、且 MySQL 仍与应用同机部署的保守配置）：
#   -Xms / -Xmx        将 Java 堆控制在 256m ~ 512m，避免单 JVM 抢占过多内存把整机拖入 OOM
#   -XX:MaxRAMPercentage  作为容器感知兜底，配合 docker-compose 里的 mem_limit 使用
#   -XX:+UseContainerSupport  启用容器内存感知（JDK 17 默认开启，显式声明更清晰）
#   -XX:+HeapDumpOnOutOfMemoryError  OOM 时自动 dump 堆快照，方便后续排查
#   -XX:HeapDumpPath   dump 文件输出路径
ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx512m", \
  "-XX:MaxRAMPercentage=60.0", \
  "-XX:+UseContainerSupport", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:HeapDumpPath=/app/heapdump.hprof", \
  "-jar", "/app/app.jar"]
