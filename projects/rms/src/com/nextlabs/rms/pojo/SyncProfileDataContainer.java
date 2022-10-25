/**
 * 
 */
package com.nextlabs.rms.pojo;

import java.util.List;

import com.nextlabs.rms.dto.repository.RepositoryDTO;
import com.nextlabs.rms.entity.repository.FavoriteFileDO;
import com.nextlabs.rms.entity.repository.OfflineFileDO;

/**
 * @author nnallagatla
 * This class acts as container for the data that needs to be sent as part of sync profile request
 *
 */
public class SyncProfileDataContainer {
	public SyncProfileDataContainer() {
	}
	
	boolean isFullCopy = false;
	
	List<RepositoryDTO> repositoryList;
	
	List<FavoriteFileDO> favoriteFileList;
	
	List<OfflineFileDO> offlineFileList;
	
	List<Long> deletedRepositoryList;
	
	List<Long> deletedFavoriteFileList;
	
	List<Long> deletedOfflineFileList;

	public List<RepositoryDTO> getRepositoryList() {
		return repositoryList;
	}

	public void setRepositoryList(List<RepositoryDTO> repositoryList) {
		this.repositoryList = repositoryList;
	}

	public List<FavoriteFileDO> getFavoriteFileList() {
		return favoriteFileList;
	}

	public void setFavoriteFileList(List<FavoriteFileDO> favoriteFileList) {
		this.favoriteFileList = favoriteFileList;
	}

	public List<OfflineFileDO> getOfflineFileList() {
		return offlineFileList;
	}

	public void setOfflineFileList(List<OfflineFileDO> offlineFileList) {
		this.offlineFileList = offlineFileList;
	}

	public boolean isFullCopy() {
		return isFullCopy;
	}

	public void setFullCopy(boolean isFullCopy) {
		this.isFullCopy = isFullCopy;
	}

	public List<Long> getDeletedRepositoryList() {
		return deletedRepositoryList;
	}

	public void setDeletedRepositoryList(List<Long> deletedRepositoryList) {
		this.deletedRepositoryList = deletedRepositoryList;
	}

	public List<Long> getDeletedFavoriteFileList() {
		return deletedFavoriteFileList;
	}

	public void setDeletedFavoriteFileList(List<Long> deletedFavoriteFileList) {
		this.deletedFavoriteFileList = deletedFavoriteFileList;
	}

	public List<Long> getDeletedOfflineFileList() {
		return deletedOfflineFileList;
	}

	public void setDeletedOfflineFileList(List<Long> deletedOfflineFileList) {
		this.deletedOfflineFileList = deletedOfflineFileList;
	}

}
