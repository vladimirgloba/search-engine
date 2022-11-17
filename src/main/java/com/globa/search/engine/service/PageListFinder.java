package com.globa.search.engine.service;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Site;
import com.globa.search.engine.service.response.ResponseForSearchQueryFirstLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PageListFinder {
    private static final Logger logger = LogManager.getLogger(PageListFinder.class);
    @Autowired
    private SiteDataService dataService;
    @Autowired
    private ResultService resultService;

    private int totalSize;

    public PageListFinder() {
    }


    private Map<String, Integer> sortedLemmasMap(String searchQuery, Long idSite) throws IOException {
        Map<String, Integer> buffer = LemmaFinder.getInstance().collectLemmas(searchQuery);
        if (!buffer.isEmpty()) {
            for (Map.Entry<String, Integer> entry : buffer.entrySet()) {
                entry.setValue(dataService.finedFrequencyByLemmaName(entry.getKey(), idSite));
            }
            Map<String, Integer> sortedMap = buffer.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors
                            .toMap(Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (e1, e2) -> e1,
                                    LinkedHashMap::new));
            for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
            return sortedMap;
        } else return new HashMap<>();
    }

    Map.Entry<String, Integer> lemmaWithMinRank(String searchQuery, Long idSite) {
        Map<String, Integer> buffer = null;
        try {
            buffer = sortedLemmasMap(searchQuery, idSite);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert buffer != null;
        if (!buffer.isEmpty()) {
            for (Map.Entry<String, Integer> entry : buffer.entrySet()) {
                if (entry.getValue() == 0) {
                    buffer.remove(entry.getKey());
                }
            }
        }
        return (!buffer.isEmpty()) ? buffer.entrySet()
                .stream()
                .findFirst()
                .get() : null;
    }


    public List<ResponseForSearchQueryFirstLevel> sortedPagesMapWithSQL(String searchQuery, String sitePath) {
        List<ResponseForSearchQueryFirstLevel> searchList = new ArrayList<>();
        Site site = dataService.finedSiteByUrl(sitePath);
        Long idSite = site.getId();
        Map<String, Integer> buffer = new HashMap<>();
        try {
            buffer = LemmaFinder.getInstance().collectLemmas(searchQuery);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<String> searchQueryList = buffer.keySet();

        searchList = lemmaWithMinRank(searchQuery, idSite) != null ? resultService.displayAllContactSummary(lemmaWithMinRank(searchQuery, idSite).getKey(),
                searchQueryList, site, dataService, 20) : new ArrayList<>();
        this.totalSize = resultService.getTotalSize();
        return searchList;
    }

    public int getTotalSize() {
        return this.totalSize;
    }
}
