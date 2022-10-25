package com.nextlabs.rms.teamcenter;

import org.apache.log4j.Logger;

import com.teamcenter.schemas.soa._2006_03.exceptions.ConnectionException;
import com.teamcenter.schemas.soa._2006_03.exceptions.InternalServerException;
import com.teamcenter.schemas.soa._2006_03.exceptions.ProtocolException;
import com.teamcenter.soa.client.ExceptionHandler;
import com.teamcenter.soa.exceptions.CanceledOperationException;

/**
 * Implementation of the ExceptionHandler. For ConnectionExceptions (server
 * temporarily down .etc) prompts the user to retry the last request. For other
 * exceptions convert to a RunTime exception.
 */
public class TCExceptionHandler implements ExceptionHandler {
	
	private static Logger logger = Logger.getLogger(TCExceptionHandler.class);

	public void handleException(InternalServerException ise) {
		if (ise instanceof ConnectionException) {
			// ConnectionException are typically due to a network error (server
			// down .etc) and can be recovered from (the last request can be
			// sent again,
			// after the problem is corrected).
			logger.error("The server returned an connection error: " + ise.getMessage());
		} else if (ise instanceof ProtocolException) {
			// ProtocolException are typically due to programming errors
			// (content of HTTP
			// request is incorrect). These are generally can not be
			// recovered from.
			logger.error("The server returned an protocol error: " + ise.getMessage());
		} else {
			logger.error("The server returned an internal server error: " + ise.getMessage());
		}

		throw new RuntimeException(ise.getMessage());
	}

	public void handleException(CanceledOperationException coe) {
		logger.error("The server returned an canceled operation error: " + coe.getMessage());
		
		throw new RuntimeException(coe);
	}

}
