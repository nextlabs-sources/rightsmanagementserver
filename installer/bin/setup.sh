#!/bin/bash

#echo "																																					";
#echo "   ____  ___ ____ _   _ _____ ____     __  __    _    _   _    _    ____ _____ __  __ _____ _   _ _____    ____  _____ ______     _______ ____  	";
#echo "  |  _ \|_ _/ ___| | | |_   _/ ___|   |  \/  |  / \  | \ | |  / \  / ___| ____|  \/  | ____| \ | |_   _|  / ___|| ____|  _ \ \   / | ____|  _ \ 	";
#echo "  | |_) || | |  _| |_| | | | \___ \   | |\/| | / _ \ |  \| | / _ \| |  _|  _| | |\/| |  _| |  \| | | |    \___ \|  _| | |_) \ \ / /|  _| | |_) |	";
#echo "  |  _ < | | |_| |  _  | | |  ___) |  | |  | |/ ___ \| |\  |/ ___ | |_| | |___| |  | | |___| |\  | | |     ___) | |___|  _ < \ V / | |___|  _ < 	";
#echo "  |_| \_|___\____|_| |_| |_| |____/   |_|  |_/_/   \_|_| \_/_/   \_\____|_____|_|  |_|_____|_| \_| |_|    |____/|_____|_| \_\ \_/  |_____|_| \_\	";
#echo "                                                                                                                                                  ";                                                                               	     

[ "$(whoami)" != "root" ] && exec sudo -- "$0" "$@"
if [ "$2" == -g ]; then
	login_shell=$(shopt | grep login | cut -f2)
	if [ "$login_shell" = "on" ] || [ -n "$SSH_CLIENT" ] || [ -n "$SSH_TTY" ]; then
		echo "No display is supported. Please run ./$1.sh -s" 
		exit
	fi
fi

#Ignore SIGINT
# This is needed, because to stop the installation half way, we send
# SIGINT to the process group which includes this script process
trap '' INT

export START_DIR=$( dirname "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )" )
export RMS_CUR_TIME=$(date +%d%m%Y"_"%H%M%S)
export LOG_DIR=RMS_Installer_${RMS_CUR_TIME}.log
export UI_LOG_DIR=RMS_Installer_UI_${RMS_CUR_TIME}.log

echo

echo "Extracting the $1er engine. This may take a few minutes."
echo
unzip -o -q ${START_DIR}/engine/engine_linux.zip -d /opt
unzip -o -q ${START_DIR}/engine/gems_linux.zip -d /tmp
cp -rf /tmp/gems/gems/* /opt/chef/embedded/lib/ruby/gems/2.1.0/gems/
cp -rf /tmp/gems/specifications/* /opt/chef/embedded/lib/ruby/gems/2.1.0/specifications/
cp -rf /tmp/gems/extensions/x86_64-linux/2.1.0/* /opt/chef/embedded/lib/ruby/gems/2.1.0/extensions/x86_64-linux/2.1.0/
echo "Finished extracting the $1er engine."
echo

if [ "$2" == -g ]
 then
	echo "Starting GUI $1er ..."
	/opt/chef/embedded/bin/ruby ${START_DIR}/bin/ui/app.rb $1 > /tmp/${UI_LOG_DIR} 2>&1
 else
 	echo "Starting the $1er ..."
	/opt/chef/bin/chef-client --format null --config ${START_DIR}/bin/client.rb -o RMS::$1
fi

echo

echo "Removing temporary files ..."
if [ -d /opt/chef ]; then
	rm -rf /opt/chef
fi
if [ -d /tmp/gems ]; then
	rm -rf /tmp/gems
fi
if [ -d /tmp/local-mode-cache ]; then
	rm -rf /tmp/local-mode-cache
fi
if [ -f /tmp/$LOG_DIR ]; then
	echo "Setup completed. See the $1ation log at /tmp/$LOG_DIR for the details."
fi