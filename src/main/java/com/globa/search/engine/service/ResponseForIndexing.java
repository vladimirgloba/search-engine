package com.globa.search.engine.service;

import com.globa.search.engine.repository.NativeQueryRepository;
import com.globa.search.engine.service.response.ResponseError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResponseForIndexing {

    @Autowired
    private NativeQueryRepository em;

    @Transactional(readOnly = true)
    public boolean isIndexing() {
        String nativeSqlQuery =
                "select id from site where status='INDEXING';";
        List result = em.result(nativeSqlQuery);
        System.out.println("size = " + result.size());
        if (result.size() < 1) {
            return true;
        } else {
            ResponseError error = new ResponseError();
            error.getError();
            return false;
        }
    }
}
