@ECHO OFF

"..\..\..\external\jre\bin\java" -cp "KMS.jar;RMSUtil.jar;KMS_xmlbeans.jar;commons-io-2.4.jar;commons-codec-1.10.jar;slf4j-api-1.7.13.jar;slf4j-log4j12-1.7.13.jar;log4j-1.2.17.jar;org.restlet-2.3.5.jar;org.restlet.ext.jaxb-2.3.5.jar" com.nextlabs.kms.importer.DefaultKeyImportUtil %*
