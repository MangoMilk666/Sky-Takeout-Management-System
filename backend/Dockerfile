FROM eclipse-temurin:17-jre

WORKDIR /app

COPY app.jar /app/app.jar

EXPOSE 8080

# JVM 调优参数说明：
#   -Xms / -Xmx        初始/最大堆内存，避免运行时频繁扩容触发 Full GC
#   -XX:MaxRAMPercentage  容器内存感知：使用容器限制内存的 75% 作为堆（需配合 docker-compose 的 mem_limit）
#   -XX:+UseContainerSupport  启用容器内存感知（JDK 17 默认开启，显式声明更清晰）
#   -XX:+HeapDumpOnOutOfMemoryError  OOM 时自动 dump 堆快照，方便排查内存泄漏
#   -XX:HeapDumpPath   dump 文件输出路径
ENTRYPOINT ["java", \
  "-Xms512m", \
  "-Xmx1024m", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseContainerSupport", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:HeapDumpPath=/app/heapdump.hprof", \
  "-jar", "/app/app.jar"]
