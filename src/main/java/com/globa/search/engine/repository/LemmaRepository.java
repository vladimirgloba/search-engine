package com.globa.search.engine.repository;
import com.globa.search.engine.model.Lemma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Long> {

    @Query(value = "SELECT * FROM lemma l WHERE l.lemma = ?1 AND l.site_id=?2", nativeQuery = true)
    List<Lemma> findByLemma(String lemma,long siteId);
    @Transactional
    @Modifying
    @Query( value ="update lemma  set frequency = ?1 where lemma = ?2 and site_id=?3",
            nativeQuery = true)
    void setFrequencyLemma( Integer frequency, String lemma,Long site_id);
    @Query(value = "SELECT COUNT(*) FROM lemma l WHERE l.site_id = ?1", nativeQuery = true)
    Long countLemmaOnSite(long siteId);


}
