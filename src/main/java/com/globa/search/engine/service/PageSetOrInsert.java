package com.globa.search.engine.service;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Page;
import com.globa.search.engine.model.Site;
import com.globa.search.engine.repository.NativeQueryRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PageSetOrInsert {
    private static final Logger logger = LogManager.getLogger(PageSetOrInsert.class);

    @Autowired
    SiteDataService dataService;

    @Autowired
    PageParameters pageParameters;

    @Autowired
    NativeQueryRepository em;


    @Transactional
    public boolean nativeQueryForPage(String uri) {
        if (pageParameters(uri)) {
            logger.info("сформирован SQL-запрос:\n" + getSQLQuery().get(0));
            em.executeQuery(getSQLQuery().get(0));
            Site site = dataService.finedSiteByUrl(pageParameters.getSitePath());
            Long idSite = dataService.getIdSite(site);
            dataService.add(new Page(pageParameters.getPath(), 200,
                    pageParameters.getContent(),
                    idSite));
            logger.info("сформирован SQL-запрос:\n" + getSQLQuery().get(1));
            em.executeQuery(getSQLQuery().get(1));
            logger.info(" процесс добавления завершён ");
            System.out.println(" процесс добавления завершён ");
            return true;
        } else
            return false;
    }

    private boolean pageParameters(String uri) {
        List<String> allPath = getPath(uri);
        String content = "";
        Map<String, Integer> frequencyMap = new HashMap<>();
        Map<String, Float> relevanceMap = new HashMap<>();
        Connection connection = Jsoup.connect(allPath.get(0));
        try {
            Document document = connection.get();
            content = document.toString();
            frequencyMap = LemmaFinder.getInstance().collectLemmas(content);
            Map<String, Integer> relevanceMapFromTitle = LemmaFinder.getInstance().collectLemmas(document.title());
            Map<String, Integer> relevanceMapFromBOdy = LemmaFinder.getInstance().collectLemmas(document.tagName("body").toString());
            for (Map.Entry<String, Integer> entry : relevanceMapFromTitle.entrySet()) {
                if (relevanceMap.containsKey(entry.getKey())) {
                    relevanceMap.put(entry.getKey(), relevanceMap.get(entry.getKey()) + entry.getValue());
                } else {
                    relevanceMap.put(entry.getKey(), Float.valueOf(entry.getValue()));
                }
            }
            for (Map.Entry<String, Integer> entry : relevanceMapFromBOdy.entrySet()) {
                if (relevanceMap.containsKey(entry.getKey())) {
                    relevanceMap.put(entry.getKey(), relevanceMap.get(entry.getKey()) + (entry.getValue() * 0.8F));
                } else {
                    relevanceMap.put(entry.getKey(), Float.valueOf(entry.getValue() * 0.8F));
                }
            }
            pageParameters.setSitePath(allPath.get(1));
            pageParameters.setContent(content);
            pageParameters.setPath(allPath.get(2));

            pageParameters.setRelevanceMap(relevanceMap);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return pageParameters != null && !pageParameters.getContent().isEmpty() &&
                !pageParameters.getPath().isEmpty() && !pageParameters.getSitePath().isEmpty()
                && !pageParameters.getRelevanceMap().isEmpty();
    }


    private List<String> getSQLQuery() {
        Map<String, Float> relevanceMap = pageParameters.getRelevanceMap();
        Map<String, Integer> oldLemmasMap = getOldLemmasMap();
        String oldLemmasInTable = getOldLemmasInTable(oldLemmasMap);
        oldLemmasInTable = oldLemmasInTable.length() > 0?oldLemmasInTable.substring(0, oldLemmasInTable.length() - 2) + "\n":
                    "(" + 1 + ", '" + 'a' + "')";
        String relevanceString = getRelevanceString(relevanceMap);
        relevanceString = relevanceString.substring(0, relevanceString.length() - 2) + "\n";
        List<String> stringList = new ArrayList<>();
        stringList.add(nativeQueryFirst( oldLemmasMap,oldLemmasInTable).replaceAll(":=", "\\\\:="));
        stringList.add(nativeQuerySecond(relevanceString).replaceAll(":=", "\\\\:="));
        return stringList;
    }
    private String  getRelevanceString(Map<String, Float> relevanceMap){
        Integer iterator = 1;
        String relevanceString = "";
        for (Map.Entry<String, Float> entry : relevanceMap.entrySet()) {
            relevanceString = relevanceString + "(" + iterator + ", " +
                    "'" + entry.getKey() + "', " +
                    entry.getValue() + ")," + "\n";
            iterator++;
        }
        return relevanceString;
    }

    private String getOldLemmasInTable(Map<String, Integer> oldLemmasMap){
        Integer iterator = 1;
        String oldLemmasInTable = "";
        for (Map.Entry<String, Integer> entry : oldLemmasMap.entrySet()) {
            oldLemmasInTable = oldLemmasInTable + "(" + iterator + ", '" + entry.getKey() + "'),\n";
            iterator++;
        }
        return oldLemmasInTable;
    }

    private Map<String, Integer>getOldLemmasMap(){
        String oldContent = dataService.finedContentFromPageByPath(pageParameters.getPath());
        try {
          return   oldContent.length() > 0? LemmaFinder.getInstance().collectLemmas(oldContent):new HashMap<>();
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

   private String nativeQuerySecond (String relevanceString) {
     return    new StringBuilder().append("DO $$\n")
                .append("DECLARE\n")
                .append("site_path varchar(255):= '")
                .append(pageParameters.getSitePath()).append("';\n")
                .append("page_path text:='")
                .append(pageParameters.getPath()).append("'; \n")
                .append("id_site bigint:= 0;\n")
                .append("id_page bigint:=0; \n")
                .append("iterator bigint:=1; \n")
                .append("count_lines bigint:=0;\n")
                .append("lemma_in_table varchar(255) :=''; \n")
                .append("id_lemma_in_lemma bigint:=0; \n")
                .append("id_lemma_in_table bigint:=0;\n")
                .append("\n")
                .append("BEGIN\n")
                .append("id_site:=(select id from site where url=site_path);\n")
                .append("DROP TABLE IF EXISTS buffer; \n")
                .append("CREATE TEMP TABLE buffer(id  bigint, lemma_name VARCHAR(255), frequency float);\n")
                .append("insert into buffer (id , lemma_name , frequency ) values\n")
                .append("\n")
                .append(relevanceString).append("\n")
                .append(";\n")
                .append("ALTER TABLE buffer ADD COLUMN site_id bigint;\n")
                .append("update buffer set site_id=id_site;\n")
                .append("ALTER TABLE buffer ADD COLUMN one bigint;\n")
                .append("update buffer set one=1;\n")
                .append("id_page:=(select id from page where path=page_path and site_id=id_site);\n")
                .append("update lemma set frequency=(frequency+1) where lemma in (select lemma_name from" +
                        " buffer) and site_id=id_site;\n")
                .append("count_lines:=(select count(*)from buffer);\n")
                .append("insert  into lemma (lemma,frequency,site_id) \n")
                .append("select lemma_name,one,site_id from buffer where lemma_name not " +
                        "in (select lemma from lemma where site_id=id_site);\n")
                .append("loop\n")
                .append("lemma_in_table:=(select lemma_name from buffer where id=iterator);\n")
                .append("insert into index(lemma_id,page_id,rank)values\n")
                .append("(\n")
                .append("(select id from lemma where lemma=lemma_in_table and site_id=id_site),\n")
                .append("    id_page,\n")
                .append("    (select frequency from buffer where id=iterator)\n")
                .append(");\n")
                .append("iterator:=(iterator+1);\n")
                .append("exit when iterator>count_lines;\n")
                .append("end loop;\n")
                .append("END $$;").toString();
    }

   private String nativeQueryFirst (Map<String, Integer> oldLemmasMap, String  oldLemmasInTable) {
      return   new StringBuilder().append("DO $$\n")
                .append("DECLARE\n").append("site_path varchar(255):= '")
                .append(pageParameters.getSitePath()).append("';\n")
                .append("page_path text:='")
                .append(pageParameters.getPath()).append("'; \n")
                .append("id_site bigint:= 0;\n")
                .append("id_page bigint:=0; \n")
                .append("iterator bigint:=1; \n")
                .append("count_lines bigint:=").append(oldLemmasMap.size()).append("; \n")
                .append("lemma_in_table varchar(255) :=''; \n")
                .append("id_lemma_in_lemma bigint:=0; \n")
                .append("id_lemma_in_table bigint:=0;\n")
                .append("\n").append("BEGIN\n")
                .append("id_site:=(select id from site where url=site_path);\n")
                .append("--блок удаления старых лемм\n")
                .append("if (count_lines>0)\n")
                .append("then\n")
                .append("id_page:=(select id from page where path=page_path and site_id=id_site);\n")
                .append("DROP TABLE IF EXISTS extra_buffer;\n")
                .append("CREATE TEMP TABLE extra_buffer( id bigint, lemma_name VARCHAR(255) );\n")
                .append("insert into extra_buffer(id,lemma_name) values \n")
                .append("\n")
                .append(oldLemmasInTable).append(";\n")
                .append("delete from index where lemma_id in\n")
                .append("(select id from lemma where lemma in (select lemma_name from extra_buffer))\n")
                .append("and page_id = id_page;\n")
                .append("\n")
                .append("update lemma set frequency=(frequency-1) where lemma in(select lemma_name" +
                        " from extra_buffer) and site_id =id_site;\n").append("delete from lemma where frequency=0;\n")
                .append("end if;\n").append("\n").append("delete from page where path=page_path and site_id=id_site;\n")
                .append("END $$;").toString();
    }

    private List<String> getPath(String uri) {
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        String path = url.getPath();
        String sitePath = uri.substring(0, uri.indexOf(path));
        sitePath += "/";
        List<String> list = new ArrayList<>();
        list.add(uri);
        list.add(sitePath);
        list.add(path);
        return list;
    }

}
