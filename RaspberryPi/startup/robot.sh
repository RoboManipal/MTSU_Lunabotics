#! /bin/sh
# /etc/init.d/robot.sh

## BEGIN INIT INFO
# Provides:         Robot-Server
# Required-Start:   $remote_fs $syslog 
# Required-Stop:    $remote_fs $syslog
# Default-Start:    2 3 4 5
# Default-Stop;     0 1 6
## END INIT INFO

case "$1" in
  start)
    echo "Starting Robot Server"
    /usr/local/bin/node /usr/local/URECA/robot.js
    ;;
  stop)
    echo "Stopping Robot Server"
    killall Robot-Server
    ;;
  *)
    exit 1
    ;;
esac

exit 0