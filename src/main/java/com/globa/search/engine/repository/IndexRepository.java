package com.globa.search.engine.repository;

import com.globa.search.engine.model.Index;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface IndexRepository extends JpaRepository<Index, Long> {

    @Query(value = "SELECT t.page_id FROM index t WHERE t.lemma_id = ?1 ", nativeQuery = true)
    List<Long> findIdPageByIdLemma(Long idLemma);

    @Query(value = "SELECT * FROM index  WHERE lemma_id = ?1 AND page_id= ?2", nativeQuery = true)
    Index finedRank(Long idLemma, Long pageId);

    @Query(value = "SELECT * FROM index  WHERE  page_id = ?1 AND lemma_id IN ?2", nativeQuery = true)
    List<Index> finedIndexByIdLemmaAndListOfIdPages(Long idPage, List<Long> idLemmas);

    //конкатенация строк нужна для удобства просмотра кода при отображении в консоли
    @Transactional
    @Modifying
    @Query(value =
            "select path, sum_rank\n" +
                    "from\n" +
                    "(select page_id, sum(rank) as sum_rank \n" +
                    "from Index where page_id in(\n" +
                    "select page_id from index \n" +
                    "where lemma_id in\n" +
                    "(select id from lemma where lemma =?1\n" +
                    " and site_id=?3\n" +
                    ")group by page_id)\n" +
                    "and lemma_id in(\n" +
                    "    select id from lemma \n" +
                    "where lemma in ?2\n" +
                    " and site_id=?3\n" +
                    ")group by page_id\n" +
                    "order by sum_rank desc) as foo\n" +
                    "LEFT OUTER JOIN page\n" +
                    "ON page_id = page.id", nativeQuery = true)
    List<Map<String, Float>> resultList(String firstLemmaName, List<String> lemmas, Long idSite);
}



