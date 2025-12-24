# 使用官方 Ubuntu 22.04 作为基础
FROM ubuntu:22.04

# 设置工作目录（可选，但推荐）
WORKDIR /app

# 更新 apt 仓库并安装 OpenJDK 11
RUN apt-get update && \
    apt-get install -y --no-install-recommends openjdk-11-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*


# (可选) 验证 Java 安装 (可以选择添加你的应用程序，并编译运行，或者只是执行 java -version)
RUN java -version

# 设置默认的模型路径环境变量
ENV SMART_MODEL_PATH=/app/models

# 创建该目录
RUN mkdir -p /app/models

# 挂载
VOLUME ["/app/models"]

# UTF-8
ENV LANG=zh_CN.UTF-8
ENV LC_ALL=zh_CN.UTF-8


# 声明服务运行在8080端口
EXPOSE 8080

# 指定docker容器启动时运行jar包
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
