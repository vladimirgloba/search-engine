package com.globa.search.engine.service;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
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
public class RunnableWithHardQuery implements Runnable {
    private static final Logger logger = LogManager.getLogger(ResultService.class);
    private final SiteDataService dataService;
    private final UserAgentProperties userAgentProperties;
    private String siteName = "";
    private String pathName = "";
    private String buffer = "";
    private List<Page> pageList = new ArrayList<>();
    private final HashSet<String> noRepeat = new HashSet<>();
    private Long idSite = 0l;
    private String error = "";

    public RunnableWithHardQuery(UserAgentProperties userAgentProperties, SiteDataService dataService, String siteName, String pathName) {
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
        System.out.println(idSite);
    }

    public void recursiveSiteCrawling() {
        int code = 0;
        String content = "";
        if (pageList.size() >= 100) {
            dataService.addAllPage(pageList);
            pageList = new ArrayList<>();
        }


        if (isValidUrl(buffer) && noRepeat.add(buffer)) {
            System.out.println("В список");
            URL url = null;
            try {
                url = new URL(buffer);
            } catch (MalformedURLException e) {
                logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");

            }
            System.out.println(buffer);
            System.out.println(url.getPath());
            Connection.Response response = null;

            try {
                response = Jsoup.connect(buffer).userAgent(userAgentProperties.getUserAgent())
                        .referrer("http://www.google.com")
                        .timeout(5000 * 3)
                        .execute();
                System.out.println(response);
            } catch (MalformedURLException e) {
                logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
                error = error + e.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                content = null;
            } catch (HttpStatusException e) {
                logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
                error = error + e.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                content = null;
            } catch (IOException e) {
                logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
                error = error + e.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                content = null;
            } catch (NullPointerException e) {
                error = error + e.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                content = null;
                logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
            } catch (Exception e) {
                logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
                error = error + e.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                content = null;
            }
            try {
                if (response != null) {
                    code = response.statusCode();
                }
            } catch (NullPointerException e) {
                error = error + e.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                code = 404;
                logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
            } catch (Exception e) {
                logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
                error = error + e.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                code = 404;
            }
            if (code != 200 && code != 0) {
                content = null;
                error = error = error + "код ответа = " + code + "  при открытии адреса - " + buffer + "\n";
            } else {
                if (code == 0) {
                    code = 404;
                    error = error = error + "код ответа = 404  при открытии адреса - " + buffer + "\n";
                } else {
                    try {
                        content = response.parse().toString();
                    } catch (UnsupportedMimeTypeException e) {
                        logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
                        content = null;
                        code = 404;
                        error = error = error + "ошибка при открытии контента страницы" + " код = 404 on path = " + buffer + "\n";
                    } catch (MalformedURLException e) {
                        logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
                        error = error + e.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                        content = null;
                    } catch (HttpStatusException e) {
                        logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
                        error = error + e.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                        content = null;
                    } catch (IOException e) {
                        logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
                        logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
                        error = error + e.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                        content = null;
                    } catch (NullPointerException e) {
                        error = error + e.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                        content = null;
                        logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
                    } catch (Exception e) {
                        logger.error((char) 27 + "[31mWarning! ошибка :\n" + e.getMessage() + (char) 27 + "[0m");
                        error = error + e.getMessage() + " код = " + code + " on path = " + buffer + "\n";
                        content = null;
                    }
                }
            }
            //

            pageList.add(new Page(url.getPath(), code, content, idSite));

            System.out.println("in pageList");
            if (content != null) {
                Document document = Jsoup.parse(content);
                System.out.println("парсинг для " + buffer);

                Elements linksOnPage = document.select("a[href]");

                for (Element element : linksOnPage) {
                    String chekBuffer = element.attr("abs:href");
                    boolean ff = chekBuffer.contains(siteName);
                    System.out.println(element.attr("abs:href") + " = " + siteName + " " + ff);
                    buffer = element.attr("abs:href");
                    if (noRepeat.size() == 5000) {

                        break;
                    }
                    if (buffer.contains(siteName) && !buffer.contains("#")) {

                        recursiveSiteCrawling();
                    }
                }
            }
        } else {
            System.out.println("не проходит по параметрам " + buffer + " = " + isValidUrl(buffer));
        }
    }

    List<Long> listPageId() {

        return dataService.getListIdPageByIdSite(idSite);
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

            } catch (IOException e) {
                e.printStackTrace();
            }

            return resultMap;

        } else
            return resultMap;
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


    private void insertLemma() {
        if (pageList.size() > 0) {
            dataService.addAllPage(pageList);
            System.out.println("Добавили остаток страниц = " + pageList.size());
        }
        String bodyOfQuery = "";
        int iterator = 1;
        for (Long idOfPage : listPageId()) {
            if (iterator > 20) {
                String nativeQuery = (getHeader() + bodyOfQuery + getFooter()).replaceAll(":=", "\\\\:=");
                dataService.insertLemmasInDB(nativeQuery);
                iterator = 1;
            }
            Map<String, Float> lemmas = getLemmaMap(idSite, idOfPage);
            if (lemmas.size() > 0) {
                bodyOfQuery = bodyOfQuery + getBody(idOfPage, lemmas);
                iterator++;
            }

        }
        if (iterator > 1) {
            String nativeQuery = (getHeader() + bodyOfQuery + getFooter()).replaceAll(":=", "\\\\:=");
            dataService.insertLemmasInDB(nativeQuery);
            iterator = 1;
        }
//dataService.setAfterIndexing(error,idSite);
    }

    @Override
    public void run() {
        getSiteId(this.siteName, this.pathName);
        recursiveSiteCrawling();
        insertLemma();
        dataService.setAfterIndexing(error, idSite);
    }
}
