/**
 * 
 */
package com.nextlabs.rms.hibernate.dialect;

import java.sql.Types;

import org.hibernate.dialect.MySQL57InnoDBDialect;

/**
 * @author nnallagatla
 *
 */
public class MySQL57InnoDBDialectX extends MySQL57InnoDBDialect {

	/**
	 * 
	 */
	public MySQL57InnoDBDialectX() {
        registerColumnType(Types.NCLOB, "longtext" );
	}

}