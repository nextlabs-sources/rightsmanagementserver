/**
 * 
 */
package com.nextlabs.rms.hibernate.dialect;

import java.sql.Types;

import org.hibernate.dialect.MySQL5InnoDBDialect;

/**
 * @author nnallagatla
 *
 */
public class MySQL5InnoDBDialectX extends MySQL5InnoDBDialect {

	/**
	 * 
	 */
	public MySQL5InnoDBDialectX() {
        registerColumnType(Types.NCLOB, "longtext" );
	}

}