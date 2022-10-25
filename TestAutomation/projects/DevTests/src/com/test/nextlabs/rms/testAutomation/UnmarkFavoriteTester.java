package com.test.nextlabs.rms.testAutomation;

import com.nextlabs.rms.rmc.UnmarkFavoriteRequestDocument;
import com.nextlabs.rms.rmc.UnmarkFavoriteResponseDocument;
import com.nextlabs.rms.rmc.types.CachedFileType;
import com.nextlabs.rms.rmc.types.StatusType;
import org.apache.xmlbeans.XmlException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UnmarkFavoriteTester implements TestInterface {
    private TestResult testResult = TestResult.NOT_EXECUTED;
    private String report = "";
    private static final int SUCCESS = 0;
    private static final int FAILURE = -1;

    private boolean sendUnmarkFavoriteRequest(String userTest, long[] cachedFileIds, String userId)  {
        System.out.println("Sending unmark favorite request for file " + userTest);
        try {
            String originalPath = IntegrationTester.inputDir + File.separator + TestingConfigManager.UNMARK_FAVORITE_FOLDER_NAME + File.separator;
            String inputFile = originalPath + IntegrationTesterFiles.OPERATION_UNMARK_FAVORITE + userTest + IntegrationTesterFiles.INPUT + ".xml";
            UnmarkFavoriteRequestDocument doc = UnmarkFavoriteRequestDocument.Factory.parse(new File(inputFile));
            doc.getUnmarkFavoriteRequest().setUserId(userId);
            doc.getUnmarkFavoriteRequest().getCachedFileIdList().setCachedFileIdArray(cachedFileIds);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            Map<String,String> headerMap = new HashMap<>();
            headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME, TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
            String responseString = HttpRequestUtil.sendPostRequest(baos.toByteArray(), "UnMarkFavoriteService", headerMap);
            UnmarkFavoriteResponseDocument response = UnmarkFavoriteResponseDocument.Factory.parse(responseString);
            StatusType status = response.getUnmarkFavoriteResponse().getStatus();
            if (status.getCode() == 0) {
                testResult = TestResult.PASS;

                return true;
            }
            else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("UnMarkFavorite failed.");
            e.printStackTrace();
            testResult = TestResult.FAIL;
        }

        return false;
    }

    public int sendUnmarkFavoriteRequests(Set<CachedFileType> favoritesSet, String userOneId) throws XmlException, IOException {
        long[] cachedFileIds = new long[favoritesSet.size()];
        int i=0;
        for (CachedFileType type : favoritesSet) {
            cachedFileIds[i] = type.getCachedFileId();
            ++i;
        }
        // valid
        if (!unmarkFavorite(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, cachedFileIds, userOneId)) {
            report += "Error: Unamrk favorite test failed for user one using Google Drive\n";
            return FAILURE;
        }

        // also valid - favorites have already been removed so there is nothing to unmark
        if (!unmarkFavorite(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, cachedFileIds, userOneId)) {
            report += "Error: Removed favorites which are already inactive\n";
            return FAILURE;
        }

        return SUCCESS;
    }

    private boolean unmarkFavorite(String repoName, long[] cachedFileIds, String userId) {
        UnmarkFavoriteTester umfTester = new UnmarkFavoriteTester();
        boolean unmarkFavoriteResult = umfTester.sendUnmarkFavoriteRequest(repoName, cachedFileIds, userId);

        return unmarkFavoriteResult;
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