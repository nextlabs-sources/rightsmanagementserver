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
@Table(name = "RMS_FAVORITE_FILE", indexes = {
		@Index(columnList = "tenant_id, user_id, updated_date", name = "idx_favorite_file_1", unique = false) })
@NamedQueries({
		@NamedQuery(name = "FavoriteFileDO.deleteByRepoId", query = "UPDATE FavoriteFileDO o SET o.active = FALSE, o.updatedDate=:updatedDate WHERE o.repository =:repo AND o.active=TRUE"),
		@NamedQuery(name = "FavoriteFileDO.updateFavoriteFileByRepoTypeAndTenantId", 
		query = "UPDATE FavoriteFileDO a SET a.active = FALSE, a.updatedDate=:updatedDate WHERE a.user.tenantId=:tenantId AND a.repository IN (SELECT r FROM RepositoryDO r WHERE r.repoType=:repoType AND r.active = TRUE AND r.createdBy.tenantId=:tenantId )"),
		@NamedQuery(name = "FavoriteFileDO.updatePersonalFavoriteFileByRepoTypeAndTenantId", 
		query = "UPDATE FavoriteFileDO a SET a.active = FALSE, a.updatedDate=:updatedDate WHERE a.user.tenantId=:tenantId AND a.repository IN (SELECT r FROM RepositoryDO r WHERE r.repoType=:repoType AND r.active = TRUE AND r.createdBy.tenantId=:tenantId AND r.shared = FALSE)"),
        @NamedQuery(name = "FavoriteFileDO.deleteInactiveFavorites",
                query = "DELETE FROM FavoriteFileDO o WHERE o.active = FALSE AND o.updatedDate < :cutoffDate")})
@AssociationOverrides(value = {
		@AssociationOverride(name = "repository", joinColumns = @JoinColumn(name = "repo_id", nullable = false) , foreignKey = @ForeignKey(name = "fk_favorite_1") ) })
@SequenceGenerator(name = "cache_file_gen", sequenceName = "rms_fav_file_seq", allocationSize = 1)
public class FavoriteFileDO extends CachedFileDO {
}
