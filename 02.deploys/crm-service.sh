#!/bin/sh
echo "Service [$1]!!!"
# đường dẫn đến service
export APP_HOME=/u01/app/backend/crm-service
export APP_NAME=crm-service-1.0-SNAPSHOT
export HCM_CRM_PORT=8888
export PID_PATH_NAME=/u01/app/backend/crm-service/api.pid
export LOG_HOME=/u01/app/logs/crm-service
export HCM_CRM_BOOK_TEMPLATE=/u01/app/files/crm/template/
export HCM_CRM_BOOK_EXPORT_FOLDER=/u01/app/files/crm/export/

# set các biến môi trường cho ứng dụng
# thông tin kết nối được mã hóa jasypt
export HCM_CRM_JDBC_URL=3w9fgVJpuTsZU8NLadvEt0zo7+VLC4ErJj+iauzhkc4akpVJtbDFc1RKJI4DBhhGv2NSwUDFN98=
export HCM_CRM_JDBC_USER_NAME=SgfCBtN/LmdkI1FzzF8RxLMmXP/ICxwI
export HCM_CRM_JDBC_PASSWORD=9ylZDaWwyXZb/c2FE6dZEXBHYvrTw/08v0+DSc1lHppxXzjqlYT6xal9mDlJzImy

# thông tin redis được mã hóa jasypt
export HCM_REDIS_URL=l1vCVZVHsQwO9aYw9IvZo3UwRB7mGAJj
export HCM_REDIS_PORT=+s0tUjew/K5LVDu3HbMWpw==

# thông tin đường dẫn ứng dụng liên quan
export HCM_FILE_STORAGE_SERVICE_URL=http://localhost:8861/file-storage-service/
export HCM_ADMIN_SERVICE_URL=http://localhost:8868/admin-service

# cấu hình đường dẫn thư mục export
export HCM_EXPORT_FOLDER=/u01/app/files/export/
export HCM_FONT_FOLDER=/u01/app/fonts/

# đường dẫn jdk

echo "Service [$APP_NAME] - [$1]"
echo "    JAVA_HOME=$JAVA_HOME"
echo "    APP_HOME=$APP_HOME"
echo "    APP_NAME=$APP_NAME"
echo "    APP_PORT=$HCM_CRM_PORT"
echo "    PID_PATH_NAME=$PID_PATH_NAME"
SERVICE_NAME=API
case $1 in
   start)
       echo "Starting $SERVICE_NAME ..."
       if [ ! -f $PID_PATH_NAME ]; then
		   nohup $JAVA_HOME/bin/java -Dspring.profiles.active=prod -jar $APP_HOME/$APP_NAME.jar < /dev/null > $LOG_HOME/app.log 2>&1 &
           echo $! > $PID_PATH_NAME
           echo "$SERVICE_NAME started ..."
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
           rm $PID_PATH_NAME
           echo "$SERVICE_NAME starting ..."
		   nohup $JAVA_HOME/bin/java -Dspring.profiles.active=prod -jar $APP_HOME/$APP_NAME.jar < /dev/null > $LOG_HOME/app.log 2>&1 &
           echo $! > $PID_PATH_NAME
           echo "$SERVICE_NAME started ..."
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
