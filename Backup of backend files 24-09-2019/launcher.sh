#!/bin/bash
# /etc/init.d/launcher.sh
### BEGIN INIT INFO
# Provides:          launcher.sh
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Start daemon at boot time
# Description:       Enable service provided by daemon.
### END INIT INFO# /etc/init.d/sample.py
sudo java -jar /home/pi/RGBProject/RGBTapeLightingProject.jar > /home/pi/RGBProject/java-log.txt &
sudo /home/pi/RGBProject/main.py > /home/pi/RGBProject/python-log.txt &
