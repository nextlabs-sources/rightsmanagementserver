package com.nextlabs.rms.sharedutil;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import com.nextlabs.kms.types.Error;
import com.nextlabs.rms.sharedutil.RestletUtil.IHTTPResponseHandler;

public class KMSWebSvcResponseHandler<T> implements IHTTPResponseHandler<T> {
	private Class<T> resultClass;
	private OperationResult operationResult;
	private static Logger logger = Logger.getLogger(KMSWebSvcResponseHandler.class);

	public KMSWebSvcResponseHandler(Class<T> resultClass, OperationResult operationResult) {
		this.resultClass = resultClass;
		this.operationResult = operationResult;
		operationResult.setMessage(null);
	}

	@Override
	public T handle(Representation representation, Status status) {
		return handleResponse(representation, status, resultClass);
	}

	private <Result> Result handleResponse(Representation representation, Status status, Class<Result> resultClass) {
		int code = status.getCode();
		if(code == 404) {	//response might not be coming from KMS
			logger.error("404: Not found.");
			operationResult.setResult(false);
			operationResult.setMessage("Could not connect to Key Management Server.");
		}
		else if (code >= 400) {
			handleErrorResponse(representation, status);
		} else if (code == 200) {
			return handleXMLResponse(representation, resultClass);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <Result> Result handleXMLResponse(Representation representation, Class<Result> responseClass) {
		StringWriter writer = new StringWriter();
		Result response = null;
		try {
			representation.write(writer);
			JAXBContext jaxbContext = JAXBContext.newInstance(responseClass);
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    ByteArrayInputStream bais = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
	    response = (Result) jaxbUnmarshaller.unmarshal(bais);
			bais.close();
			return response;
		} catch (final Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return response;
	}

	private void handleErrorResponse(Representation representation, Status status) {
		StringWriter writer = new StringWriter();
		try {
			representation.write(writer);
			JAXBContext jaxbContext = JAXBContext.newInstance(Error.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			ByteArrayInputStream bais = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
			Error errorResponse = (Error) jaxbUnmarshaller.unmarshal(bais);
			bais.close();
			logger.error(errorResponse.getCode()+": " + errorResponse.getDescription());
			operationResult.setResult(false);
			operationResult.setMessage(errorResponse.getDescription());
		} catch (final Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
}