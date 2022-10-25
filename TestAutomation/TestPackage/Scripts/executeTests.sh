#!/bin/bash -xv
# RMS test automation script
echo "CATALINA_HOME is $CATALINA_HOME"
cd $CATALINA_HOME
echo "Changed directory to $(pwd)"
cd bin
echo 'Shutting down tomcat'
sh shutdown.sh
if [ "$OSTYPE" = 'cygwin' ] 
then 
TASKKILL /F /IM java.exe
else
sudo pkill -f java 
fi
echo 'Deleting war file'
rm -rf ../webapps/RMS.war ../webapps/RMS
cp $RMS_TEST_DIR/RMS/RMS.war ../webapps
sh startup.sh
sleep 60
#Run the main program here and redirect the output to a text file
#Evaluate the test result and send out emails
cd $RMS_TEST_DIR/RMSTestAutomation
if [ "$OSTYPE" = "cygwin" ] 
then 
java -cp "RMSTest.jar;xbean.jar;xmlpublic.jar;RMS_xmlbeans.jar;commons-io-2.4.jar;javax.mail.jar" com.test.nextlabs.rms.testAutomation.TestRunner
else
java -cp "RMSTest.jar:xbean.jar:xmlpublic.jar:RMS_xmlbeans.jar:commons-io-2.4.jar:javax.mail.jar" com.test.nextlabs.rms.testAutomation.TestRunner
fi
