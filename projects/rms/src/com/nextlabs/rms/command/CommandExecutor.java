package com.nextlabs.rms.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class CommandExecutor {

	private static final Logger logger = Logger.getLogger(CommandExecutor.class);
	
	public static void executeCommand(HttpServletRequest request, HttpServletResponse response){
		AbstractCommand cmd = AbstractCommand.createCommand(request);
		try {
			cmd.doAction(request, response);
		} catch (Exception e) {
			logger.error("Error occurred while executing command - " + (cmd != null ? cmd.getClass().getName() : "<unknown>"), e);
			return;
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Command: " + (cmd != null ? cmd.getClass().getName() : "<unknown>") + " executed, returning..");
		}
	}
}
