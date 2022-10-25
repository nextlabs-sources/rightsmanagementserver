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
rm -rf !(RMSConfigTest.properties|testAutomationClient.sh|testAutomationServer.sh|testAutomation.sh)
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
echo "Url for RMS Test zip file is: $rmsTestZipUrl"
echo 'Downloading the RMS test automation zip file to $RMS_TEST_DIR'
aria2c --http-user $username --http-passwd $password $rmsTestZipUrl
echo 'RMS test automation zip download completed'

#unzip the files downloaded from hudson
unzip -o -d RMSTestAutomation RMSTestAutomation*.zip

#call the test script downloaded from hudson
#start to execute test automation
cd $RMS_TEST_DIR/RMSTestAutomation/QA
if [ "$OSTYPE" = "cygwin" ] 
then 
#run test cases
java -cp "lib/jcommander-1.48.jar;lib/RMS_QA_Test.jar;lib/testng.jar;lib/selenium-java-2.49.0.jar;lib/zip4j_1.3.2.jar;lib/dom4j-1.6.1.jar;lib/log4j-1.2.17.jar;lib/selenium-java-2.49.0-srcs.jar;lib/selenium-server-standalone-2.49.0.jar;lib/guice-3.0.jar;lib/velocity-dep-1.4.jar;lib/reportng-1.1.4.jar;lib/AutoItX4Java.jar;lib/jacob.jar;lib/javax.mail.jar" org.testng.TestNG testcases/All.xml
else
java -cp "lib/jcommander-1.48.jar;lib/RMS_QA_Test.jar:lib/testng.jar:lib/selenium-java-2.49.0.jar:lib/zip4j_1.3.2.jar:lib/dom4j-1.6.1.jar:lib/log4j-1.2.17.jar:lib/selenium-java-2.49.0-srcs.jar:lib/selenium-server-standalone-2.49.0.jar:lib/guice-3.0.jar:lib/velocity-dep-1.4.jar:lib/reportng-1.1.4.jar:lib/AutoItX4Java.jar:lib/jacob.jar:lib/javax.mail.jar" org.testng.TestNG testcases/All.xml
fi
sleep 10
#send email with report
java -cp "lib/jcommander-1.48.jar;lib/RMS_QA_Test.jar;lib/zip4j_1.3.2.jar;lib/dom4j-1.6.1.jar;lib/log4j-1.2.17.jar;lib/javax.mail.jar" com.test.nextlabs.rms.qa.testAutomation.main.Main