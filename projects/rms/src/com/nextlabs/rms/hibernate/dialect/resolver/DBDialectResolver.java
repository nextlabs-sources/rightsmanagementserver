/**
 * 
 */
package com.nextlabs.rms.hibernate.dialect.resolver;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServer2005Dialect;
import org.hibernate.dialect.SQLServer2008Dialect;
import org.hibernate.dialect.SQLServer2012Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;

import com.nextlabs.rms.hibernate.dialect.MySQL57InnoDBDialectX;
import com.nextlabs.rms.hibernate.dialect.MySQL5InnoDBDialectX;

/**
 * @author nnallagatla
 *
 */
public class DBDialectResolver implements DialectResolver {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3960631724377898043L;

	@Override
	public Dialect resolveDialect(DialectResolutionInfo info) {
		final String databaseName = info.getDatabaseName();

		if ("MySQL".equals(databaseName)) {
			final int majorVersion = info.getDatabaseMajorVersion();
			final int minorVersion = info.getDatabaseMinorVersion();
			if (majorVersion >= 5) {
				if (minorVersion >= 7) {
					return new MySQL57InnoDBDialectX();
				}
				// innodb engine starting from mysql 5.1
				if (minorVersion >= 1) {
					return new MySQL5InnoDBDialectX();
				}
			}
		} else if (databaseName.startsWith("Microsoft SQL Server")) {
			final int majorVersion = info.getDatabaseMajorVersion();

			switch (majorVersion) {
			case 8:
				return new SQLServerDialect();
			case 9:
				return new SQLServer2005Dialect();
			case 10:
				return new SQLServer2008Dialect();
			case 11:
			case 12:
				return new SQLServer2012Dialect();
			}
		}

		return null;
	}
}
