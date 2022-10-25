package com.test.nextlabs.rms.testAutomation;

import com.nextlabs.rms.rmc.GetRepositoryDetailsRequestDocument;
import com.nextlabs.rms.rmc.GetRepositoryDetailsResponseDocument;
import com.nextlabs.rms.rmc.types.*;
import org.apache.xmlbeans.XmlException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GetRepositoryDetailsTester implements TestInterface {
    private TestResult testResult = TestResult.NOT_EXECUTED;
    private String report = "";
    private static final int SUCCESS = 0;
    private static final int FAILURE = -1;

    private GetRepositoryDetailsResponseDocument sendGetRepositoryDetailsRequest(String userTest, String userId)  {
        GetRepositoryDetailsResponseDocument response = null;
        try {
            String originalPath = IntegrationTester.inputDir + File.separator + TestingConfigManager.GET_REPOSITORY_DETAILS_FOLDER_NAME + File.separator;
            String inputFile = originalPath + IntegrationTesterFiles.OPERATION_GET_REPOSITORY_DETAILS + userTest + IntegrationTesterFiles.INPUT + ".xml";
            GetRepositoryDetailsRequestDocument doc = GetRepositoryDetailsRequestDocument.Factory.parse(new File(inputFile));
            doc.getGetRepositoryDetailsRequest().setUserId(userId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            Map<String,String> headerMap = new HashMap<>();
            headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME, TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
            String responseString = HttpRequestUtil.sendPostRequest(baos.toByteArray(), "GetRepositoryDetailsService", headerMap);
            response = GetRepositoryDetailsResponseDocument.Factory.parse(responseString);
            StatusType status = response.getGetRepositoryDetailsResponse().getStatus();
            if (status.getCode() == 0) {
                testResult = TestResult.PASS;
            }
            else {
                testResult = TestResult.FAIL;
            }
        } catch (Exception e) {
            System.out.println("GetRepositoryDetails failed.");
            e.printStackTrace();
            testResult = TestResult.FAIL;
        }

        return response;
    }

    public int sendGetRepoRequestsAddRepoTest(Map<String, Long> repoMap, String userOneId) throws XmlException, IOException {
        // valid call
        Set<RepositoryType> theirRepos = getRepos(IntegrationTesterFiles.REPOSITORY_DETAILS_USER_ONE_VALID, userOneId);
        if (theirRepos == null || theirRepos.size() != 2) {
            report += "Found wrong number of repos for user one\n";
            return FAILURE;
        }
        if (!checkRepoIds(repoMap.values(), theirRepos)) {
            report += "Repos returned had the wrong Ids\n";
            return FAILURE;
        }

        // call with invalid user id
        theirRepos = getRepos(IntegrationTesterFiles.REPOSITORY_DETAILS_USER_ONE_VALID, "-1");
        if (theirRepos != null && theirRepos.size() != 0) {
            report += "Found repositories for invalid user\n";
            return FAILURE;
        }

        return SUCCESS;
    }

    public Set<CachedFileType> sendGetRepoRequestsMarkFavoriteTest(String userOneId) throws XmlException, IOException {
        Set<CachedFileType> theirFavorites = getFavorites(IntegrationTesterFiles.REPOSITORY_DETAILS_USER_ONE_VALID, userOneId);
        if (theirFavorites == null || theirFavorites.size() != 3) {
            report += "Found wrong number of favorites for user 1.\n";
            return null;
        }

        return theirFavorites;
    }

    public Set<CachedFileType> sendGetRepoRequestsMarkOfflineTest(String userOneId) throws XmlException, IOException {
        Set<CachedFileType> theirOfflines = getOfflines(IntegrationTesterFiles.REPOSITORY_DETAILS_USER_ONE_VALID, userOneId);
        if (theirOfflines == null || theirOfflines.size() != 3) {
            report += "Found wrong number of offline files for user 1.\n";
            return null;
        }

        return theirOfflines;
    }

    private Set<RepositoryType> getRepos(String test, String userId) {
        Set<RepositoryType> repos = new HashSet<RepositoryType>();

        GetRepositoryDetailsResponseDocument getRepositoryDetailsResult = sendGetRepositoryDetailsRequest(test, userId);
        if (getRepositoryDetailsResult == null) {
            return null;
        }
        else {
            int errorCode = getRepositoryDetailsResult.getGetRepositoryDetailsResponse().getStatus().getCode();
            if (errorCode != 0) {
                return null;
            }
            RepositoryListType repoItems = getRepositoryDetailsResult.getGetRepositoryDetailsResponse().getRepoItems();
            for (RepositoryType repoType : repoItems.getRepositoryArray()) {
                repos.add(repoType);
            }
        }

        return repos;
    }

    private Set<CachedFileType> getFavorites(String test, String userId) {
        Set<CachedFileType> favorites = new HashSet<CachedFileType>();

        GetRepositoryDetailsResponseDocument getRepositoryDetailsResult = sendGetRepositoryDetailsRequest(test, userId);
        if (getRepositoryDetailsResult == null) {
            return null;
        }
        else {
            int errorCode = getRepositoryDetailsResult.getGetRepositoryDetailsResponse().getStatus().getCode();
            if (errorCode != 0) {
                return null;
            }
            CachedFileListType favoriteItems = getRepositoryDetailsResult.getGetRepositoryDetailsResponse().getFavoriteItems();
            for (CachedFileType fileType : favoriteItems.getCachedFileArray()) {
                favorites.add(fileType);
            }
        }

        return favorites;
    }

    Set<CachedFileType> getOfflines(String test, String userId) {
        Set<CachedFileType> offlines = new HashSet<CachedFileType>();

        GetRepositoryDetailsTester grdTester = new GetRepositoryDetailsTester();
        GetRepositoryDetailsResponseDocument getRepositoryDetailsResult = grdTester.sendGetRepositoryDetailsRequest(test, userId);
        report += grdTester.getReport() + "\n";
        if (getRepositoryDetailsResult == null) {
            return null;
        }
        else {
            int errorCode = getRepositoryDetailsResult.getGetRepositoryDetailsResponse().getStatus().getCode();
            if (errorCode != 0) {
                return null;
            }
            CachedFileListType offlineItems = getRepositoryDetailsResult.getGetRepositoryDetailsResponse().getOfflineItems();
            for (CachedFileType fileType : offlineItems.getCachedFileArray()) {
                offlines.add(fileType);
            }
        }

        return offlines;
    }

    private boolean checkRepoIds(Collection<Long> repoIds, Set<RepositoryType> repos) {
        for (RepositoryType repo : repos) {
            if (!repoIds.contains(repo.getRepoId())) {
                return false;
            }
        }

        return true;
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