package com.globa.search.engine.service;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
@Scope("prototype")
public class ForMultithreadedAddition implements Runnable {
    private static final Logger logger = LogManager.getLogger(ForMultithreadedAddition.class);
    private final SiteDataService dataService;
    private final UserAgentProperties userAgentProperties;
    private final HashSet<String> noRepeat = new HashSet<>();
    private final List<Page> pageList = new ArrayList<>();
    private String siteName = "";
    private String pathName = "";
    private String buffer = "";
    private Long idSite = 0l;
    private String error = "";

    public ForMultithreadedAddition(UserAgentProperties userAgentProperties, SiteDataService dataService, String siteName, String pathName) {
        this.dataService = dataService;
        this.userAgentProperties = userAgentProperties;
        this.siteName = siteName;
        this.pathName = pathName;
        this.buffer = pathName;
    }

    //проверка на валидность адреса
    private boolean isValidUrl(String url) {
        Pattern pattern = Pattern.compile("^(http(s)?://)?[a-z0-9-]+\\.(.[a-z0-9-]+)+(:[0-9]+)?(/.*)?$");
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    private void getSiteId(String nameSite, String pathSite) {
        this.idSite = dataService.deleteALLFromDBWhereSiteNameAndGetSiteId(nameSite, pathSite);
        logger.info("обработка сайта " + siteName);
    }

    public void recursiveSiteCrawling() {
        if (isValidUrl(buffer) && noRepeat.add(buffer)) {
            Page page = insertAndGetPage(buffer);
            insertLemma(idSite, page.getId());
            if (page.getContent() != null) {
                Document document = Jsoup.parse(page.getContent());
                logger.info("парсинг для " + buffer);
                Elements linksOnPage = document.select("a[href]");
                for (Element element : linksOnPage) {
                    buffer = element.attr("abs:href");
                    if (buffer.contains(siteName)) {
                        recursiveSiteCrawling();
                    }
                }
            }
        } else {
            logger.info("не проходит по параметрам");
        }
    }

    @Override
    public void run() {
        getSiteId(this.siteName, this.pathName);
        recursiveSiteCrawling();
        dataService.setAfterIndexing(error, idSite);
    }

    private Page insertAndGetPage(String buffer) {
        int code = 0;
        String content = "";
        URL url = null;
        try {
            url = new URL(buffer);
            logger.info("обработка страницы: " + url);
        } catch (MalformedURLException e) {
            logger.error((char) 27 + "[31mWarning! " + "ошибка при проверке URL:\n" + e.getMessage() + (char) 27 + "[0m");
        }
        Connection.Response response = null;
        try {
            response = Jsoup.connect(buffer).userAgent(userAgentProperties.getUserAgent()).referrer("http://www.google.com").timeout(5000).execute();
            code = response.statusCode();
        } catch (Exception exc) {
            if (exc instanceof MalformedURLException || exc instanceof HttpStatusException || exc instanceof IOException || exc instanceof NullPointerException || exc instanceof NullPointerException)
                content = null;
        }
        if (code != 200 || code == 0) {
            content = null;
            error = error = error + "код ответа = " + code + "  при открытии адреса - " + buffer + "\n";
        } else {
            try {
                content = response.parse().toString();
            } catch (Exception exc) {
                if (exc instanceof MalformedURLException || exc instanceof HttpStatusException || exc instanceof IOException || exc instanceof NullPointerException || exc instanceof NullPointerException)
                    logger.error((char) 27 + "[31mWarning! " + "ошибка :\n" + exc.getMessage() + (char) 27 + "[0m");
                error = error + exc.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                content = null;
            }
        }
        Page page = new Page(url.getPath(), code, content, idSite);
        dataService.addPage(page);
        return page;
    }

    private Map<String, Float> getLemmaMap(Long idSite, Long idPage) {
        Map<String, Float> resultMap = new HashMap<>();
        String content = dataService.getContentByIdPageAndIdSite(idSite, idPage);
        if (content.length() != 0) {
            Document document = Jsoup.parse(content);
            Map<String, Integer> titleMap = new HashMap<>();
            Map<String, Integer> bodyMap = new HashMap<>();
            try {
                titleMap = LemmaFinder.getInstance().collectLemmas(document.title());
                bodyMap = LemmaFinder.getInstance().collectLemmas(document.body().toString());
                setAbsolutRelevance(bodyMap, titleMap, resultMap);
            } catch (Exception e) {
                logger.error((char) 27 + "[31mWarning! " + "ошибка:\n" + e.getMessage() + (char) 27 + "[0m");
            }

        }
        return resultMap;
    }

    private void setAbsolutRelevance(Map<String, Integer> titleMap, Map<String, Integer> bodyMap, Map<String, Float> resultMap) {
        for (Map.Entry<String, Integer> entry : titleMap.entrySet()) {
            Float buffer = 0f;
            if (bodyMap.containsKey(entry.getKey())) {
                buffer = (float) (entry.getValue() + (bodyMap.get(entry.getKey()) * 0.8));
                resultMap.put(entry.getKey(), buffer);
                bodyMap.remove(entry.getKey());
            } else {
                resultMap.put(entry.getKey(), entry.getValue() * 1f);
            }
        }
        for (Map.Entry<String, Integer> entry : bodyMap.entrySet()) {
            resultMap.put(entry.getKey(), entry.getValue() * 0.8f);
        }
    }

    private String getHeader() {
        return "DO $$\n" +
                "DECLARE\n" +
                "id_site bigint:=0;\n" +
                "id_page bigint:=0;\n" +
                "iterator bigint:=1;\n" +
                "size_buffer bigint:=0;\n" +
                "BEGIN\n" +
                "DROP TABLE IF EXISTS buffer;\n" +
                "CREATE TEMP TABLE buffer (id bigint,frequency bigint,lemma_name varchar(255), lemma_rank float, id_page_in_buffer bigint,id_site_in_buffer bigint);\n";
    }

    private String getFooter() {
        return
                "END $$;";
    }

    private String getBody(Long idPage, Map<String, Float> lemmasMap) {
        String insertTable = "";
        int i = 1;
        for (Map.Entry<String, Float> entry : lemmasMap.entrySet()) {
            insertTable = insertTable + "(" + i + ", " + 1 + ", " + "'" + entry.getKey() + "'" + ", " + entry.getValue() + ", " + idPage + ", " + idSite + "),\n";
            i++;
        }
        insertTable = insertTable.substring(0, (insertTable.length() - 2));
        return
                "id_site:=" + this.idSite + ";\n" +
                        "id_page:=" + idPage + ";\n" +
                        "INSERT INTO buffer(id,frequency, lemma_name, lemma_rank,id_page_in_buffer,id_site_in_buffer) values\n" +
                        insertTable + ";\n" +
                        "UPDATE lemma SET frequency=(frequency+1) where lemma in (select lemma_name from buffer) and site_id=id_site;\n" +
                        "INSERT INTO lemma(lemma,frequency,site_id) \n" +
                        "SELECT lemma_name,frequency,id_site_in_buffer FROM buffer WHERE lemma_name NOT IN\n" +
                        "(SELECT lemma FROM lemma WHERE site_id=id_site) ;\n" +
                        "size_buffer:=(select count(*)from buffer);\n" +
                        "LOOP\n" +
                        "UPDATE buffer SET frequency=(SELECT id FROM lemma WHERE site_id=id_site AND lemma=(\n" +
                        "select lemma_name from buffer where id=iterator)) where id=iterator;\n" +
                        "iterator=iterator+1;\n" +
                        "EXIT WHEN iterator>size_buffer;\n" +
                        "END LOOP;\n" +
                        "iterator:=1;\n" +
                        "INSERT INTO index (lemma_id,rank,page_id)\n" +
                        "select frequency, lemma_rank, id_page_in_buffer from buffer;\n" +
                        "DELETE FROM buffer;\n";
    }


    private void insertLemma(Long idSite, Long idPage) {
        String bodyOfQuery = "";
        Map<String, Float> lemmas = getLemmaMap(idSite, idPage);
        if (lemmas.size() > 0) {
            bodyOfQuery = bodyOfQuery + getBody(idPage, lemmas);
            String nativeQuery = (getHeader() + bodyOfQuery + getFooter()).replaceAll(":=", "\\\\:=");
            dataService.insertLemmasInDB(nativeQuery);
        }
    }


}
