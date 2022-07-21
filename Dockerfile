FROM openjdk:8-jdk-alpine
MAINTAINER liuzhi

#部署服务器目录地址
ARG workdir=/data/szhz-gateway/szhz-gateway-route
#springboot使用的配置文件
ARG profile=test
#springboot启动的端口
ARG port=8082


#设置编码
ENV LANG en_US.utf8
#定义时区参数
ENV TZ=Asia/Shanghai
#设置springboot启动配置文件
ENV SPRING_PROFILES_ACTIVE ${profile}
ENV SERVER.PORT=${port}

#目录准备
RUN mkdir -p ${workdir}

VOLUME ${workdir}
WORKDIR ${workdir}
EXPOSE $PORT
#dockerfile和jar是在同级目录，故此出不使用绝对路劲
ADD szhz-gateway-route-web-*.jar ${workdir}/boot.jar

ENTRYPOINT ["java","-Xms256m","-Xmx256m","-Djava.security.egd=file:/dev/./urandom","-jar" ,"boot.jar"]