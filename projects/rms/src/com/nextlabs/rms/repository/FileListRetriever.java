package com.nextlabs.rms.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.repository.exception.InvalidTokenException;
import com.nextlabs.rms.repository.exception.RepositoryAccessException;
import com.nextlabs.rms.repository.exception.UnauthorizedRepositoryException;

public class FileListRetriever implements Callable<List<RepositoryContent>> {

	private RMSUserPrincipal userPrincipal = null;
	
	private long repoId;
	
	public FileListRetriever(RMSUserPrincipal userPrincipal, long repoId) {
		this.userPrincipal = userPrincipal;
		this.repoId = repoId;
	}
	
	@Override
	public List<RepositoryContent> call() throws Exception {
		IRepository repository = null;
		List<RepositoryContent> fileList=new ArrayList<RepositoryContent>();
		try{
			repository=RepositoryFactory.getInstance().getRepository(userPrincipal, repoId);
			fileList = repository.getFileList("/");			
		}catch(InvalidTokenException e){
			throw new InvalidTokenException(e.getMessage(), e, repository.getRepoName());
		}catch (UnauthorizedRepositoryException ue){
			throw new UnauthorizedRepositoryException(ue.getMessage(), ue, repository.getRepoName());
		}catch (Exception ex){
			throw new RepositoryAccessException(ex.getMessage(), ex, repository.getRepoName());
		}
		if(fileList.size()==0){
			return null;
		}
		return fileList;
	}

}
