package com.nextlabs.kms.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.stereotype.Repository;

import com.nextlabs.kms.dao.TenantDAO;
import com.nextlabs.kms.entity.Tenant;

@Repository
public class TenantDAOImpl extends BaseDAOImpl<Tenant> implements TenantDAO {

	@Override
	public Tenant getTenant(String code) {
		String hql = "from " + Tenant.class.getName() + " t where t.code = :code";
		Session session = getSession();
		Query query = session.createQuery(hql);
		query.setString("code", code);
		Tenant tenant = (Tenant) query.uniqueResult();
		return tenant;
	}

	@Override
	public Tenant getTenantDetail(String code) {
		DetachedCriteria dc = DetachedCriteria.forClass(Tenant.class);
		dc.createCriteria("provider", "p");
		dc.createCriteria("p.attributes", "attr", JoinType.LEFT_OUTER_JOIN);
		dc.add(Restrictions.eq("code", code));
		Criteria criteria = dc.getExecutableCriteria(getSession());
		Tenant tenant = (Tenant) criteria.uniqueResult();
		return tenant;
	}
}
