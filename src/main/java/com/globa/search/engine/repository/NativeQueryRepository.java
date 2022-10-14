package com.globa.search.engine.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@Transactional
public class NativeQueryRepository {
    @PersistenceContext
    private EntityManager em;

    public NativeQueryRepository() {
    }

    @Transactional
    public void executeQuery(String query) {
        em.createNativeQuery(query).executeUpdate();
    }

    @Transactional
    public List result(String query) {
        return em.createNativeQuery(query).getResultList();
    }
}
