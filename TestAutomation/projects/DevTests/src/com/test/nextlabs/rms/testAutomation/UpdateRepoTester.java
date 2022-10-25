package com.test.nextlabs.rms.testAutomation;

import com.nextlabs.rms.rmc.UpdateRepositoryRequestDocument;
import com.nextlabs.rms.rmc.UpdateRepositoryResponseDocument;
import com.nextlabs.rms.rmc.types.StatusType;
import org.apache.xmlbeans.XmlException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UpdateRepoTester implements TestInterface {
    private TestResult testResult = TestResult.NOT_EXECUTED;
    private String report = "";
    private static final int SUCCESS = 0;
    private static final int FAILURE = -1;

    private boolean sendUpdateRepoRequest(String repoName, String userId, long repoId)  {
        try {
            String originalPath = IntegrationTester.inputDir + File.separator + TestingConfigManager.UPDATE_REPO_FOLDER_NAME + File.separator;
            String inputFile = originalPath + IntegrationTesterFiles.OPERATION_REPO_UPDATE + repoName + IntegrationTesterFiles.INPUT + ".xml";
            UpdateRepositoryRequestDocument doc = UpdateRepositoryRequestDocument.Factory.parse(new File(inputFile));
            doc.getUpdateRepositoryRequest().setUserId(userId);
            doc.getUpdateRepositoryRequest().getRepository().setRepoId(repoId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            Map<String,String> headerMap = new HashMap<>();
            headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME, TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
            String responseString = HttpRequestUtil.sendPostRequest(baos.toByteArray(), "UpdateRepoService", headerMap);
            UpdateRepositoryResponseDocument response = UpdateRepositoryResponseDocument.Factory.parse(responseString);
            StatusType status = response.getUpdateRepositoryResponse().getStatus();
            if (status.getCode() == 0) {
                testResult = TestResult.PASS;

                return true;
            }
            else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("UpdateRepo failed.");
            e.printStackTrace();
            testResult = TestResult.FAIL;
        }

        return false;
    }

    public int sendUpdateRepoRequests(Map<String, Long> repoMap, String userOneId, String userTwoId) throws XmlException, IOException {
        // three valid updates
        long repoId = repoMap.get(IntegrationTesterFiles.REPO_DROPBOX_USER_ONE_VALID);
        if (!updateRepo(IntegrationTesterFiles.REPO_DROPBOX_USER_ONE_VALID, userOneId, repoId)) {
            report += "Error: Update repo test failed for user one update Dropbox\n";
            return FAILURE;
        }
        repoId = repoMap.get(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID);
        if (!updateRepo(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, userOneId, repoId)) {
            report += "Error: Update repo test failed for user one update Google Drive\n";
            return FAILURE;
        }
        repoId = repoMap.get(IntegrationTesterFiles.REPO_ONE_DRIVE_USER_TWO_VALID);
        if (!updateRepo(IntegrationTesterFiles.REPO_ONE_DRIVE_USER_TWO_VALID, userTwoId, repoId)) {
            report += "Error: Update repo test failed for user two update OneDrive\n";
            return FAILURE;
        }

        // update with invalid repoId
        repoId = getRepoIdInvalid(repoMap);

        if (updateRepo(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, userOneId, repoId)) {
            report += "Error: Update repo test passed with invalid repo id\n";
            return FAILURE;
        }

        return SUCCESS;
    }

    private int getRepoIdInvalid(Map<String, Long> repoMap) {
        int repoId;
        Random r = new Random();
        do {
            repoId = r.nextInt();
        } while (repoMap.containsKey(repoId));

        return repoId;
    }

    private boolean updateRepo(String repoName, String userId, long repoId) throws XmlException, IOException {
        boolean updateRepoResult = sendUpdateRepoRequest(repoName, userId, repoId);

        return updateRepoResult;
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