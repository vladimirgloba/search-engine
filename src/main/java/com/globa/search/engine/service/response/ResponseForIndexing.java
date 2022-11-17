package com.globa.search.engine.service.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Component
public class ResponseForIndexing {

    @Autowired
    NoError noError;

    @Autowired
    ResponseError error;

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public boolean isIndexing(){
        String nativeSqlQuery=
                "select id from site where status='INDEXING';";
        List result = em.createNativeQuery(nativeSqlQuery).getResultList();
        System.out.println("size = "+result.size());
       if(result.size()<1){
           return true;
       }else

           error.getError();
        return false;
    }
}
