#!/bin/bash -xv
# RMS test automation script
cd $RMS_TEST_DIR/RightsMgmtSrv
chmod -R 755 .
#Replace setup.json
cp $RMS_TEST_DIR/setup.json setup.json
if [ "$OSTYPE" = 'cygwin' ] 
then 
./install.bat -s
else
sh install.sh -s
fi
sleep 60

