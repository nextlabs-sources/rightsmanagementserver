package com.test.nextlabs.rms.testAutomation;

import com.nextlabs.rms.rmc.types.CachedFileType;
import org.apache.xmlbeans.XmlException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SyncTesterHelper {
    Map<String, Long> repoIdMap = new HashMap<String, Long>();
    String userOneId, userTwoId;

    public String testAllSyncServices() throws XmlException, IOException, InterruptedException {
        String report = "";

        userOneId = "one";
        userTwoId = "two";

        AddRepoTester addRepoTester = new AddRepoTester();
        int addRepoResult = addRepoTester.sendAddRepoRequests(repoIdMap, userOneId, userTwoId);
        report += addRepoTester.getReport();
        if (addRepoResult != 0){
            report += "AddRepo tests completed with an exception; exiting sync tests" + "\n";
            return report;
        }

        UpdateRepoTester updateRepoTester = new UpdateRepoTester();
        int updateRepoResult = updateRepoTester.sendUpdateRepoRequests(repoIdMap, userOneId, userTwoId);
        report += updateRepoTester.getReport();
        if (updateRepoResult != 0){
            report += "UpdateRepo tests completed with an exception; exiting sync tests" + "\n";
            return report;
        }

        GetRepositoryDetailsTester getRepositoryDetailsTester = new GetRepositoryDetailsTester();
        int getReposResult = getRepositoryDetailsTester.sendGetRepoRequestsAddRepoTest(repoIdMap, userOneId);
        report += getRepositoryDetailsTester.getReport();
        if (getReposResult != 0){
            report += "GetRepositoryDetails tests completed with an exception; exiting sync tests" + "\n";
            return report;
        }

        MarkFavoriteTester markFavoriteTester = new MarkFavoriteTester();
        int markFavoriteResult = markFavoriteTester.sendMarkFavoriteRequests(repoIdMap, userOneId);
        report += markFavoriteTester.getReport();
        if (markFavoriteResult != 0) {
            report += "MarkFavorite tests completed with an exception; exiting sync tests" + "\n";
            return report;
        }

        getRepositoryDetailsTester = new GetRepositoryDetailsTester();
        Set<CachedFileType> theirFavorites = getRepositoryDetailsTester.sendGetRepoRequestsMarkFavoriteTest(userOneId);
        report += getRepositoryDetailsTester.getReport();
        if (theirFavorites == null){
            report += "GetRepositoryDetails tests completed with an exception; exiting sync tests" + "\n";
            return report;
        }

        // unmark favorites
        UnmarkFavoriteTester unmarkFavoriteTester = new UnmarkFavoriteTester();
        int unmarkFavoriteResult = unmarkFavoriteTester.sendUnmarkFavoriteRequests(theirFavorites, userOneId);
        if (unmarkFavoriteResult != 0) {
            report += "UnmarkFavorite tests completed with an exception; exiting sync tests" + "\n";
            return report;
        }

        // mark offline
        MarkOfflineTester markOfflineTester = new MarkOfflineTester();
        int markOfflineResult = markOfflineTester.sendMarkOfflineRequests(repoIdMap, userOneId);
        if (markOfflineResult != 0) {
            report += "MarkOffline tests completed with an exception; exiting sync tests" + "\n";
            return report;
        }

        getRepositoryDetailsTester = new GetRepositoryDetailsTester();
        Set<CachedFileType> theirOfflines = getRepositoryDetailsTester.sendGetRepoRequestsMarkOfflineTest(userOneId);
        if (theirOfflines == null){
            report += "GetRepositoryDetails tests completed with an exception; exiting sync tests" + "\n";
            return report;
        }

        // unmark offlines
        UnmarkOfflineTester unmarkOfflineTester = new UnmarkOfflineTester();
        int unmarkOfflineResult = unmarkOfflineTester.sendUnmarkOfflineRequests(theirOfflines, userOneId);
        if (unmarkOfflineResult != 0) {
            report += "UnmarkOffline tests completed with an exception; exiting sync tests" + "\n";
            return report;
        }

        RemoveRepoTester removeRepoTester = new RemoveRepoTester();
        int removeRepoResult = removeRepoTester.sendRemoveRepoRequests(repoIdMap, userOneId, userTwoId);
        if (removeRepoResult != 0){
            report += "RemoveRepo tests completed with an exception; exiting sync tests" + "\n";
            return report;
        }

        report = "Sync services tests were successful";

        return report;
    }
}