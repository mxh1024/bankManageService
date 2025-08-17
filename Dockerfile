# 第一阶段：构建Spring Boot应用
FROM maven:3.8.5-openjdk-17 AS build

# 设置工作目录
WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 构建应用，跳过测试以加快构建速度
RUN mvn clean package -DskipTests

# 第二阶段：运行环境
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制打包好的jar文件
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# 创建启动脚本，同时启动Redis和Spring Boot应用
RUN echo '#!/bin/bash\n\
java ${JAVA_OPTS} -jar app.jar' > /app/start.sh && \
chmod +x /app/start.sh

# 启动应用
CMD ["/app/start.sh"]
