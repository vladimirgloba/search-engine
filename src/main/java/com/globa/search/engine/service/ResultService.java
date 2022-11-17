package com.globa.search.engine.service;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Site;
import com.globa.search.engine.repository.NativeQueryRepository;
import com.globa.search.engine.service.response.ResponseForSearchQueryFirstLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Repository
@Transactional

public class ResultService {

    private static final Logger logger = LogManager.getLogger(ResultService.class);
    @Autowired
    private NativeQueryRepository nativeQueryRepository;

    private int totalSize = 0;


    public List<ResponseForSearchQueryFirstLevel> displayAllContactSummary(String firstLemma, Set<String> lemmasString, Site site, SiteDataService dataService, int iteration) {
        Long idSIte = site.getId();
        String lemmas = "";
        for (String str : lemmasString) {
            lemmas = lemmas + "'" + str + "', ";
        }
        lemmas = lemmas.substring(0, lemmas.length() - 2);

        logger.info(sqlString(firstLemma, site.getId()));
        List result = nativeQueryRepository.result(sqlString(firstLemma, idSIte));
        iteration=result.size()<50? result.size() : 50;
        List<ResponseForSearchQueryFirstLevel> responseForSearchQueryFirstLevels = new ArrayList<>();
        responseForSearchQueryFirstLevels = result.size() > 0 ? dataList(result, iteration, lemmasString, site, dataService) : new ArrayList<>();
        if (!responseForSearchQueryFirstLevels.isEmpty()) {
            Float maxValue = responseForSearchQueryFirstLevels.get(0).getRelevance();
            for (ResponseForSearchQueryFirstLevel d : responseForSearchQueryFirstLevels) {
                d.setRelevance(d.getRelevance() / maxValue);
            }
        }
        this.totalSize = result.size();
        return responseForSearchQueryFirstLevels;
    }

    private List<ResponseForSearchQueryFirstLevel> dataList(List result, int iteration, Set<String> lemmasString, Site site, SiteDataService dataService) {
        List<ResponseForSearchQueryFirstLevel> responseForSearchQueryFirstLevels = new ArrayList<>();
        for (int i = 0; i < iteration; i++) {
            Object[] values = (Object[]) result.get(i);
            ResponseForSearchQueryFirstLevel responseForSearchQueryFirstLevel = new ResponseForSearchQueryFirstLevel();
            Document document = Jsoup.parse(values[1].toString());
            String title = document.title();
            Elements snippetList = document.getElementsByTag("b");
            String snippet = "";
            if (snippetList.size() > 0) {
                for (Element element : snippetList) {
                    snippetFinder(lemmasString, element, snippet);
                }

                snippet = snippet.length() > 300 ? snippet.substring(0, 290) : snippet;

            }
            responseForSearchQueryFirstLevel.getData(dataService, site.getUrl(), site.getName(), values[0].toString(),
                    title, snippet, (Float) values[2]);

            responseForSearchQueryFirstLevels.add(responseForSearchQueryFirstLevel);
        }
        System.out.println("size ============================== " + responseForSearchQueryFirstLevels.size());
        for (ResponseForSearchQueryFirstLevel response : responseForSearchQueryFirstLevels) {
            System.out.println(response.getTitle() + " = " + response.getRelevance().toString());
        }
        return responseForSearchQueryFirstLevels;

    }

    private String snippetFinder(Set<String> lemmasList, Element element, String snippet) {
        for (String lemma : lemmasList) {
            snippet = (element.toString().contains(lemma)) ? snippet + element : snippet;
        }
        return snippet;
    }

    private String sqlString(String firstLemma, Long idSIte) {
        return
                "select path,content, sum_rank from page join \n" +
                        "(select page_id, sum(rank) as sum_rank from index where\n" +
                        " lemma_id=\n" +
                        "(select id from lemma where lemma='" + firstLemma + "' and site_id=" + idSIte + ")\n" +
                        "group by page_id ) as foo on page.id=foo.page_id order by sum_rank  desc";
    }

    public int getTotalSize() {
        return this.totalSize;
    }
}
