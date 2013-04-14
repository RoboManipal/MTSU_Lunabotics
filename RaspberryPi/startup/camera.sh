#! /bin/sh
# /etc/init.d/robot.sh

## BEGIN INIT INFO
# Provides:         Camera-Server
# Required-Start:   $remote_fs $syslog 
# Required-Stop:    $remote_fs $syslog
# Default-Start:    2 3 4 5
# Default-Stop;     0 1 6
## END INIT INFO

case "$1" in
  start)
    echo "Starting Camera Web Server on port 8080"
    mjpg_streamer -i "/usr/lib/input_uvc.so -d /dev/video0 -f 15" -o "/usr/lib/output_http.so -p 8080 -w /usr/www"
    ;;
  stop)
    echo "Stopping Camera Web Server"
    killall mjpg_streamer
    ;;
  *)
    exit 1
    ;;
esac

exit 0