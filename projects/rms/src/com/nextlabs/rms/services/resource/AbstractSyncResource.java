/**
 * 
 */
package com.nextlabs.rms.services.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.nextlabs.rms.exception.DuplicateRepositoryNameException;
import com.nextlabs.rms.exception.RepositoryAlreadyExists;
import com.nextlabs.rms.exception.RepositoryNotFoundException;
import com.nextlabs.rms.exception.UnauthorizedOperationException;
import com.nextlabs.rms.exception.UserNotFoundException;
import com.nextlabs.rms.repository.exception.FileNotFoundException;
import com.nextlabs.rms.rmc.ServiceUtil;
import com.nextlabs.rms.rmc.StatusTypeEnum;
import com.nextlabs.rms.rmc.types.StatusType;

/**
 * @author nnallagatla
 *
 */
public abstract class AbstractSyncResource extends ServerResource {

	static final Logger logger = Logger.getLogger(AbstractSyncResource.class);

	/**
	 * 
	 */
	public AbstractSyncResource() {
	}

	@Post
	public final Representation doPost(Representation entity) throws IOException {
		Representation response = null;
		try {
			response = handlePost(entity);
		} catch (XmlException e) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			response = getErrorResponse(StatusTypeEnum.BAD_REQUEST.getCode(), StatusTypeEnum.BAD_REQUEST.getDescription());
			logger.error("Error parsing request for " + getResourceName() + ":\n", e);
		} catch (UserNotFoundException e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			response = getErrorResponse(StatusTypeEnum.USER_NOT_FOUND.getCode(),
					StatusTypeEnum.USER_NOT_FOUND.getDescription());
			logger.error("User not found for " + getResourceName() + ". userId: " + e.getUserId() + "\n", e);
		} catch (RepositoryNotFoundException e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			response = getErrorResponse(StatusTypeEnum.REPO_NOT_FOUND.getCode(),
					StatusTypeEnum.REPO_NOT_FOUND.getDescription());
			logger.error("Repository not found for " + getResourceName() + ". RepoId: " + e.getRepoId() + "\n", e);
		}catch (RepositoryAlreadyExists e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			response = getErrorResponse(StatusTypeEnum.REPO_ALREADY_EXISTS.getCode(),
					StatusTypeEnum.REPO_ALREADY_EXISTS.getDescription());
			logger.error("Repository Already exists " + getResourceName() + "\n", e);
		}catch (DuplicateRepositoryNameException e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			response = getErrorResponse(StatusTypeEnum.DUPLICATE_REPO_NAME.getCode(),
					StatusTypeEnum.DUPLICATE_REPO_NAME.getDescription());
			logger.error("Duplicate Repository Name " + getResourceName() + "\n", e);
		}catch (Exception e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			response = getErrorResponse(StatusTypeEnum.UNKNOWN.getCode(), StatusTypeEnum.UNKNOWN.getDescription());
			logger.error("Error occurred when handling POST request for " + getResourceName() + ":\n", e);
		}
		return response;
	}

	/**
	 * 
	 * @param entity
	 * @return
	 * @throws XmlException
	 * @throws UserNotFoundException
	 * @throws RepositoryNotFoundException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws RepositoryAlreadyExists 
	 * @throws DuplicateRepositoryNameException 
	 */
	protected abstract Representation handlePost(Representation entity)
			throws XmlException, UserNotFoundException, RepositoryNotFoundException, IOException, FileNotFoundException, RepositoryAlreadyExists, 
			UnauthorizedOperationException, DuplicateRepositoryNameException;

	protected abstract Representation getErrorResponse(int errCode, String errMsg) throws IOException;

	protected String getResourceName() {
		return this.getClass().getName();
	}

	protected StatusType getStatus(int statusCode, String statusMsg) {
		return ServiceUtil.getStatus(statusCode, statusMsg);
	}

	protected StringRepresentation getStringRepresentation(XmlObject doc) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		doc.save(baos);
		return new StringRepresentation(baos.toString(), MediaType.TEXT_PLAIN);
	}

}
