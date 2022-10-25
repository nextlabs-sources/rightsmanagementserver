package com.nextlabs.kms.dao.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.nextlabs.kms.dao.KeyRingDAO;
import com.nextlabs.kms.entity.KeyRing;
import com.nextlabs.kms.entity.Tenant;
import com.nextlabs.kms.entity.enums.Status;

@Repository
public class KeyRingDAOImpl extends BaseDAOImpl<KeyRing> implements KeyRingDAO {

	@Override
	public void deleteKeyRing(Tenant tenant, String keyRingName) {
		Session session = getSession();
		String hql = "update " + KeyRing.class.getName()
				+ " set deleted = true, lastUpdatedDate = :currentDate where tenant = :tenant and name = :name and deleted = false";
		Query query = session.createQuery(hql);
		query.setParameter("tenant", tenant);
		query.setDate("currentDate", new Date());
		query.setString("name", keyRingName);
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public KeyRing getNonDeletedKeyRing(Tenant tenant, String keyRingName) {
		String hql = "from " + KeyRing.class.getName()
				+ " k where k.tenant = :tenant and k.name = :name and k.deleted = false";
		Session session = getSession();
		Query query = session.createQuery(hql);
		query.setParameter("tenant", tenant);
		query.setString("name", keyRingName);
		List<KeyRing> list = query.list();
		if (!CollectionUtils.isEmpty(list)) {
			assert list.size() <= 1;
			return list.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getNonDeletedKeyRings(Tenant tenant) {
		String hql = "select k.name from " + KeyRing.class.getName() + " k where k.tenant = :tenant and k.deleted = false";
		Session session = getSession();
		Query query = session.createQuery(hql);
		query.setParameter("tenant", tenant);
		List<String> list = query.list();
		Set<String> results = new HashSet<>(!CollectionUtils.isEmpty(list) ? list : Collections.<String> emptyList());
		return results;
	}

	@Override
	public Date getLatestModifiedDate(Tenant tenant) {
		String hql = "select max(k.lastUpdatedDate) from " + KeyRing.class.getName() + " k where k.tenant = :tenant";
		Session session = getSession();
		Query query = session.createQuery(hql);
		query.setParameter("tenant", tenant);
		Date date = (Date) query.uniqueResult();
		if (date == null) {
			date = new Date(0);
		}
		return date;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KeyRing> getActiveKeyRings(Tenant tenant) {
		String hql = "from " + KeyRing.class.getName()
				+ " k where k.tenant = :tenant and k.deleted = false and k.status = :status";
		Session session = getSession();
		Query query = session.createQuery(hql);
		query.setParameter("tenant", tenant);
		query.setParameter("status", Status.ACTIVE);
		List<KeyRing> results = query.list();
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public KeyRing getActiveKeyRing(Tenant tenant, String keyRingName) {
		String hql = "from " + KeyRing.class.getName()
				+ " k where k.tenant = :tenant and k.name = :name and k.deleted = false and k.status = :status";
		Session session = getSession();
		Query query = session.createQuery(hql);
		query.setParameter("tenant", tenant);
		query.setParameter("status", Status.ACTIVE);
		query.setString("name", keyRingName);
		List<KeyRing> list = query.list();
		if (!CollectionUtils.isEmpty(list)) {
			assert list.size() <= 1;
			return list.get(0);
		}
		return null;
	}
}
