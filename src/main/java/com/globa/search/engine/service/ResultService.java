package com.globa.search.engine.service;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Site;
import com.globa.search.engine.service.response.ResponseForSearchQueryFirstLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Service
@Repository
@Transactional

public class ResultService {

    private static final Logger logger = LogManager.getLogger(ResultService.class);
    @PersistenceContext
    private EntityManager em;
    private int totalSize = 0;

    @Transactional(readOnly = true)
    public List<ResponseForSearchQueryFirstLevel> displayAllContactSummary(String firstLemma, List<String> lemmasString, Site site, SiteDataService dataService, int iteration) {
        Long idSIte = site.getId();
        String lemmas = "";
        for (String str : lemmasString) {
            lemmas = lemmas + "'" + str + "', ";
        }

        lemmas = lemmas.substring(0, lemmas.length() - 2);
        String sqlString =
                new StringBuilder().append("select path, content, sum_rank from ")
                        .append("(select page_id, sum(rank)")
                        .append(" as sum_rank from Index where page_id in")
                        .append("(select page_id from index  where lemma_id in ")
                        .append("(select id from lemma where lemma ='")
                        .append(firstLemma).append("' ")
                        .append("and site_id=")
                        .append(idSIte)
                        .append(") group by page_id) and lemma_id in")
                        .append("(select id from lemma where lemma in (")
                        .append(lemmas).append(")")
                        .append(" and site_id=")
                        .append(idSIte)
                        .append(") group by page_id order by sum_rank desc)")
                        .append(" as foo LEFT OUTER JOIN page ON page_id =page.id")
                        .toString();
        logger.info(sqlString);

        List result = em.createNativeQuery(sqlString).getResultList();
        if (result.size() < iteration) {
            iteration = result.size();
        }

        List<ResponseForSearchQueryFirstLevel> dataList = new ArrayList<>();
        for (int i = 0; (i < result.size() || i < iteration); i++) {

            Object[] values = (Object[]) result.get(i);
            ResponseForSearchQueryFirstLevel data = new ResponseForSearchQueryFirstLevel();
            Document document = Jsoup.parse(values[1].toString());
            String title = document.title();
            Elements snippetList = document.getElementsByTag("b");
            String snippet = "";

            if (snippetList.size() > 0) {
                for (Element element : snippetList) {
                    boolean flag = false;
                    for (String lemma : lemmasString) {
                        if (element.toString().contains(lemma)) {
                            flag = true;
                        }
                    }
                    if (flag) {
                        snippet = snippet + element;
                    }
                }
            }

            data.getData(dataService, site.getUrl(), site.getName(), values[0].toString(),
                    title, snippet, (Float) values[2]);

            dataList.add(data);


        }
        if (dataList.size() > 0) {
            Float maxValue = dataList.get(0).getRelevance();
            for (ResponseForSearchQueryFirstLevel d : dataList) {
                d.setRelevance(d.getRelevance() / maxValue);
            }
        }
        this.totalSize = result.size();
        return dataList;
    }

    public int getTotalSize() {
        return this.totalSize;
    }
}
