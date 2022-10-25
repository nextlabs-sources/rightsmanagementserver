package com.test.nextlabs.rms.testAutomation;

import com.nextlabs.rms.rmc.MarkOfflineRequestDocument;
import com.nextlabs.rms.rmc.MarkOfflineResponseDocument;
import com.nextlabs.rms.rmc.types.CachedFileType;
import com.nextlabs.rms.rmc.types.StatusType;
import org.apache.xmlbeans.XmlException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MarkOfflineTester implements TestInterface {
    private TestResult testResult = TestResult.NOT_EXECUTED;
    private String report = "";
    private static final int SUCCESS = 0;
    private static final int FAILURE = -1;

    private boolean sendMarkOfflineRequest(String userTest, long repoId, String userId)  {
        System.out.println("Sending mark offline request for file " + userTest);

        try {
            String originalPath = IntegrationTester.inputDir + File.separator + TestingConfigManager.MARK_OFFLINE_FOLDER_NAME + File.separator;
            String inputFile = originalPath + IntegrationTesterFiles.OPERATION_MARK_OFFLINE + userTest + IntegrationTesterFiles.INPUT + ".xml";
            MarkOfflineRequestDocument doc = MarkOfflineRequestDocument.Factory.parse(new File(inputFile));
            doc.getMarkOfflineRequest().setUserId(userId);
            CachedFileType[] cachedFileArray = doc.getMarkOfflineRequest().getCachedFileList().getCachedFileArray();
            for (CachedFileType cft : cachedFileArray) {
                cft.setRepoId(repoId);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            Map<String,String> headerMap = new HashMap<>();
            headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME, TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
            String responseString = HttpRequestUtil.sendPostRequest(baos.toByteArray(), "MarkOfflineService", headerMap);
            MarkOfflineResponseDocument response = MarkOfflineResponseDocument.Factory.parse(responseString);
            StatusType status = response.getMarkOfflineResponse().getStatus();
            if (status.getCode() == 0) {
                testResult = TestResult.PASS;

                return true;
            }
            else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("MarkOffline failed.");
            e.printStackTrace();
            testResult = TestResult.FAIL;
        }

        return false;
    }

    public int sendMarkOfflineRequests(Map<String, Long> repoMap, String userOneId) throws XmlException, IOException {
        // valid
        long repoId = repoMap.get(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID);
        if (!markOffline(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, repoId, userOneId)) {
            report += "Error: Mark offline test failed for user one using Google Drive\n";
            report += "repoId = " + repoId + " and userId = " + userOneId + "\n";
            return FAILURE;
        }

        // unknown user id
        if (markOffline(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, repoId, "-1")) {
            report += "Error: Mark offline test passed with invalid user id\n";
            report += "repoId = " + repoId + " and userId = -1" + "\n";
            return FAILURE;
        }

        // mark offline with invalid repoId
        repoId = getRepoIdInvalid(repoMap);

        if (markOffline(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, repoId, userOneId)) {
            report += "Error: Mark offline test passed with invalid repoId\n";
            report += "repoId = " + repoId + " and userId = " + userOneId + "\n";
            return FAILURE;
        }

        return SUCCESS;
    }

    private boolean markOffline(String repoName, long repoId, String userId) {
        boolean markOfflineResult = sendMarkOfflineRequest(repoName, repoId, userId);

        return markOfflineResult;
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