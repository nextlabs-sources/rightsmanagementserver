/**
 * 
 */
package com.nextlabs.rms.rmc;

import com.nextlabs.rms.dto.repository.AuthorizedRepoUserDTO;
import com.nextlabs.rms.dto.repository.RepositoryDTO;
import com.nextlabs.rms.entity.repository.TenantUser;
import com.nextlabs.rms.exception.DuplicateRepositoryNameException;
import com.nextlabs.rms.exception.RepositoryAlreadyExists;
import com.nextlabs.rms.exception.RepositoryNotFoundException;
import com.nextlabs.rms.exception.UnauthorizedOperationException;
import com.nextlabs.rms.exception.UserNotFoundException;
import com.nextlabs.rms.pojo.SyncProfileDataContainer;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.rmc.GetRepositoryDetailsResponseDocument.GetRepositoryDetailsResponse;
import com.nextlabs.rms.rmc.types.DeletedItemIdListType;
import com.nextlabs.rms.rmc.types.RepoTypeEnum;
import com.nextlabs.rms.rmc.types.RepositoryListType;
import com.nextlabs.rms.rmc.types.RepositoryType;

import java.util.Date;
import java.util.List;

/**
 * @author nnallagatla
 *
 */
public class RepositoryServiceAdaptor {
	private RepositoryServiceAdaptor() {
	}
	
	private static final RepositoryServiceAdaptor instance = new RepositoryServiceAdaptor();
	
	public static RepositoryServiceAdaptor getInstance(){
		return instance;
	}
	
	public long addRepository(TenantUser tUser, RepositoryType repo) throws UserNotFoundException, RepositoryAlreadyExists, DuplicateRepositoryNameException{
		RepositoryDTO dto = convertToDTO(tUser, repo, true);
		dto = RepositoryManager.getInstance().addRepository(dto);
		return dto.getId();
	}
	
	private RepositoryDTO convertToDTO(TenantUser tUser, RepositoryType repo, boolean create) {
		RepositoryDTO dto = new RepositoryDTO();
		if (!create) {
			dto.setId(repo.getRepoId());
		}
		dto.setCreatedByUserId(tUser.getUserId());
		dto.setCurrentUser(tUser.getUserId());
		dto.setRepoAccountId(repo.getAccountId());
        dto.setRepoType(repo.getRepoType().toString());
        dto.setRepoName(repo.getRepoDisplayName());
        dto.setTenantId(tUser.getTenantId());

		if (repo.getAccount() != null && repo.getAccountId() != null && repo.xgetRefreshToken() != null){
			AuthorizedRepoUserDTO authRepoUserDTO = new AuthorizedRepoUserDTO();
			if (!create) {
				authRepoUserDTO.setRepoId(dto.getId());
			}
			authRepoUserDTO.setAccountId(repo.getAccountId());
			authRepoUserDTO.setAccountName(repo.getAccount());
			authRepoUserDTO.setRefreshToken(repo.getRefreshToken());
            authRepoUserDTO.setUserId(tUser.getUserId());
            authRepoUserDTO.setTenantId(tUser.getTenantId());
			dto.setAuthorizedUser(authRepoUserDTO);
		}
		return dto;
	}

	public boolean updateRepository(TenantUser tUser, RepositoryType repo) throws UserNotFoundException,
			RepositoryNotFoundException, UnauthorizedOperationException, DuplicateRepositoryNameException {
		RepositoryDTO dto = convertToDTO(tUser, repo, false);
		return RepositoryManager.getInstance().updateRepository(dto);
	}

	
	public boolean removeRepository(TenantUser tUser, long repoId) throws RepositoryNotFoundException, UnauthorizedOperationException{
		return RepositoryManager.getInstance().deleteRepository(false, tUser, repoId);
	}
	
	private void convertFromDTO(RepositoryDTO repoDTO, RepositoryType repo){
		if (repoDTO == null || repo == null){
			return;
		}
		repo.setAccountId(repoDTO.getRepoAccountId());
		repo.setIsShared(repoDTO.isShared());
		repo.setRepoId(repoDTO.getId());
		repo.setRepoDisplayName(repoDTO.getRepoName());
		repo.setRepoType(RepoTypeEnum.Enum.forString(repoDTO.getRepoType()));
		
		if (repoDTO.getAuthorizedUser() != null) {
			repo.setAccount(repoDTO.getAuthorizedUser().getAccountName());
			//TODO do we need to send user accountId to rmc mobile?
			repo.setRefreshToken(repoDTO.getAuthorizedUser().getRefreshToken());
		}
		
		return;
	}

	private RepositoryListType convertToRepoItems(List<RepositoryDTO> repoDTOList){
		RepositoryListType repoList = RepositoryListType.Factory.newInstance();
		for (RepositoryDTO repoDTO : repoDTOList){
				convertFromDTO(repoDTO, repoList.addNewRepository());
		}
		return repoList;
	}
	
	public GetRepositoryDetailsResponse fetchAndPopulateSyncData(TenantUser tUser, Date timestamp){
		GetRepositoryDetailsResponse response = GetRepositoryDetailsResponse.Factory.newInstance();
		SyncProfileDataContainer container = RepositoryManager.getInstance().getSyncDataUpdatedOnOrAfter(
				tUser, timestamp);
		response.setIsFullCopy(container.isFullCopy());
		response.setRepoItems(convertToRepoItems(container.getRepositoryList()));
		response.setFavoriteItems(CachedFileServiceAdaptor.getInstance().convertFromCachedFileDO(container.getFavoriteFileList()));
		response.setOfflineItems(CachedFileServiceAdaptor.getInstance().convertFromCachedFileDO(container.getOfflineFileList()));
		
		if (!container.isFullCopy()){
			response.setDeletedRepoItems(convertToDeletedItemListType(container.getDeletedRepositoryList()));
			response.setDeletedFavoriteItems(convertToDeletedItemListType(container.getDeletedFavoriteFileList()));
			response.setDeletedOfflineItems(convertToDeletedItemListType(container.getDeletedOfflineFileList()));
		}
		return response;
	}
	
	private DeletedItemIdListType convertToDeletedItemListType(List<Long> deletedList){
		DeletedItemIdListType list = DeletedItemIdListType.Factory.newInstance();
		
		if (deletedList == null || deletedList.isEmpty()){
			return list;
		}
		
		for (Long id : deletedList){
			list.addItemId(id);
		}
		return list;
	}
	
}
