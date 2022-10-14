package com.globa.search.engine.data;

import com.globa.search.engine.model.*;
import com.globa.search.engine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SiteDataService {
    //неиспользованные импорты и переменные необходимы при изменении
    // обхода страниц(в коде несколько реализаций), я их использовал
    // для тестирования скорости и памяти
    @Autowired
    private NativeQueryRepository em;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private SiteRepository siteRepository;


    public SiteDataService() {

    }

    public void addAllPage(List<Page> pages) {
        pageRepository.saveAll(pages);
    }

    public List<Site> finedAllSites() {
        return siteRepository.findAll();
    }

    public Long countPagesOnSite(Long idSite) {
        return pageRepository.findCountPagesByIdSite(idSite);
    }

    public Long countLemmasOnSite(Long idSite) {
        return lemmaRepository.countLemmaOnSite(idSite);
    }

    public Long countOfPage() {
        return pageRepository.count();
    }

    public Long countOfLemma() {
        return lemmaRepository.count();
    }

    public List<Long> finedAllIdPageByIdSite(long idSite) {
        return pageRepository.findByIdSite(idSite);
    }

    public Optional<Page> finedAllPageByIdPage(long idPage) {
        return pageRepository.findById(idPage);
    }

    public void add(Page page) {

        pageRepository.save(page);
    }

    public void addAll(List<Page> pages) {
        pageRepository.saveAll(pages);

    }

    public String finedContentFromPageByPath(String path) {
        List<Page> pages = pageRepository.findByPath(path);
        if (pages.isEmpty()) {
            return "";
        } else return pages.get(0).getContent();
    }

    //пробный метод, пока оставлю
    public Page finedPageByPath(String path) {
        List<Page> pages = pageRepository.findByPath(path);
        if (!pages.isEmpty()) {
            return pages.get(0);
        } else return null;
    }

    public long finedIdByPath(String path) {
        return pageRepository.findByPath(path).get(0).getId();
    }

    public long finedIdByTagName(String tagName) {
        return fieldRepository.findByTag(tagName).get(0).getId();
    }

    public Map<Long, Float> sortedPageMap(List<Long> listPage, Map<String, Integer> mapOfLemmas, Long idSite) {
        List<Long> lemmas = new ArrayList<>();
        Map<String, Float> buffer = new HashMap<>();
        for (Map.Entry<String, Integer> entry : mapOfLemmas.entrySet()) {
            lemmas.add(finedIdByLemmaName(entry.getKey(), idSite));
        }
        Map<Long, Float> bufferMap = new HashMap<>();
        for (Long idPage : listPage) {
            List<Index> indexList = indexRepository.finedIndexByIdLemmaAndListOfIdPages(idPage, lemmas);
            Double total = indexList.stream().mapToDouble(Index::getRank).sum();
            bufferMap.put(idPage, Float.valueOf(String.valueOf(total)));
        }
        for (Map.Entry<Long, Float> entry : bufferMap.entrySet()) {
            System.out.println("idPage = " + entry.getKey() + " rank = " + entry.getValue());
        }
        return bufferMap;
    }

    public void insertInField(String name, String selector, float weight) {
        fieldRepository.save(new Field(name, selector, weight));
    }

    public void deleteAllFromField() {
        fieldRepository.deleteAll();
    }

    public void setFrequency(Integer frequency, String lemmaName, long site_id) {

        lemmaRepository.setFrequencyLemma(frequency, lemmaName, site_id);

    }

    public void deleteAllFromLemma() {
        lemmaRepository.deleteAll();
    }

    public void deleteAllFromIndex() {
        indexRepository.deleteAll();
    }

    public void addLemma(Lemma lemma) {
        lemmaRepository.save(lemma);
    }

    public long finedIdByLemmaName(String lemmaName, long siteId) {
        try {
            return lemmaRepository.findByLemma(lemmaName, siteId).get(0).getId();
        } catch (Exception e) {
            return 0;
        }
    }

    public Page finedPage(String path, Long idSite) {
        return pageRepository.findPageByPathAndSiteId(path, idSite);
    }

    public Integer finedFrequencyByLemmaName(String lemmaName, Long idSite) {
        try {
            return lemmaRepository.findByLemma(lemmaName, idSite).get(0).getFrequency();
        } catch (Exception e) {
            return 0;
        }
    }

    public Site finedSiteByUrl(String url) {
        try {
            return siteRepository.findBySIteUrl(url).get(0);
        } catch (Exception e) {
            return null;
        }
    }

    public void addIndex(Index index) {
        indexRepository.save(index);
    }

    public void addSite(Status status, LocalDateTime status_time,
                        String last_error, String name, String url) {
        siteRepository.save(new Site(status, status_time, last_error, name, url));
    }

    public long getIdSite(Site site) {
        List<Site> sites = siteRepository.findBySIteUrl(site.getUrl());
        if (sites.size() > 0) {
            return sites.get(0).getId();
        } else return 0;
    }

    public void updateSite(Status status, LocalDateTime status_time, String last_error, String url) {
        siteRepository.updateSiteByUrl(status, status_time, last_error, url);
    }

    public List<Long> getIdAllSites() {
        List<Long> list = new ArrayList<>();
        for (Site site : siteRepository.findAll()) {
            list.add(site.getId());
        }
        return list;
    }

    public List<Long> listIdPages(Long idLemma) {
        return indexRepository.findIdPageByIdLemma(idLemma);
    }

    public Float getRankFromIndexByLemmaIdAndPageId(Long lemmaId, Long pageId) {
        if (indexRepository.finedRank(lemmaId, pageId) != null) {
            return indexRepository.finedRank(lemmaId, pageId).getRank();

        } else return 0f;
    }

    public List<Page> finedListPageByIdPageList(List<Long> idPageList) {
        return pageRepository.findPageByListId(idPageList);
    }

    public List<Map<String, Float>> finedResultList(String firstLemmaName, List<String> lemmas, Long idSite) {
        return indexRepository.resultList(firstLemmaName, lemmas, idSite);
    }

    private String nativeQueryForDeleteSite(String siteName) {
        String str = " DO $$  \n" +
                "                 DECLARE  \n" +
                "                 site_name varchar(255):='" + siteName + "';  \n" +
                "                 id_site bigint:=0;  \n" +
                "                 BEGIN  \n" +
                "                 id_site:=(select id from site where name=site_name );  \n" +
                "                 delete from index where page_id in (select id from page where site_id=id_site);  \n" +
                "                 delete from lemma where site_id=id_site;  \n" +
                "                 delete from site where id=id_site;\n" +
                "                 delete from page where site_id=id_site;  \n" +
                "                 END $$; ";
        str = str.replaceAll(":", "\\\\:");
        return str;
    }

    @Transactional
    public Long deleteALLFromDBWhereSiteNameAndGetSiteId(String nameSite, String pathSite) {
        em.executeQuery(nativeQueryForDeleteSite(nameSite));
        Site site = new Site(Status.INDEXING, LocalDateTime.now(), "no error", nameSite, pathSite + "/");
        addSite(Status.INDEXING, LocalDateTime.now(), "no error", nameSite, pathSite + "/");
        return getIdSite(site);
    }

    @Transactional
    public List<Long> getListIdPageByIdSite(Long idSite) {
        List<Long> lst = new ArrayList<>();
        var result = em.result("select id from page where site_id = " + idSite + ";");
        for (Object idPage : result) {
            System.out.println(idPage.toString());
        }
        if (!result.isEmpty()) {
            for (Object object : result) {
                lst.add(Long.valueOf(object.toString()));
            }
        }
        return lst;
    }

    @Transactional
    public String getContentByIdPageAndIdSite(Long idSite, Long idPage) {
        List resultList = em.result("select content from page where id=" +
                idPage + " and site_id=" + idSite + ";");
        return (resultList.size() > 0 && (resultList.get(0) != null)) ? resultList.get(0).toString() : "";
    }

    @Transactional
    public void insertLemmasInDB(String query) {
        em.executeQuery(query);
    }

    @Transactional
    public void setAfterIndexing(String error, Long idSite) {
        if (error.equals("")) {
            error = "no error";
        }
        siteRepository.updateSiteById("INDEXED", LocalDateTime.now(), error, idSite);
    }

    public Long addPage(Page page) {
        return pageRepository.saveAndFlush(page).getId();
    }
}
