package com.nextlabs.rms.entity.security;

import javax.persistence.*;

import com.nextlabs.rms.entity.FieldConstants;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;


/**
 * @author nnallagatla
 *
 */
@Entity
@Table(name = "RMS_SECURITY", indexes = {
		@Index(columnList = "tenant_id, file_name", name = "idx_security_1", unique = true)})
@NamedQueries({
        @NamedQuery(name = "SecurityDO.findFileByNameAndTenant",
                query = "SELECT s FROM SecurityDO s WHERE s.fileName IN (:fileNames) AND s.tenantId = :tenantId")})
@SequenceGenerator(name = "security_gen", sequenceName = "rms_security_seq", allocationSize = 1)
public class SecurityDO implements Serializable {
	private static final long serialVersionUID = 3063266617448226254L;

		public SecurityDO() {
    }

    public SecurityDO(String tenantId, String fileName, byte[] fileContent) {
        this.tenantId = tenantId;
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "security_gen")
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "tenant_id", length = FieldConstants.TENANT_ID_LENGTH, nullable = false)
    private String tenantId;

    @Column(name = "file_name", length = 128, nullable = false)
    private String fileName;

    @Column(name = "file_content", nullable = false)
    @Lob
    private byte[] fileContent;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if ( !(other instanceof SecurityDO) ) return false;

        final SecurityDO security = (SecurityDO) other;

        if ( ! (Objects.equals(security.getTenantId(), getTenantId()) &&
                Objects.equals(security.getFileName(), getFileName()) &&
                Arrays.equals(security.getFileContent(), getFileContent()))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getFileName());
    }
}