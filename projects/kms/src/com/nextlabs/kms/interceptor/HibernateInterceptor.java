package com.nextlabs.kms.interceptor;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

public class HibernateInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = 6238327350767009896L;

	public boolean onFlushDirty(Object obj, Serializable id, Object[] state, Object[] prevState, String[] names,
			Type[] types) throws CallbackException {
		if (obj instanceof ICreatedDate || obj instanceof IUpdatedDate) {
			updateDate(obj, state, names);
			return true;
		} else {
			return false;
		}
	}

	public boolean onSave(Object obj, Serializable id, Object[] state, String[] names, Type[] types)
			throws CallbackException {
		if (obj instanceof ICreatedDate || obj instanceof IUpdatedDate) {
			updateDate(obj, state, names);
			return true;
		}
		return false;
	}

	private void updateDate(Object obj, Object[] state, String[] names) {
		int posLastUpdated = -1;
		int posCreated = -1;
		final Date now = new Date();
		for (int i = 0; i != names.length; i++) {
			if ("lastUpdatedDate".equals(names[i])) {
				posLastUpdated = i;
			}
			if ("createdDate".equals(names[i])) {
				posCreated = i;
			}
		}
		if (obj instanceof ICreatedDate) {
			ICreatedDate obj1 = (ICreatedDate) obj;
			if (obj1.getCreatedDate() == null) {
				obj1.setCreatedDate(now);
				if (posCreated != -1) {
					state[posCreated] = obj1.getCreatedDate();
				}
			}
		}
		if (obj instanceof IUpdatedDate) {
			IUpdatedDate obj1 = (IUpdatedDate) obj;
			obj1.setLastUpdatedDate(now);
			if (posLastUpdated != -1) {
				state[posLastUpdated] = now;
			}
		}
	}
}
