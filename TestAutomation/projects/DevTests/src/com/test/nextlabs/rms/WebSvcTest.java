package com.test.nextlabs.rms;

import noNamespace.LogServiceDocument;
import noNamespace.ResponseEnum;
import org.apache.commons.io.IOUtils;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
/**
 * Created by IntelliJ IDEA.
 * User: tbiegeleisen
 * Date: 7/22/15
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class WebSvcTest {
    private String inputDir = "C:\\p4\\skaranam_AMAMI\\Destiny\\D_Jasper\\release\\RMS_8.0.2\\src\\server\\apps\\odrmConsole\\src\\ODRM\\src\\com\\test\\nextlabs\\rms\\resources\\loggingInputs";
    
    public int sendLogSuccessTest(String inputFile)  {
        System.out.println("Sending log request for file "+ inputFile);
    	int errorCode = -1;
    	try {
            byte[] encoded = Files.readAllBytes(Paths.get(inputDir + "\\"+ inputFile));
            LogServiceDocument response = sendLog(encoded);
            errorCode = response.getLogService().getLogResponse().getFault().getErrorCode();
            ResponseEnum.Enum responseEnum = response.getLogService().getLogResponse().getResponse();
            if (errorCode == 0 && responseEnum.equals(ResponseEnum.SUCCESS)) {
                System.out.println("LogSuccessTest was successful");
            }
            else {
                System.out.println("LogSuccessTest was unsuccessful, error code: " + errorCode);
            }
        } catch (Exception e) {
            System.out.println("LogSuccessTest() failed.");
            e.printStackTrace();
            return errorCode;
        }
        return errorCode;
    }

    public LogServiceDocument sendLog(byte[] logRequest) throws Exception {
        String uri = String.format("http://localhost:8080/RMS/service/SendLog");
        URL url = new URL(uri);

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        connection.setRequestProperty("Content-Length", Integer.toString(logRequest.length));
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.write(logRequest);
        InputStream xml = connection.getInputStream();

        StringWriter writer = new StringWriter();
        IOUtils.copy(xml, writer);
        String logResponse = writer.toString();

        LogServiceDocument response = LogServiceDocument.Factory.parse(logResponse);

        return response;
    }
    
    @Test
    public void LogTestCases() {
      WebSvcTest tester = new WebSvcTest();
      // Pass cases
      assertEquals("Error code is", 0, tester.sendLogSuccessTest("LogTestRequest1.xml"));
      assertEquals("Error code is", 0, tester.sendLogSuccessTest("EvaluationLogWithAllFields.xml"));
      assertEquals("Error code is", 0, tester.sendLogSuccessTest("MultipleLogsWithUserAttributes.xml"));
      assertEquals("Error code is", 0, tester.sendLogSuccessTest("OperationLogsWithAllFields.xml"));
      assertEquals("Error code is", 0, tester.sendLogSuccessTest("OperationLogsWithNoRights.xml"));
      assertEquals("Error code is", 0, tester.sendLogSuccessTest("OperationLogsWithoutEnvironment.xml"));
      assertEquals("Error code is", 0, tester.sendLogSuccessTest("CorrectInput.xml"));
      assertEquals("Error code is", 0, tester.sendLogSuccessTest("OperationLogsWithoutApplication.xml"));
      //Fail cases
      assertEquals("Error code is", 5, tester.sendLogSuccessTest("EvaluationNoEnvironment.xml"));
      assertEquals("Error code is", 5, tester.sendLogSuccessTest("NoUserContext.xml"));
    }
    
}