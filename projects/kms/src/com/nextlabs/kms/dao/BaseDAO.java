package com.nextlabs.kms.dao;

public interface BaseDAO <T> {
	void save(T entity);
	void update(T entity);
}
