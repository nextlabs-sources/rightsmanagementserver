package com.nextlabs.rms.services.manager;

import com.nextlabs.rms.exception.RepositoryNotFoundException;
import com.nextlabs.rms.exception.UserNotFoundException;
import com.nextlabs.rms.rmc.*;
import com.nextlabs.rms.rmc.MarkFavoriteResponseDocument.MarkFavoriteResponse;
import com.nextlabs.rms.rmc.MarkOfflineResponseDocument.MarkOfflineResponse;
import com.nextlabs.rms.rmc.UnmarkFavoriteResponseDocument.UnmarkFavoriteResponse;
import com.nextlabs.rms.rmc.UnmarkOfflineResponseDocument.UnmarkOfflineResponse;
import com.nextlabs.rms.rmc.types.CachedFileIdListType;
import com.nextlabs.rms.rmc.types.CachedFileListType;
import com.nextlabs.rms.rmc.types.CachedFileListInputType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;

public class CachedFileSvcManager {
    private static Log logger = LogFactory.getLog(CachedFileSvcManager.class.toString());

    private static CachedFileSvcManager instance = new CachedFileSvcManager();

    private CachedFileSvcManager(){
    }

    public static CachedFileSvcManager getInstance(){
        return instance;
    }

	public MarkFavoriteResponseDocument markFavoriteRequest(MarkFavoriteRequestDocument.MarkFavoriteRequest request)
			throws UserNotFoundException, RepositoryNotFoundException {
		CachedFileListInputType favorites = request.getCachedFileList();
		String userId = request.getUserId();
		String tenantId = request.getTenantId();
		CachedFileListType favoritesResult = CachedFileServiceAdaptor.getInstance().markFavoriteFiles(tenantId, userId, favorites);
		MarkFavoriteResponseDocument responseDoc = MarkFavoriteResponseDocument.Factory.newInstance();
		MarkFavoriteResponse response = responseDoc.addNewMarkFavoriteResponse();
		response.setCachedFileResultList(favoritesResult);
		response.setStatus(ServiceUtil.getStatus(StatusTypeEnum.SUCCESS.getCode(), StatusTypeEnum.SUCCESS.getDescription()));
		return responseDoc;
	}

	public UnmarkFavoriteResponseDocument unmarkFavoriteRequest(
			UnmarkFavoriteRequestDocument.UnmarkFavoriteRequest request) throws XmlException {
		CachedFileIdListType favorites = request.getCachedFileIdList();
		boolean result = CachedFileServiceAdaptor.getInstance().unMarkFavoriteFiles(favorites);
		UnmarkFavoriteResponseDocument responseDoc = UnmarkFavoriteResponseDocument.Factory.newInstance();
		UnmarkFavoriteResponse response = responseDoc.addNewUnmarkFavoriteResponse();
		if (result){
			response.setStatus(ServiceUtil.getStatus(StatusTypeEnum.SUCCESS.getCode(), StatusTypeEnum.SUCCESS.getDescription()));
		}
		else{
			response.setStatus(ServiceUtil.getStatus(StatusTypeEnum.UNKNOWN.getCode(), StatusTypeEnum.UNKNOWN.getDescription()));
		}
		return responseDoc;
	}

	public MarkOfflineResponseDocument markOfflineRequest(MarkOfflineRequestDocument.MarkOfflineRequest request)
			throws UserNotFoundException, RepositoryNotFoundException {
		CachedFileListInputType offlines = request.getCachedFileList();
		String userId = request.getUserId();
		String tenantId = request.getTenantId();
		CachedFileListType offlinesResult = CachedFileServiceAdaptor.getInstance().markOfflineFiles(tenantId, userId, offlines);
		MarkOfflineResponseDocument responseDoc = MarkOfflineResponseDocument.Factory.newInstance();
		MarkOfflineResponse response = responseDoc.addNewMarkOfflineResponse();
		response.setCachedFileResultList(offlinesResult);
		response
				.setStatus(ServiceUtil.getStatus(StatusTypeEnum.SUCCESS.getCode(), StatusTypeEnum.SUCCESS.getDescription()));
		return responseDoc;
	}

	public UnmarkOfflineResponseDocument unmarkOfflineRequest(UnmarkOfflineRequestDocument.UnmarkOfflineRequest request)
			throws XmlException {
		CachedFileIdListType favorites = request.getCachedFileIdList();
		boolean result = CachedFileServiceAdaptor.getInstance().unMarkOfflineFiles(favorites);
		UnmarkOfflineResponseDocument responseDoc = UnmarkOfflineResponseDocument.Factory.newInstance();
		UnmarkOfflineResponse response = responseDoc.addNewUnmarkOfflineResponse();
		if (result){
			response.setStatus(ServiceUtil.getStatus(StatusTypeEnum.SUCCESS.getCode(), StatusTypeEnum.SUCCESS.getDescription()));
		}
		else{
			response.setStatus(ServiceUtil.getStatus(StatusTypeEnum.UNKNOWN.getCode(), StatusTypeEnum.UNKNOWN.getDescription()));
		}
		return responseDoc;
	}
}