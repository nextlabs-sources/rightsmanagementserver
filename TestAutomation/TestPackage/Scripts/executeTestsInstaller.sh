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
#Run the main program here and redirect the output to a text file
#Evaluate the test result and send out emails
cd $RMS_TEST_DIR/RMSTestAutomation
if [ "$OSTYPE" = "cygwin" ] 
then 
java -cp "RMSTest.jar;xbean.jar;xmlpublic.jar;RMS_xmlbeans.jar;commons-io-2.4.jar;javax.mail.jar" com.test.nextlabs.rms.testAutomation.TestRunner
else
java -cp "RMSTest.jar:xbean.jar:xmlpublic.jar:RMS_xmlbeans.jar:commons-io-2.4.jar:javax.mail.jar" com.test.nextlabs.rms.testAutomation.TestRunner
fi
