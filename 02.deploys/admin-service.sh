#!/bin/sh
echo "Service [$1]!!!"
# đường dẫn đến service
export APP_HOME=C:/HRM/app/backend/admin
export APP_LOG_PATH=C:/HRM/logs/admin
export APP_NAME=admin-service-1.0-SNAPSHOT
export HCM_ADMIN_PORT=8868
export PID_PATH_NAME=C:/HRM/app/backend/admin/api.pid
# cấu hình đường dẫn thư mục export
export HCM_EXPORT_FOLDER=C:/HRM/app/file-exports/

# set các biến môi trường cho ứng dụng
# thông tin kết nối được mã hóa jasypt
export HCM_ADMIN_JDBC_URL=RUiV5kVvfxcyEvlE9dGBvO92JcOWojOCsA44xh1zza9713DhMkc+G5XLK51w5poq
export HCM_ADMIN_JDBC_USER_NAME=VmmKPYQ0UmnXh0oTQByb0A==
export HCM_ADMIN_JDBC_PASSWORD=4t4ncQZGNaBRzU6F+3rHlEzE308nTSm0

# thông tin redis được mã hóa jasypt
export HCM_REDIS_URL=i83ajZvu72/niOkJ80Qs5w==
export HCM_REDIS_PORT=rN83rhuJuaiMVZpkJ4f3Gg==
export HCM_REDIS_PASSWORD=L5xvoTLIVKWjrrGYmFzIiPfB2ruadE8n

# thông tin đường dẫn ứng dụng liên quan
export HCM_FILE_STORAGE_SERVICE_URL=http://localhost:8861/storage-service/
export HCM_ADMIN_SERVICE_URL=http://localhost:8868/admin-service
export HCM_HRM_SERVICE_URL=http://localhost:8869/admin-service
export ACS_AUTH_URL=default

# đường dẫn jdk
export JAVA_HOME="C:/HRM/setup/jdk-17.0.11/"
echo "Service [$APP_NAME] - [$1]"
echo "    JAVA_HOME=$JAVA_HOME"
echo "    APP_HOME=$APP_HOME"
echo "    APP_NAME=$APP_NAME"
echo "    APP_PORT=$HCM_ADMIN_PORT"
echo "    PID_PATH_NAME=$PID_PATH_NAME"
SERVICE_NAME=API
case $1 in
   start)
       echo "Starting $SERVICE_NAME ..."
       if [ ! -f $PID_PATH_NAME ]; then
		   nohup "$JAVA_HOME"/bin/java -Dspring.profiles.active=prod -jar $APP_HOME/$APP_NAME.jar < /dev/null > $APP_LOG_PATH/app.log 2>&1 &
           echo $! > $PID_PATH_NAME
           sleep 1
           echo "$SERVICE_NAME started ..."
           (sleep 1 && tail -300f $APP_LOG_PATH/app.log) &
       else
           echo "$SERVICE_NAME is already running ..."
       fi
   ;;
   stop)
       if [ -f $PID_PATH_NAME ]; then
           PID=$(cat $PID_PATH_NAME);
           echo "$SERVICE_NAME stoping ..."
           kill $PID;
           echo "$SERVICE_NAME stopped ..."
           mv $APP_LOG_PATH/app.log $APP_LOG_PATH/app."$(date +\%Y-\%m-\%d-\%H-\%M).log"
           rm $PID_PATH_NAME
       else
           echo "$SERVICE_NAME is not running ..."
       fi
   ;;
   restart)
       if [ -f $PID_PATH_NAME ]; then
           PID=$(cat $PID_PATH_NAME);
           echo "$SERVICE_NAME stopping ...";
           kill -9 $PID;
           echo "$SERVICE_NAME stopped ...";
           mv $APP_LOG_PATH/app.log $APP_LOG_PATH/app."$(date +\%Y-\%m-\%d-\%H-\%M).log"
           rm $PID_PATH_NAME
           echo "$SERVICE_NAME starting ..."
		   nohup "$JAVA_HOME"/bin/java -Dspring.profiles.active=prod -jar $APP_HOME/$APP_NAME.jar < /dev/null > $APP_LOG_PATH/app.log 2>&1 &
           echo $! > $PID_PATH_NAME
           sleep 1
           echo "$SERVICE_NAME started ..."
           (sleep 1 && tail -300f $APP_LOG_PATH/app.log) &
       else
           echo "$SERVICE_NAME is not running ..."
       fi
   ;;
   status)
		if [ -f "$PID_PATH_NAME" ]
		then
			PID=$(cat $PID_PATH_NAME)
			echo "$SERVICE_NAME is running, PID is $PID"
		else
			echo "$SERVICE_NAME is not running !"
		fi
   ;;
   *)
		echo "Usage: $0 {start|stop|restart|status}"
esac
