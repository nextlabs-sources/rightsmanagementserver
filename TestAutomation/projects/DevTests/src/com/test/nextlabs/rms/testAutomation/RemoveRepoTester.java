package com.test.nextlabs.rms.testAutomation;

import com.nextlabs.rms.rmc.RemoveRepositoryRequestDocument;
import com.nextlabs.rms.rmc.RemoveRepositoryResponseDocument;
import com.nextlabs.rms.rmc.types.StatusType;
import org.apache.xmlbeans.XmlException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RemoveRepoTester implements TestInterface {
    private TestResult testResult = TestResult.NOT_EXECUTED;
    private String report = "";
    private static final int SUCCESS = 0;
    private static final int FAILURE = -1;

    public boolean sendRemoveRepoRequest(long repoId, String userId)  {
        System.out.println("Sending remove repo request for repoId " + repoId);

        try {
            String originalPath = IntegrationTester.inputDir + File.separator + TestingConfigManager.REMOVE_REPO_FOLDER_NAME + File.separator;
            String inputFile = originalPath + IntegrationTesterFiles.OPERATION_REPO_REMOVE + IntegrationTesterFiles.REPO + IntegrationTesterFiles.INPUT + ".xml";
            RemoveRepositoryRequestDocument doc = RemoveRepositoryRequestDocument.Factory.parse(new File(inputFile));
            doc.getRemoveRepositoryRequest().setUserId(userId);
            doc.getRemoveRepositoryRequest().setRepoId(repoId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            Map<String,String> headerMap = new HashMap<>();
            headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME, TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
            String responseString = HttpRequestUtil.sendPostRequest(baos.toByteArray(), "RemoveRepoService", headerMap);
            RemoveRepositoryResponseDocument response = RemoveRepositoryResponseDocument.Factory.parse(responseString);
            StatusType status = response.getRemoveRepositoryResponse().getStatus();
            if (status.getCode() == 0) {
                testResult = TestResult.PASS;
                return true;
            }
            else {
                testResult = TestResult.FAIL;
                return false;
            }
        } catch (Exception e) {
            System.out.println("RemoveRepo failed.");
            e.printStackTrace();
            testResult = TestResult.FAIL;
        }

        return false;
    }

    public int sendRemoveRepoRequests(Map<String, Long> repoMap, String userOneId, String userTwoId) throws XmlException, IOException {
        // remove three repositories
        long repoId = repoMap.get(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID);
        if (!removeRepo(repoId, userOneId)) {
            report += "Error: Remove repo test failed for user one remove Google Drive\n";
            return FAILURE;
        }
        repoId = repoMap.get(IntegrationTesterFiles.REPO_DROPBOX_USER_ONE_VALID);
        if (!removeRepo(repoId, userOneId)) {
            report += "Error: Add repo test failed for user one remove Dropbox\n";
            return FAILURE;
        }
        repoId = repoMap.get(IntegrationTesterFiles.REPO_ONE_DRIVE_USER_TWO_VALID);
        if (!removeRepo(repoId, userTwoId)) {
            report += "Error: Add repo test failed for user two remove OneDrive\n";
            return FAILURE;
        }

        // removing a repo which was already removed
        if (removeRepo(repoId, userTwoId)) {
            report += "Error: Removed a repository twice from RMS\n";
            return FAILURE;
        }

        // update with invalid repoId
        repoId = getRepoIdInvalid(repoMap);

        if (removeRepo(repoId, userOneId)) {
            report += "Error: Removed unknown repository from RMS\n";
            return FAILURE;
        }

        return SUCCESS;
    }

    private boolean removeRepo(long repoId, String userId) throws XmlException, IOException {
        boolean removeRepoResult = sendRemoveRepoRequest(repoId, userId);

        return removeRepoResult;
    }

    private int getRepoIdInvalid(Map<String, Long> repoMap) {
        int repoId;
        Random r = new Random();
        do {
            repoId = r.nextInt();
        } while (repoMap.containsKey(repoId));

        return repoId;
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