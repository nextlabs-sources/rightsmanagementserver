package com.nextlabs.rms.command;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.eval.EvalResponse;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.FileDetails;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.util.RepositoryFileUtil;
import com.nextlabs.rms.util.StringUtils;
import com.nextlabs.rms.util.UtilMethods;

public class GetFileDetailsCommand  extends  AbstractCommand {

	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		FileDetails fileDetails=new FileDetails();
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		
		File fileFromRepo ;
		try{
			fileFromRepo = RepositoryFileUtil.getFileFromRepo(request,userPrincipal);
		}catch(RMSException e){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		String sessionId=request.getSession().getId();	
		EvaluationHandler evalHandler = new EvaluationHandler();
		Map<String,String[]> rightsMap = new HashMap<String,String[]>();
		Map<String,List<String>> tagsMap = new HashMap<>();
		EvalResponse evalResponse;
		 
		if(StringUtils.endsWithIgnoreCase(fileFromRepo.getName(), EvaluationHandler.NXL_FILE_EXTN)){	
			evalResponse = evalHandler.evaluate(fileFromRepo, userPrincipal, sessionId, UtilMethods.getIP(request)).getEvalResponse();
			tagsMap = evalResponse.getTagMap();
			fileDetails.setTagsMap(tagsMap);	
		}
		else{
			evalResponse=evalHandler.getAllRights();
			fileDetails.setTagsMap(tagsMap);	
		}
		String[] rights = evalResponse.getRights().getRights();
		String[] localeRights = new String[rights == null? 0 : rights.length];
		for (int i= 0; i < localeRights.length; i++){
			localeRights[i] = RMSMessageHandler.getClientString(rights[i]);
		}
		rightsMap.put("rights", localeRights);
		fileDetails.setRightsMap(rightsMap);
		JsonUtil.writeJsonToResponse(fileDetails, response);
		
	}

}
