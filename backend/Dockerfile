FROM openjdk:17-jdk-alpine
WORKDIR /app
# 修改这一行，直接拷贝 改名后的 app.jar
COPY app.jar app.jar
#COPY sky-server-1.0-SNAPSHOT.jar app.jar
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]