package com.nextlabs.rms.command;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.json.ShowFileResponse;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.IRepository;
import com.nextlabs.rms.repository.RepositoryFactory;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.repository.exception.FileNotFoundException;
import com.nextlabs.rms.repository.exception.InvalidTokenException;
import com.nextlabs.rms.repository.exception.NonUniqueFileException;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.repository.exception.UnauthorizedRepositoryException;
import com.nextlabs.rms.util.CADAssemblyUtil;
import com.nextlabs.rms.util.Nvl;
import com.nextlabs.rms.util.RepositoryFileUtil;
import com.nextlabs.rms.util.RepositoryFileUtil.RepositoryFileParams;
import com.nextlabs.rms.wrapper.util.HTMLWrapperUtil;
import com.nextlabs.rms.util.StringUtils;
import com.nextlabs.rms.util.UtilMethods;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ShowFileCommand extends AbstractCommand {
	
	private static Logger logger = Logger.getLogger(ShowFileCommand.class);

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		RepositoryFileParams params = RepositoryFileUtil.getRepositoryFileQueryStringParams(request);
		
		long repoId = params.getRepoId();
		String filePath = params.getFilePath();
		
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		EvaluationHandler evalHandler = new EvaluationHandler();
		String sessionId=request.getSession().getId();		
		if(!RepositoryManager.getInstance().isRepoValid(userPrincipal, repoId)){
			logger.error("Unable to access repository '" + repoId + "' for user " + userPrincipal.getPrincipalName());
			String cacheId=EvaluationHandler.setError(userPrincipal, filePath, sessionId,RMSMessageHandler.getClientString("repoDeleted"));
			String redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME + "/ShowError.jsp?errId=" + cacheId;
			response.sendRedirect(redirectURL);
			return;
		}
		
		String userTempDir = (String)request.getSession().getAttribute(AuthFilter.USER_TEMP_DIR);
		String redirectURL="";

		String outputPath = RepositoryFileUtil.getTempOutputFolder(userTempDir);
		File fileFromRepo = null;
		try {
			fileFromRepo = RepositoryFileUtil.downloadFileFromRepo(userPrincipal, params, outputPath);
			if (fileFromRepo == null) {
				throw new FileNotFoundException(RMSMessageHandler.getClientString("fileProcessErr"));
			}
		} catch (Exception e) {
			logger.error("Error occurred when downloading the file: " + e.getMessage(), e);
			String error = null;
			if (e instanceof InvalidTokenException) {
				error = RMSMessageHandler.getClientString("invalidRepositoryToken");
			} else if (e instanceof UnauthorizedRepositoryException) {
				error = RMSMessageHandler.getClientString("unauthorizedRepositoryAccess");
			} else if (e instanceof FileNotFoundException){
				error = RMSMessageHandler.getClientString("fileDeletedFromRepo");
			} else {
				error = RMSMessageHandler.getClientString("fileProcessErr");
			}
			String cacheId=EvaluationHandler.setError(userPrincipal, filePath, sessionId, error);
			redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME + "/ShowError.jsp?errId=" + cacheId;
			response.sendRedirect(redirectURL);
			return;
		}
		if (!fileFromRepo.exists()) {
			logger.error("File " + filePath + " could not be obtained from the repository.");
		}
		if (StringUtils.endsWithIgnoreCase(fileFromRepo.getAbsolutePath(), ConfigManager.ZIP_EXTN)) {
			try{
				fileFromRepo = CADAssemblyUtil.getCADFileFromZIP(outputPath, fileFromRepo.getName());
			}
			catch(RMSException e){
				String cacheId = EvaluationHandler.setError(userPrincipal, fileFromRepo.getName(), sessionId,e.getMessage());
				redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME + "/ShowError.jsp?errId=" + cacheId;
				response.sendRedirect(redirectURL);
				return;
			}
		}
		ShowFileResponse result = new ShowFileResponse();
		try{
			redirectURL = evalHandler.validateFileRequest(userPrincipal, fileFromRepo, sessionId, UtilMethods.getIP(request), params);
		}catch (MissingDependenciesException missingException) {
			if (filePath.toLowerCase().endsWith(ConfigManager.ZIP_EXTN)) {
				redirectURL = getMissingDepRedirectUrl(missingException, userPrincipal, fileFromRepo.getName(), sessionId);
			}
			else {
				try {
					redirectURL = downloadFilesAndValidateFileRequest(missingException, filePath, outputPath, fileFromRepo,
							userPrincipal, sessionId, params, request, evalHandler);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					String error = null;
					if (e instanceof NonUniqueFileException) {
						NonUniqueFileException ex = (NonUniqueFileException) e;
						error = HTMLWrapperUtil.generateHTMLUnorderedList(RMSMessageHandler.getClientString("nonUniqueRepoFileErr"),
								new String[] { Nvl.nvl(ex.getFilename()) });
					} else if (e instanceof InvalidTokenException) {
						error = RMSMessageHandler.getClientString("invalidRepositoryToken");
					} else if (e instanceof UnauthorizedRepositoryException) {
						error = RMSMessageHandler.getClientString("unauthorizedRepositoryAccess");
					} else {
						error = RMSMessageHandler.getClientString("fileProcessErr");
					}
					String cacheId = EvaluationHandler.setError(userPrincipal, fileFromRepo.getName(), sessionId, error);
					redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME + "/ShowError.jsp?errId=" + cacheId;
					result.setViewerUrl(redirectURL);
					JsonUtil.writeJsonToResponse(result, response);
					return;
				}
			}
		}
		result.setViewerUrl(redirectURL);
		JsonUtil.writeJsonToResponse(result, response);
		return;
	}
		
	private String downloadFilesAndValidateFileRequest (MissingDependenciesException missingException, String filePath, String outputPath, File fileFromRepo, 
											RMSUserPrincipal userPrincipal, String sessionId, RepositoryFileParams params, HttpServletRequest request, EvaluationHandler evalHandler) throws IOException, RepositoryException, RMSException {
		String redirectURL = "";
		List<String> missingDependencies = missingException.getMissingDependencies();
		int index = filePath.lastIndexOf("/");
		String parentDir = index == -1 ? filePath : filePath.substring(0, index);
		outputPath += File.separator + FilenameUtils.getBaseName(FilenameUtils.getBaseName(fileFromRepo.getName()));
		File dir = new File(outputPath);
		if(dir.exists()) {
			FileUtils.deleteDirectory(dir);					
		}
		
		String fileExt = "." + FilenameUtils.getExtension(filePath).toLowerCase();
		if (fileExt.equals(".")) {
			fileExt = "." + FilenameUtils.getExtension(fileFromRepo.getAbsolutePath()).toLowerCase();
		}
		if(fileExt.equals(EvaluationHandler.NXL_FILE_EXTN)) {
			for (final ListIterator<String> i = missingDependencies.listIterator(); i.hasNext();) {
			  final String part = i.next();
			  i.set(part+EvaluationHandler.NXL_FILE_EXTN);
			} 
		}
		
		while(!missingDependencies.isEmpty()) {
			FileUtils.copyFileToDirectory(fileFromRepo, dir);		// assembly nxl file is always needed for evalRes
      IRepository repo = RepositoryFactory.getInstance().getRepository(userPrincipal, params.getRepoId());
			List<String> missingPartsFromRepo = downloadMissingFilesFromRepo(missingDependencies, repo, parentDir, outputPath);			
			try {
				if(!missingPartsFromRepo.isEmpty()){
					MissingDependenciesException mde = new MissingDependenciesException(missingPartsFromRepo);
					redirectURL = getMissingDepRedirectUrl(mde, userPrincipal, fileFromRepo.getName(), sessionId);
					break;
				}
				RepositoryFileParams partFileParams = new RepositoryFileParams(params.getRepoId(), filePath, params.getLastModifiedDate(), params.getDisplayPath());
				redirectURL = evalHandler.validateFileRequest(userPrincipal, dir, sessionId, UtilMethods.getIP(request), partFileParams);
			} catch (MissingDependenciesException missingEx) {
					missingDependencies.addAll(missingEx.getMissingDependencies());
			}
		}
		FileUtils.deleteDirectory(dir);
		return redirectURL;
	}
	
	private String getMissingDepRedirectUrl(MissingDependenciesException missingException, RMSUserPrincipal userPrincipal, String displayName, String sessionId){
		String errorMessage = HTMLWrapperUtil.generateHTMLUnorderedList(
				RMSMessageHandler.getClientString("missingDependenciesErr"), missingException.getMissingDependencies());
		logger.error(missingException.getMessage());
		String cacheId = EvaluationHandler.setError(userPrincipal, displayName, sessionId, errorMessage);
		return GlobalConfigManager.RMS_CONTEXT_NAME + "/ShowError.jsp?errId=" + cacheId;
	}
	
	private List<String> downloadMissingFilesFromRepo(List<String> missingDependencies, IRepository repo, String parentDir, String outputPath) throws RepositoryException, RMSException{
		List<String> missingPartsFromRepo = new ArrayList<>();
		String[] filenames = missingDependencies.toArray(new String[missingDependencies.size()]);
		try {
			repo.downloadFiles(parentDir, filenames, outputPath);
		} catch (MissingDependenciesException e) {
			missingPartsFromRepo.addAll(e.getMissingDependencies());
		}
		missingDependencies.clear();
		return missingPartsFromRepo;
	}
	
}
