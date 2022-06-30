# 简介

## 项目架构
该项目模仿airportal，进行分享内容的空投服务，项目采用前后端分离的架构设计；前端使用vue2+后端使用java语言进行开发, java框架使用springboot；

前端地址：[进入](https://github.com/WinterChenS/airportal-frontend)

- java: jdk1.8
- springboot: 2.6.6
- mongodb: 5.0
- minio: RELEASE.2022-06-25T15-50-16Z

## Quick Start


### 源代码部署（不推荐）
1.下载源码
```
git clone https://github.com/WinterChenS/airportal-frontend
git clone https://github.com/WinterChenS/airportal
```
2.后端部署
```
cd airportal 
mvn clean package
cd target
java -jar airportal-0.0.1-SNAPSHOT.jar
```
3.前端部署
```
cd airportal-frontend
npm install
npm run build  #编译
npm run serve  #启动本地调试
```
注意：需要修改源码：src/api/request.js
```
 axios.defaults.baseURL = "/back"; //将这里改成后端地址
      //  axios.defaults.baseURL = "http://127.0.0.1:8080"; 
```

### docker-compose部署(推荐)

**1.nginx 配置**

```
## minio分片上传的nginx配置，需要ssl
server {
    listen 443 ssl;
    listen [::]:443 ssl;
    server_name winterchen.com;

    ssl_certificate ./cert/cert.pem;
    ssl_certificate_key ./cert/cert.key;

    ssl_session_cache shared:SSL:1m;
    ssl_session_timeout 5m;

    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    #root /usr/share/nginx/html/dist;
    sendfile on;
    send_timeout 2m;
    keepalive_timeout 65s;
    client_max_body_size 30m;
    client_body_timeout 36s;

    #minio服务反向代理配置
    location ^~ /share/ {
      rewrite ^/minio/(.*)$ /$1 break;
      proxy_pass http://121.41.196.178:39000;
    }

  }
  
 ## 前后端nginx配置
  server {
    listen 3001 ssl;
    listen [::]:3001 ssl;
    server_name winterchen.com;

    ssl_certificate ./cert/cert.pem;
    ssl_certificate_key ./cert/cert.key;

    ssl_session_timeout  60m; # session有效时间10分钟
    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE; # 按照这个套件配置
    ssl_prefer_server_ciphers on;

    sendfile on;
    send_timeout 2m;
    keepalive_timeout 65s;
    client_max_body_size 30m;
    client_body_timeout 36s;

    root /usr/share/nginx/html/dist;

    proxy_buffering off;

    ignore_invalid_headers off;

    # 前端配置
    location  / {
        try_files $uri $uri/ /index.html;
        index index.html index.htm;

    }
    

    # 后端配置
    location  /back/ {
        proxy_pass https://winterchen.com:8081/;
        proxy_buffer_size     128K;
        proxy_buffers         4 256K;
        proxy_busy_buffers_size  256K;
    }


  }
```

**2.minio部署**
docker-compose.yml
```yaml
version: '2'
services:
  minio:
    image: minio/minio
    #hostname: "minio"
    ports:
      - 39000:9000 # api 端口
      - 39001:9001 # 控制台端口
    environment:
      MINIO_ROOT_USER: minioadmin    #管理后台用户名
      MINIO_ROOT_PASSWORD: minioadmin #管理后台密码，最小8个字符
      MINIO_SERVER_URL: https://winterchen.com:39000 #这里是设置访问的域名地址
    volumes:
      - ./data:/data               #映射当前目录下的data目录至容器内/data目录
      - /docker/minio/config:/root/.minio/     #映射配置目录
    command: server --console-address ":9001" /data #--address "winterchen.com:39000" --console-address ":39001" /data  #指定容器中的目录 /data
    #privileged: true
    restart: always
    #network_mode: host
```
命令：
```
docker-compose up -d
```

**3.mongodb部署**

```yaml
version: '3.9'

services:
  mongodb:
    image: mongo:5.0
    ports:
      - 27017:27017
    volumes:
      - ./mongo:/data/db
    environment:
      - MONGO_INITDB_ROOT_USERNAME=admin
      - MONGO_INITDB_ROOT_PASSWORD=123456

```
命令：
```
docker-compose up -d
```

**4.airportal部署**

新建文件:
/home/winterchen/conf/airportal.yml
```yaml
spring:
  data:
    mongodb:
      database: airportal
      host: 127.0.0.1
      port: 27017
      username: admin
      password: 123456
      authentication-database: admin
  mail:
    host: <stmp服务地址>
    username: <邮箱地址>
    password: <密码>
    default-encoding: UTF-8

minio:
  endpoint: http://192.168.150.123:9999 #minio的地址
  accessKey: minioadmin
  secretKey: minioadmin
  bucketName: normalizedvideo
  downloadUri: http://localhost:8889/?takeCode=      #前端下载的地址，需要修改
  path: http://192.168.150.123:9999

swagger:
  enable: false

airportal:
  register:
    enable: false

```

```yaml
version: '3'

services:
  delay-server:
    container_name: airportal
    image: winterchen/airportal
    ports:
      - "8081:8081"
    volumes:
      - "./tmp:/tmp"
      - ./conf:/home/winterchen/conf
    environment:
      SERVER_PORT: 8081
```
命令：
```
docker-compose up -d
```

### docker部署
根据docker-compose，相信聪明的你应该会部署

### 报错解决方案
1. `PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException`

解决方案: [点击进入](https://github.com/WinterChenS/airportal-frontend/wiki/PKIX-path-building-failed:-sun.security.provider.certpath.SunCertPathBuilderException)

