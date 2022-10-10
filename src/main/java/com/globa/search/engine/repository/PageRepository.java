package com.globa.search.engine.repository;

import com.globa.search.engine.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    @Query(value = "SELECT * FROM page t WHERE t.path = ?1", nativeQuery = true)
    List<Page> findByPath(String path);

    @Query(value = "SELECT * FROM page t WHERE t.path = ?1 AND site_id=?2", nativeQuery = true)
    Page findPageByPathAndSiteId(String path, Long idSite);

    @Query(value = "SELECT id FROM page t WHERE site_id = ?1", nativeQuery = true)
    List<Long> findByIdSite(long siteId);

    @Query(value = "SELECT COUNT(*) FROM page t WHERE site_id = ?1", nativeQuery = true)
    Long findCountPagesByIdSite(Long siteId);

    @Query(value = "SELECT * FROM page t WHERE id IN ?1", nativeQuery = true)
    List<Page> findPageByListId(List<Long> idPageList);

}
