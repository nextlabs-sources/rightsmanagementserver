package com.test.nextlabs.rms.testAutomation;

import com.nextlabs.rms.rmc.AddRepositoryRequestDocument;
import com.nextlabs.rms.rmc.AddRepositoryResponseDocument;
import com.nextlabs.rms.rmc.types.StatusType;
import org.apache.xmlbeans.XmlException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddRepoTester implements TestInterface {
    private TestResult testResult = TestResult.NOT_EXECUTED;
    private String report = "";
    private static final int SUCCESS = 0;
    private static final int FAILURE = -1;

    private long sendAddRepoRequest(String repoName, String userId) {
        System.out.println("Sending add repo request for file " + repoName);
        try {
            String originalPath = IntegrationTester.inputDir + File.separator + TestingConfigManager.ADD_REPO_FOLDER_NAME + File.separator;
            String inputFile = originalPath + IntegrationTesterFiles.OPERATION_REPO_ADD + repoName + IntegrationTesterFiles.INPUT + ".xml";
            AddRepositoryRequestDocument doc = AddRepositoryRequestDocument.Factory.parse(new File(inputFile));
            doc.getAddRepositoryRequest().setUserId(userId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            Map<String,String> headerMap = new HashMap<>();
            headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME, TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
            String responseString = HttpRequestUtil.sendPostRequest(baos.toByteArray(), "AddRepoService", headerMap);
            AddRepositoryResponseDocument response = AddRepositoryResponseDocument.Factory.parse(responseString);
            StatusType status = response.getAddRepositoryResponse().getStatus();
            if (status.getCode() == 0) {
                testResult = TestResult.PASS;

                return response.getAddRepositoryResponse().getRepoId();
            }
            else {
                return -1L;
            }
        } catch (Exception e) {
            System.out.println("AddRepo failed.");
            e.printStackTrace();
            testResult = TestResult.FAIL;
        }

        return -1L;
    }

    public int sendAddRepoRequests(Map<String, Long> repoMap, String userOneId, String userTwoId) throws XmlException, IOException {
        // add three valid repositories
        long result = addRepo(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, userOneId);
            if (result == -1L) {
            report += "Error: Add repo test failed for user one add Google Drive\n";
            return FAILURE;
        } else {
            repoMap.put(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, result);
        }
        result = addRepo(IntegrationTesterFiles.REPO_DROPBOX_USER_ONE_VALID, userOneId);
        if (result == -1L) {
            report += "Error: Add repo test failed for user one add Dropbox\n";
            return FAILURE;
        } else {
            repoMap.put(IntegrationTesterFiles.REPO_DROPBOX_USER_ONE_VALID, result);
        }
        result = addRepo(IntegrationTesterFiles.REPO_ONE_DRIVE_USER_TWO_VALID, userTwoId);
        if (result == -1L) {
            report += "Error: Add repo test failed for user two add OneDrive\n";
            return FAILURE;
        } else {
            repoMap.put(IntegrationTesterFiles.REPO_ONE_DRIVE_USER_TWO_VALID, result);
        }

        // add three invalid repositories
        if (addRepo(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_INVALID_USER_ID, userOneId) != -1L) {
            report += "Error: Add repo test passed with invalid user id\n";
            return FAILURE;
        }
        if (addRepo(IntegrationTesterFiles.REPO_DROPBOX_USER_ONE_INVALID_REPO_TYPE, userOneId) != -1L) {
            report += "Error: Add repo test passed with invalid repo type\n";
            return FAILURE;
        }
        if (addRepo(IntegrationTesterFiles.REPO_ONE_DRIVE_USER_TWO_INVALID_USER_ID, userTwoId) != -1L) {
            report += "Error: Add repo test passed with invalid user id\n";
            return FAILURE;
        }

        return SUCCESS;
    }

    private long addRepo(String repoName, String userId) throws XmlException, IOException {
        long addRepoResult = sendAddRepoRequest(repoName, userId);

        return addRepoResult;
    }

    @Override
    public TestResult getResult() {
        return testResult;
    }

    @Override
    public String getReport() {
        return report;
    }

    @Override
    public void setResult(TestResult result) {
        this.testResult = result;
    }

    @Override
    public void setReport(String report) {
        this.report = report;
    }
}