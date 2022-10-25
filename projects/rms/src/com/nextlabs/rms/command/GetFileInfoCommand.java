package com.nextlabs.rms.command;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.FileInfo;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.util.RepositoryFileUtil;

public class GetFileInfoCommand extends  AbstractCommand {

	private static Logger logger = Logger.getLogger(GetFileInfoCommand.class);

	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		File fileFromRepo ;
		try{
			fileFromRepo = RepositoryFileUtil.getFileFromRepo(request,userPrincipal);
		}catch(RMSException e){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		EvaluationHandler evalHandler = new EvaluationHandler();
		FileInfo fileInfo = evalHandler.getFileInfo(fileFromRepo);

		Map<String, List<String>> tags = fileInfo.getTagMap();
		ArrayList<String> localeTags = new ArrayList<String>();
		for (Entry<String, List<String>> entry : tags.entrySet())
		{
			List<String> values=entry.getValue();
			for (String value : values){
				localeTags.add(entry.getKey()+"-"+value);    
			}
		}

		Map<String,ArrayList<String>> tagsMap = new HashMap<String,ArrayList<String>>();
		tagsMap.put("tags", localeTags);

		JsonUtil.writeJsonToResponse(tagsMap, response);

	}
}
