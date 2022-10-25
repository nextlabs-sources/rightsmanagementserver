#!/bin/bash
export RMS_GUI=true

while [[ $# > 0 ]]
do
key="$1"
case $key in
    -g)
	export RMS_GUI=true
    ;;
    -s)
    export RMS_GUI=false
    ;;
    *)
	if [ $key != -h ]; then echo Invalid Argument; fi;
	echo
	echo "Description: Installs Rights Management Server"
	echo "Usage: ./install.sh [flag]"
	echo
	echo "-s	Run the installer silently"
	echo "-g	Run the installer with a GUI (default)"
	echo "-h	Display help" 
	echo
    ;;
esac
shift
done
if [ ${RMS_GUI} == "true" ]; then
	./bin/setup.sh install -g &
else
	./bin/setup.sh install
fi
