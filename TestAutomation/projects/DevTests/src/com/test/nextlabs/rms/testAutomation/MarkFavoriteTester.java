package com.test.nextlabs.rms.testAutomation;

import com.nextlabs.rms.rmc.MarkFavoriteRequestDocument;
import com.nextlabs.rms.rmc.MarkFavoriteResponseDocument;
import com.nextlabs.rms.rmc.types.CachedFileType;
import com.nextlabs.rms.rmc.types.StatusType;
import org.apache.xmlbeans.XmlException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MarkFavoriteTester implements TestInterface {
    private TestResult testResult = TestResult.NOT_EXECUTED;
    private String report = "";
    private static final int SUCCESS = 0;
    private static final int FAILURE = -1;

    private boolean sendMarkFavoriteRequest(String userTest, long repoId, String userId)  {
        System.out.println("Sending mark favorite request for file " + userTest);

        try {
            String originalPath = IntegrationTester.inputDir + File.separator + TestingConfigManager.MARK_FAVORITE_FOLDER_NAME + File.separator;
            String inputFile = originalPath + IntegrationTesterFiles.OPERATION_MARK_FAVORITE + userTest + IntegrationTesterFiles.INPUT + ".xml";
            MarkFavoriteRequestDocument doc = MarkFavoriteRequestDocument.Factory.parse(new File(inputFile));
            doc.getMarkFavoriteRequest().setUserId(userId);
            CachedFileType[] cachedFileArray = doc.getMarkFavoriteRequest().getCachedFileList().getCachedFileArray();
            for (CachedFileType cft : cachedFileArray) {
                cft.setRepoId(repoId);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            Map<String,String> headerMap = new HashMap<>();
            headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME, TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
            String responseString = HttpRequestUtil.sendPostRequest(baos.toByteArray(), "MarkFavoriteService", headerMap);
            MarkFavoriteResponseDocument response = MarkFavoriteResponseDocument.Factory.parse(responseString);
            StatusType status = response.getMarkFavoriteResponse().getStatus();
            if (status.getCode() == 0) {
                testResult = TestResult.PASS;

                return true;
            }
            else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("MarkFavorite failed.");
            e.printStackTrace();
            testResult = TestResult.FAIL;
        }

        return false;
    }

    public int sendMarkFavoriteRequests(Map<String, Long> repoMap, String userOneId) throws XmlException, IOException {
        // valid
        long repoId = repoMap.get(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID);
        if (!markFavorite(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, repoId, userOneId)) {
            report += "Error: Mark favorite test failed for user one using Google Drive\n";
            return FAILURE;
        }

        // unknown user id
        if (markFavorite(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, repoId, "-1")) {
            report += "Error: Mark favorites test passed with invalid user id\n";
            return FAILURE;
        }

        // mark favorite with invalid repoId
        repoId = getRepoIdInvalid(repoMap);

        if (markFavorite(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, repoId, userOneId)) {
            report += "Error: Mark favorites test passed with invalid repoId\n";
            return FAILURE;
        }

        return SUCCESS;
    }

    private boolean markFavorite(String repoName, long repoId, String userId) {
        boolean markFavoriteResult = sendMarkFavoriteRequest(repoName, repoId, userId);

        return markFavoriteResult;
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