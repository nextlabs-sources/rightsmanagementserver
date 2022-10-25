package com.test.nextlabs.rms.testAutomation;

import com.nextlabs.rms.rmc.UnmarkOfflineRequestDocument;
import com.nextlabs.rms.rmc.UnmarkOfflineResponseDocument;
import com.nextlabs.rms.rmc.types.CachedFileType;
import com.nextlabs.rms.rmc.types.StatusType;
import org.apache.xmlbeans.XmlException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UnmarkOfflineTester implements TestInterface {
    private TestResult testResult = TestResult.NOT_EXECUTED;
    private String report = "";
    private static final int SUCCESS = 0;
    private static final int FAILURE = -1;

    public boolean sendUnmarkOfflineRequest(String userTest, long[] cachedFileIds, String userId)  {
        System.out.println("Sending unmark offline request for file " + userTest);

        try {
            String originalPath = IntegrationTester.inputDir + File.separator + TestingConfigManager.UNMARK_OFFLINE_FOLDER_NAME + File.separator;
            String inputFile = originalPath + IntegrationTesterFiles.OPERATION_UNMARK_OFFLINE + userTest + IntegrationTesterFiles.INPUT + ".xml";
            UnmarkOfflineRequestDocument doc = UnmarkOfflineRequestDocument.Factory.parse(new File(inputFile));
            doc.getUnmarkOfflineRequest().setUserId(userId);
            doc.getUnmarkOfflineRequest().getCachedFileIdList().setCachedFileIdArray(cachedFileIds);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            Map<String,String> headerMap = new HashMap<>();
            headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME, TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
            String responseString = HttpRequestUtil.sendPostRequest(baos.toByteArray(), "UnMarkOfflineService", headerMap);
            UnmarkOfflineResponseDocument response = UnmarkOfflineResponseDocument.Factory.parse(responseString);
            StatusType status = response.getUnmarkOfflineResponse().getStatus();
            if (status.getCode() == 0) {
                testResult = TestResult.PASS;

                return true;
            }
            else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("UnMarkOffline failed.");
            e.printStackTrace();
            testResult = TestResult.FAIL;
        }

        return false;
    }

    public int sendUnmarkOfflineRequests(Set<CachedFileType> offlinesSet, String userOneId) throws XmlException, IOException {
        long[] cachedFileIds = new long[offlinesSet.size()];
        int i=0;
        for (CachedFileType type : offlinesSet) {
            cachedFileIds[i] = type.getCachedFileId();
            ++i;
        }
        // valid
        if (!unmarkOffline(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, cachedFileIds, userOneId)) {
            report += "Error: Unmark offline test failed for user one using Google Drive\n";
            return FAILURE;
        }

        // also valid - offlines have already been removed so there is nothing to unmark
        if (!unmarkOffline(IntegrationTesterFiles.REPO_GOOGLE_DRIVE_USER_ONE_VALID, cachedFileIds, userOneId)) {
            report += "Error: Removed offline files which are already inactive\n";
            return FAILURE;
        }

        return SUCCESS;
    }

    private boolean unmarkOffline(String repoName, long[] cachedFileIds, String userId) {
        boolean unmarkOfflineResult = sendUnmarkOfflineRequest(repoName, cachedFileIds, userId);

        return unmarkOfflineResult;
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