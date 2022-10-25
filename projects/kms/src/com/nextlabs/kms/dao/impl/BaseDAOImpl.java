package com.nextlabs.kms.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nextlabs.kms.dao.BaseDAO;

public class BaseDAOImpl<T> implements BaseDAO<T>{
	@Autowired
	private SessionFactory sessionFactory;

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	public void save(T entity) {
		getSession().save(entity);
	}
	
	@Override
	public void update(T entity) {
		getSession().update(entity);
	}
}