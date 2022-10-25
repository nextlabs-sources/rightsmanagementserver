/**
 * 
 */
package com.nextlabs.rms.rmc;

/**
 * @author nnallagatla
 *
 */
public enum StatusTypeEnum {
	SUCCESS(0, "status_success"),
	UNKNOWN(500, "status_error_generic"),
	USER_NOT_FOUND(1, "status_error_user_not_found"),
	REPO_NOT_FOUND(2, "status_error_repo_not_found"),
	FAVORITE_FILE_NOT_FOUND(3, "status_error_favorite_file_not_found"),
	OFFLINE_FILE_NOT_FOUND(4, "status_error_offline_file_not_found"),
	BAD_REQUEST(5, "status_error_bad_request"),
	REPO_ALREADY_EXISTS(6, "status_error_user_repo_already_exists"),
	USER_ATTR_NOT_FOUND(7, "status_error_user_attr_not_found"),
	DUPLICATE_REPO_NAME(7, "status_error_duplicate_repo_name");
	
	private String description;
	private int code;

	private StatusTypeEnum(int code, String description) {
		this.code = code;
		this.description = description;
	}

	public int getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}