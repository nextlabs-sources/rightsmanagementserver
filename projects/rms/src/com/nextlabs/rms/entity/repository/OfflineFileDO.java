/**
 * 
 */
package com.nextlabs.rms.entity.repository;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author nnallagatla
 *
 */

@Entity
@Table(name = "RMS_OFFLINE_FILE", indexes = {
		@Index(columnList = "tenant_id, user_id, updated_date", name = "idx_offline_file_1", unique = false) })
@NamedQueries({
		@NamedQuery(name = "OfflineFileDO.deleteByRepoId", query = "UPDATE OfflineFileDO o SET o.active = FALSE, updatedDate=:updatedDate WHERE o.repository =:repo AND o.active=TRUE"),
		@NamedQuery(name = "OfflineFileDO.updateOfflineFileByRepoTypeAndTenantId", 
		query = "UPDATE OfflineFileDO a SET a.active = FALSE, a.updatedDate=:updatedDate WHERE a.user.tenantId=:tenantId AND a.repository IN (SELECT r FROM RepositoryDO r WHERE r.repoType=:repoType AND r.active = TRUE AND r.createdBy.tenantId=:tenantId )"),
		@NamedQuery(name = "OfflineFileDO.updatePersonalOfflineFileByRepoTypeAndTenantId", 
		query = "UPDATE OfflineFileDO a SET a.active = FALSE, a.updatedDate=:updatedDate WHERE a.user.tenantId=:tenantId AND a.repository IN (SELECT r FROM RepositoryDO r WHERE r.repoType=:repoType AND r.active = TRUE AND r.createdBy.tenantId=:tenantId AND r.shared = FALSE)"),
        @NamedQuery(name = "OfflineFileDO.deleteInactiveOfflines",
                query = "DELETE FROM OfflineFileDO o WHERE o.active = FALSE AND o.updatedDate < :cutoffDate")})
@AssociationOverrides(value = {
		@AssociationOverride(name = "repository", joinColumns = @JoinColumn(name = "repo_id", nullable = false) , foreignKey = @ForeignKey(name = "fk_offline_1") ) })
@SequenceGenerator(name = "cache_file_gen", sequenceName = "rms_off_file_seq", allocationSize = 1)
public class OfflineFileDO extends CachedFileDO {
}
