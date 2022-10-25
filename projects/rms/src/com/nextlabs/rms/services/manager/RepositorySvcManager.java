package com.nextlabs.rms.services.manager;

import com.nextlabs.rms.entity.repository.TenantUser;
import com.nextlabs.rms.exception.DuplicateRepositoryNameException;
import com.nextlabs.rms.exception.RepositoryAlreadyExists;
import com.nextlabs.rms.exception.RepositoryNotFoundException;
import com.nextlabs.rms.exception.UnauthorizedOperationException;
import com.nextlabs.rms.exception.UserNotFoundException;
import com.nextlabs.rms.rmc.*;
import com.nextlabs.rms.rmc.AddRepositoryResponseDocument.AddRepositoryResponse;
import com.nextlabs.rms.rmc.RemoveRepositoryResponseDocument.RemoveRepositoryResponse;
import com.nextlabs.rms.rmc.UpdateRepositoryResponseDocument.UpdateRepositoryResponse;
import com.nextlabs.rms.rmc.types.RepositoryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Calendar;

public class RepositorySvcManager {
    private static Log logger = LogFactory.getLog(RepositorySvcManager.class.toString());

    private static RepositorySvcManager instance = new RepositorySvcManager();

    private RepositorySvcManager(){
    }

    public static RepositorySvcManager getInstance(){
        return instance;
    }

    public AddRepositoryResponseDocument addRepositoryRequest(AddRepositoryRequestDocument.AddRepositoryRequest request) throws UserNotFoundException, 
    RepositoryAlreadyExists, DuplicateRepositoryNameException {
        AddRepositoryResponseDocument doc = AddRepositoryResponseDocument.Factory.newInstance();
        AddRepositoryResponse response = doc.addNewAddRepositoryResponse();        
        RepositoryType repo = request.getRepository();
        String userId = request.getUserId();
        String tenantId = request.getTenantId();
        TenantUser tUser = new TenantUser(tenantId, userId);
        long repoId = RepositoryServiceAdaptor.getInstance().addRepository(tUser, repo);
        response.setRepoId(repoId);
        response.setStatus(ServiceUtil.getStatus(StatusTypeEnum.SUCCESS.getCode(), StatusTypeEnum.SUCCESS.getDescription()));
        return doc;
    }

    public RemoveRepositoryResponseDocument removeRepositoryRequest(RemoveRepositoryRequestDocument.RemoveRepositoryRequest request) 
    		throws RepositoryNotFoundException, UnauthorizedOperationException {
        RemoveRepositoryResponseDocument doc = RemoveRepositoryResponseDocument.Factory.newInstance();
        RemoveRepositoryResponse response = doc.addNewRemoveRepositoryResponse();
        long repoId = request.getRepoId();
        String userId = request.getUserId();
        String tenantId = request.getTenantId();
        TenantUser tUser = new TenantUser(tenantId, userId);
        boolean result = RepositoryServiceAdaptor.getInstance().removeRepository(tUser, repoId);
    		if (result){
    			response.setStatus(ServiceUtil.getStatus(StatusTypeEnum.SUCCESS.getCode(), StatusTypeEnum.SUCCESS.getDescription()));
    		}
    		else{
    			response.setStatus(ServiceUtil.getStatus(StatusTypeEnum.UNKNOWN.getCode(), StatusTypeEnum.UNKNOWN.getDescription()));
    		}
        return doc;
    }

    public GetRepositoryDetailsResponseDocument getRepositoryDetailsRequest(GetRepositoryDetailsRequestDocument.GetRepositoryDetailsRequest request) {
        GetRepositoryDetailsResponseDocument doc = GetRepositoryDetailsResponseDocument.Factory.newInstance();
        String userId = request.getUserId();
        String tenantId = request.getTenantId();
        Calendar timestamp = request.getFetchSinceGMTTimestamp();
        
        TenantUser tUser = new TenantUser(tenantId, userId);
        
        GetRepositoryDetailsResponseDocument.GetRepositoryDetailsResponse response = 
        		RepositoryServiceAdaptor.getInstance().fetchAndPopulateSyncData(tUser, timestamp.getTime());
        response.setStatus(ServiceUtil.getStatus(StatusTypeEnum.SUCCESS.getCode(), StatusTypeEnum.SUCCESS.getDescription()));
        doc.setGetRepositoryDetailsResponse(response);
        return doc;
    }

    public UpdateRepositoryResponseDocument updateRepositoryRequest(UpdateRepositoryRequestDocument.UpdateRepositoryRequest request) 
    		throws RepositoryNotFoundException, UserNotFoundException, UnauthorizedOperationException, DuplicateRepositoryNameException {
        UpdateRepositoryResponseDocument doc = UpdateRepositoryResponseDocument.Factory.newInstance();
        UpdateRepositoryResponse response = doc.addNewUpdateRepositoryResponse();

        RepositoryType repo = request.getRepository();
        String userId = request.getUserId();
        String tenantId = request.getTenantId();
        TenantUser tu = new TenantUser(tenantId, userId);
        boolean result = RepositoryServiceAdaptor.getInstance().updateRepository(tu, repo);
    		if (result){
    			response.setStatus(ServiceUtil.getStatus(StatusTypeEnum.SUCCESS.getCode(), StatusTypeEnum.SUCCESS.getDescription()));
    		}
    		else{
    			response.setStatus(ServiceUtil.getStatus(StatusTypeEnum.UNKNOWN.getCode(), StatusTypeEnum.UNKNOWN.getDescription()));
    		}
        return doc;
    }
}