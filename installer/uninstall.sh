#!/bin/bash
if [ -z "$1" ] || [ "$1" == -g ]; then
    ./bin/setup.sh uninstall -g &
elif [ "$1" == -s ]; then
    ./bin/setup.sh uninstall
else
	if [ "$1" != -h ]; then echo "Invalid argument." 
	fi
	echo
	echo "Description: Uninstalls Rights Management Server"
	echo "Usage: ./uninstall.sh [flag]"
	echo
	echo "-s	Run the uninstaller silently"
	echo "-g	Run the uninstaller with a GUI (default)"
	echo "-h	Display help"
fi