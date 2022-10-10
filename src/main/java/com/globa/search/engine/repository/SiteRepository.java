package com.globa.search.engine.repository;

import com.globa.search.engine.model.Site;
import com.globa.search.engine.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    @Query(value = "SELECT * FROM site t WHERE t.url = ?1", nativeQuery = true)
    List<Site> findBySIteUrl(String url);

    @Transactional
    @Modifying
    @Query(value = "update site  set status = ?1, status_time = ?2, last_error = ?3 where url = ?4",
            nativeQuery = true)
    void updateSiteByUrl(Status status, LocalDateTime status_time, String last_error, String url);

    @Transactional
    @Modifying
    @Query(value = "update site  set status = ?1, status_time = ?2, last_error = ?3 where id = ?4",
            nativeQuery = true)
    void updateSiteById(String status, LocalDateTime status_time, String last_error, Long idSite);

    ///////

}

