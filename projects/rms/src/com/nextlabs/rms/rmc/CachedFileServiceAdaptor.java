/**
 * 
 */
package com.nextlabs.rms.rmc;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.nextlabs.rms.entity.repository.CachedFileDO;
import com.nextlabs.rms.entity.repository.TenantUser;
import com.nextlabs.rms.entity.repository.FavoriteFileDO;
import com.nextlabs.rms.entity.repository.OfflineFileDO;
import com.nextlabs.rms.entity.repository.RepositoryDO;
import com.nextlabs.rms.exception.RepositoryNotFoundException;
import com.nextlabs.rms.exception.UserNotFoundException;
import com.nextlabs.rms.persistence.EntityManagerHelper;
import com.nextlabs.rms.persistence.RMCCachedFilePersistenceManager;
import com.nextlabs.rms.rmc.types.CachedFileIdListType;
import com.nextlabs.rms.rmc.types.CachedFileListInputType;
import com.nextlabs.rms.rmc.types.CachedFileListType;
import com.nextlabs.rms.rmc.types.CachedFileType;

/**
 * @author nnallagatla
 *
 */
public class CachedFileServiceAdaptor {
	private CachedFileServiceAdaptor() {
	}

	private static final CachedFileServiceAdaptor INSTANCE = new CachedFileServiceAdaptor();

	public static CachedFileServiceAdaptor getInstance() {
		return INSTANCE;
	}

	public CachedFileListType markFavoriteFiles(String tenantId, String userId, CachedFileListInputType cachedFileList)
			throws RepositoryNotFoundException, UserNotFoundException {
		try {
			List<FavoriteFileDO> favorites = convertToFavoriteFileDO(tenantId, userId, cachedFileList);
			RMCCachedFilePersistenceManager.getInstance().markCachedFiles(favorites);
			return convertFromCachedFileDO(favorites);
		}
		finally {
			EntityManagerHelper.closeEntityManager();
		}
	}

	public CachedFileListType markOfflineFiles(String tenantId, String userId, CachedFileListInputType cachedFileList)
			throws UserNotFoundException, RepositoryNotFoundException {
		try {
			List<OfflineFileDO> favorites = convertToOfflineFileDO(tenantId, userId, cachedFileList);
			RMCCachedFilePersistenceManager.getInstance().markCachedFiles(favorites);
			return convertFromCachedFileDO(favorites);
		}
		finally {
			EntityManagerHelper.closeEntityManager();
		}
	}

	public boolean unMarkOfflineFiles(CachedFileIdListType cachedFileIdList) {
		try {
			return RMCCachedFilePersistenceManager.getInstance().unMarkOfflineFiles(cachedFileIdList);
		}
		finally {
			EntityManagerHelper.closeEntityManager();
		}
	}

	public boolean unMarkFavoriteFiles(CachedFileIdListType cachedFileIdList) {
		try {
			return RMCCachedFilePersistenceManager.getInstance().unMarkFavoriteFiles(cachedFileIdList);
		}
		finally {
			EntityManagerHelper.closeEntityManager();
		}
	}

	public <T> CachedFileListType convertFromCachedFileDO(List<? extends CachedFileDO> cachedFileDOList) {
		CachedFileListType cfList = CachedFileListType.Factory.newInstance();
		
		if (cachedFileDOList == null){
			return cfList;
		}
		
		for (CachedFileDO entry : cachedFileDOList) {
			CachedFileType cf = cfList.addNewCachedFile();
			cf.setCachedFileId(entry.getId());
			cf.setRepoId(entry.getRepository().getId());
			cf.setFilePath(entry.getFileId());
		}
		return cfList;
	}

	private List<OfflineFileDO> convertToOfflineFileDO(String tenantId, String userId, CachedFileListInputType cachedFiles)
			throws UserNotFoundException, RepositoryNotFoundException {

		List<OfflineFileDO> offlineList = new ArrayList<>();
		EntityManager em = EntityManagerHelper.getEntityManager();
		for (CachedFileType cachedFile : cachedFiles.getCachedFileArray()) {
			OfflineFileDO off = new OfflineFileDO();
			off.setActive(true);
			RepositoryDO repo = ServiceUtil.getRepoReference(em, cachedFile.getRepoId());

			if (repo == null) {
				throw new RepositoryNotFoundException(cachedFile.getRepoId());
			}
			off.setRepository(repo);

			off.setUser(new TenantUser(tenantId, userId));
			off.setFileId(cachedFile.getFilePath());
			offlineList.add(off);
		}
		return offlineList;
	}

	private List<FavoriteFileDO> convertToFavoriteFileDO(String tenantId, String userId, CachedFileListInputType cachedFiles)
			throws RepositoryNotFoundException, UserNotFoundException {

		List<FavoriteFileDO> favList = new ArrayList<>();
		EntityManager em = EntityManagerHelper.getEntityManager();
		for (CachedFileType cachedFile : cachedFiles.getCachedFileArray()) {
			FavoriteFileDO fav = new FavoriteFileDO();
			fav.setActive(true);

			RepositoryDO repo = ServiceUtil.getRepoReference(em, cachedFile.getRepoId());

			if (repo == null) {
				throw new RepositoryNotFoundException(cachedFile.getRepoId());
			}
			fav.setRepository(repo);

			fav.setUser(new TenantUser(tenantId, userId));
			fav.setFileId(cachedFile.getFilePath());
			favList.add(fav);
		}
		return favList;
	}

}
