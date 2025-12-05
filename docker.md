# 📦 KPI Service - Hướng dẫn cài đặt docker

Hướng dẫn này giải thích cách run và build `kpi-service (có thể thay bằng service khác)` trong microservice sử dụng
Docker.
Port `8879 (sử dụng port map với service)`.

---

## Cài đặt

- Installed:
    - [Docker Desktop](https://www.docker.com/products/docker-desktop)
- Docker phải chạy trước khi bắt đầu build
- Sử dụng `wsl --update` nếu được Docker Desktop nhắc

---

## Step 1: Build the JAR

- Vào project chạy lệnh:

```bash
mvn clean package
```

- Đảm bảo rằng có file [Dockerfile](kpi-service/Dockerfile) trong service muốn build:
- Nội dung file: 

```bash
FROM amazoncorretto:17-alpine '(có thể thay đổi theo SDK dự án sử dụng)'

WORKDIR /app

COPY target/kpi-service.jar app.jar '(có thể thay đổi theo service muốn build)'

EXPOSE 8879 '(sử dụng port map với service)'

ENTRYPOINT ["java", "-jar", "app.jar"]
```
## Step 2: Build the DOCKER
- Khởi động docker destop
- Vào command chạy lệnh:

```bash
cd ..\backend-service\kpi-service

docker build -t kpi-service-image .

docker run -p 8879:8879 kpi-service-image
```


## Step 3: Rebuild if code changes

```bash
mvn clean package

docker build -t kpi-service-image .
```