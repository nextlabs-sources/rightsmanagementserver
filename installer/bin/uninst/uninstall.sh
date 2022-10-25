#!/bin/bash
export START_DIR=$(pwd)
echo
echo "Preparing the uninstaller. This may take a few minutes ..."
cp -rf "${START_DIR}/.uninst/" "/tmp/RMS_TMP/"

cd /tmp/RMS_TMP/
./uninstall.sh $1