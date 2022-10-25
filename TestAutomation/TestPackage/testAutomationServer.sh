#!/bin/bash -xv
# RMS test automation script
#Check if the RMS_TEST_DIR exists
echo "RMS_TEST_DIR is $RMS_TEST_DIR"
if [ ! -d "$RMS_TEST_DIR" ]; then
  mkdir -p $RMS_TEST_DIR
  echo "Created directory $RMS_TEST_DIR"
fi
cd $RMS_TEST_DIR
#Set shopt option to enable ! option with rm command
shopt -s extglob
#rm -rf !(RMSConfigTest.properties|testAutomation.sh)
#This branch url has to be configured for each individual branch
rmsBranchUrl='http://nxt-build18-apache.nextlabs.com:9051/pcv/SecureCollaboration/8.3.0.0/'
#Name of the test automation file generated on hudson 
rmsTestAutomationFile='RMS_TEST_AUTOMATION.txt'
rmsTestAutomationFileUrl=$rmsBranchUrl$rmsTestAutomationFile
echo "rmsTestAutomationFileUrl is $rmsTestAutomationFileUrl"
#Username and password for the hudson server
username=raymond
password=Blue123!
#aria2c is a cygwin package that helps download files with concurrent threads
aria2c --http-user $username --http-passwd $password $rmsTestAutomationFileUrl
read -a d -d '\n' <RMS_TEST_AUTOMATION.txt
rmsTestZipUrl=$rmsBranchUrl${d[0]}
rmsLinZipUrl=$rmsBranchUrl${d[1]}
rmsWinZipUrl=$rmsBranchUrl${d[2]}
echo "Url for RMS Test zip file is: $rmsTestZipUrl"
echo "Url for RMS Windows Installer zip file is: $rmsWinZipUrl"
echo "Url for RMS Linux Installer zip file is: $rmsLinZipUrl"

#echo 'RMS zip download completed'
if [ "$OSTYPE" = 'cygwin' ] 
then
echo 'Downloading the RMS Windows Installer zip file to $RMS_TEST_DIR'
aria2c  --min-split-size 9M --max-connection-per-server 16 --http-user $username --http-passwd $password $rmsWinZipUrl
echo 'RMS Windows Installer zip download completed'
else
echo 'Downloading the RMS Linux Installer zip file to $RMS_TEST_DIR'
aria2c  --min-split-size 9M --max-connection-per-server 16 --http-user $username --http-passwd $password $rmsLinZipUrl
echo 'RMS Linux Installer zip download completed'
fi

#unzip the files downloaded from hudson
unzip -o -d RightsMgmtSrv RightsMgmtSrv*.zip
#call the test script downloaded from hudson
source RMSTestAutomation/QA/Scripts/executeTestsInstaller.sh