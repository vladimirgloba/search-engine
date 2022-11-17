package com.globa.search.engine.service.response;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Page;
import com.globa.search.engine.model.Site;
import com.globa.search.engine.service.LemmaFinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Repository
@Transactional
public class PageSetOrInsert {
    private static final Logger logger = LogManager.getLogger(PageSetOrInsert.class);

    @Autowired
    SiteDataService dataService;

    @Autowired
    PageParameters pageParameters;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public boolean nativeQueryForPage(String uri) {
        if (pageParameters(uri)) {
            logger.info("сформирован SQL-запрос:\n"+getSQLQuery().get(0));
            em.createNativeQuery(getSQLQuery().get(0)).executeUpdate();
            Site site = dataService.finedSiteByUrl(pageParameters.getSitePath());
            Long idSite = dataService.getIdSite(site);
            dataService.add(new Page(pageParameters.getPath(), 200,
                    pageParameters.getContent(),
                    idSite));
            logger.info("сформирован SQL-запрос:\n"+getSQLQuery().get(1));
            em.createNativeQuery(getSQLQuery().get(1)).executeUpdate();
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
        String relevanceString = "";
        Integer iterator = 1;
        Map<String, Float> relevanceMap = pageParameters.getRelevanceMap();
        Map<String, Integer> oldLemmasMap = new HashMap<>();
        String oldContent = dataService.finedContentFromPageByPath(pageParameters.getPath());
        String oldLemmasInTable = "";
        if (oldContent.length() > 0) {
            try {
                oldLemmasMap = LemmaFinder.getInstance().collectLemmas(oldContent);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        for (Map.Entry<String, Integer> entry : oldLemmasMap.entrySet()) {
            oldLemmasInTable = oldLemmasInTable + "(" + iterator + ", '" + entry.getKey() + "'),\n";
            iterator++;
        }
        iterator = 1;
        if (oldLemmasInTable.length() > 0) {
            oldLemmasInTable = oldLemmasInTable.substring(0, oldLemmasInTable.length() - 2) + "\n";
        } else {
            oldLemmasInTable = "(" + 1 + ", '" + 'a' + "')";
        }
        for (Map.Entry<String, Float> entry : relevanceMap.entrySet()) {
            relevanceString = relevanceString + "(" + iterator + ", " +
                    "'" + entry.getKey() + "', " +
                    entry.getValue() + ")," + "\n";
            iterator++;
        }

        relevanceString = relevanceString.substring(0, relevanceString.length() - 2) + "\n";

        String nativeQueryFirst =

                new StringBuilder().append("DO $$\n")
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


        String nativeQuerySecond =

                new StringBuilder().append("DO $$\n")
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

//                new StringBuilder()
//                        .append("DO $$ ")
//                        .append("\n").append("DECLARE ")
//                        .append("\n")
//                        .append("site_path varchar(255) :='").append(pageParameters.getSitePath()).append("'; ").append("\n")
//                        .append("page_path varchar(255) :='").append(pageParameters.getPath()).append("' ; ").append("\n")
//                        .append("page_contetnt text := '").append(pageParameters.getContent()).append("' ; ").append("\n")
//                        .append("id_page bigint :=0; ").append("\n")
//                        .append("id_site bigint :=0; ").append("\n")
//                        .append("iterator bigint :=1; ").append("\n")
//                        .append("count_rous bigint :=0; ").append("\n")
//                        .append("second_count_rows bigint :=1; ").append("\n")
//                        .append("lemma_in_buffer varchar(255):=''; ").append("\n")
//                        .append("id_lemma_in_lemma bigint:=0; ").append("\n")
//                        .append("BEGIN ").append("\n")
//                        .append("DROP TABLE IF EXISTS buffer; ").append("\n")
//                        .append("CREATE TEMP TABLE buffer( ").append("\n")
//                        .append("id bigint, ").append("\n")
//                        .append("lemma_name VARCHAR(255), ").append("\n")
//                        .append("relevance FLOAT ").append("\n")
//                        .append("); ").append("\n")
//                        .append("INSERT INTO buffer (id,lemma_name,relevance) VALUES ").append("\n").append(" ")
//                        .append("\n").append(relevanceString).append("; ").append("\n")
//                        .append("id_site:=(SELECT site.id FROM site WHERE site.url=site_path); ").append("\n")
//                        .append("count_rous:=(SELECT COUNT (*) FROM buffer); ").append("\n")
//                        .append(" IF page_path =(SELECT path FROM page WHERE path=page_path AND site_id=id_site) ")
//                        .append("\n").append("THEN ").append("\n")
//                        .append("id_page:=(SELECT id FROM page WHERE path=page_path AND site_id=id_site); ")
//                        .append("\n").append("UPDATE page SET content=page_contetnt WHERE id=id_page; ")
//                        .append("\n").append("LOOP ").append("\n")
//                        .append(" lemma_in_buffer:=(SELECT lemma_name FROM buffer where id=iterator); ")
//                        .append("\n").append(" IF ( ")
//                        .append("\n").append(" (SELECT id FROM lemma WHERE lemma=lemma_in_buffer ")
//                        .append("\n").append("AND site_id=id_site)= ").append("\n")
//                        .append("(SELECT lemma_id FROM index WHERE lemma_id= ")
//                        .append("\n").append("(SELECT id FROM lemma WHERE lemma=lemma_in_buffer ")
//                        .append("\n").append("AND site_id=id_site )AND page_id=id_page) ")
//                        .append("\n").append(") ").append("\n").append(" THEN ").append("\n")
//                        .append("id_lemma_in_lemma:=(SELECT id from lemma WHERE lemma=lemma_in_buffer AND site_id=id_site); ")
//                        .append("\n").append(" UPDATE index SET rank=(SELECT relevance FROM buffer WHERE id=iterator) where lemma_id=id_lemma_in_lemma ")
//                        .append("\n").append(" AND page_id=id_page; ").append("\n").append("iterator:=iterator+1; ")
//                        .append("\n").append("ELSE ").append("\n").append("INSERT INTO lemma (frequency,lemma,site_id) VALUES ")
//                        .append("\n").append(" (1,  ").append("\n").append("(SELECT lemma_name from buffer where id=iterator), ")
//                        .append("\n").append("id_site ").append("\n").append(" ); ").append("\n")
//                        .append("id_lemma_in_lemma:=(SELECT id from buffer WHERE lemma=lemma_in_buffer AND site_id=id_site); ")
//                        .append("\n").append("INSERT INTO index (lemma_id,page_id,rank) VALUES ").append("\n")
//                        .append("( ").append("\n").append("id_lemma_in_lemma, ").append("\n").append(" id_page, ")
//                        .append("\n").append("(SELECT relevance from buffer where id=iterator)").append("\n")
//                        .append(" ); ").append("\n").append("iterator=iterator+1; ").append("\n").append("END IF; ")
//                        .append("\n").append("EXIT WHEN iterator > count_rous; ").append("\n").append("END LOOP; ")
//                        .append("\n").append("iterator:=1; ").append("\n").append("RAISE NOTICE 'page is exist'; ")
//                        .append("\n").append("ELSE ").append("\n").append("INSERT INTO page (code,content,path,site_id) VALUES ")
//                        .append("\n").append("(200,page_contetnt,page_path,id_site); ").append("\n")
//                        .append("id_page:=(SELECT id FROM page WHERE path=page_path AND site_id=id_site); ")
//                        .append("\n").append("LOOP ").append("\n").append("lemma_in_buffer:=(SELECT lemma_name FROM buffer WHERE id=iterator); ")
//                        .append("\n").append("IF (SELECT lemma_name from buffer WHERE id=iterator)= ")
//                        .append("\n").append("(SELECT lemma from lemma WHERE lemma=lemma_in_buffer ")
//                        .append("\n").append("AND site_id=id_site) ").append("\n").append("THEN ")
//                        .append("\n").append("UPDATE lemma SET frequency=frequency+1 ").append("\n")
//                        .append("WHERE site_id=id_site AND lemma=lemma_in_buffer; ").append("\n")
//                        .append("id_lemma_in_lemma:=(SELECT id FROM lemma WHERE lemma=lemma_in_buffer and site_id=id_site); ")
//                        .append("\n").append(" INSERT INTO  index(lemma_id,page_id,rank) VALUES ").append("\n")
//                        .append("( ").append("\n").append("id_lemma_in_lemma, ").append("\n").append("id_page, ")
//                        .append("\n").append("(select relevance from buffer where id =iterator) ").append("\n")
//                        .append("); ").append("\n").append("iterator:=iterator+1; ").append("\n").append("ELSE ")
//                        .append("\n").append("INSERT INTO lemma (frequency,lemma,site_id) VALUES ").append("\n")
//                        .append("(1, ").append("\n").append("(SELECT lemma_name from buffer WHERE id=iterator), ")
//                        .append("\n").append("id_site ").append("\n").append("); ").append("\n")
//                        .append("id_lemma_in_lemma:=(SELECT id FROM lemma WHERE lemma=lemma_in_buffer and site_id=sid_site); ")
//                        .append("\n").append("INSERT INTO index (lemma_id,page_id,rank) VALUES ").append("\n")
//                        .append("(id_lemma_in_lemma, ").append("\n").append("id_page, ").append("\n")
//                        .append("(SELECT relevance from buffer where id=iterator) ").append("\n")
//                        .append("); ").append("\n").append("iterator:=iterator+1; ").append("\n")
//                        .append("END IF; ").append("\n").append("EXIT WHEN iterator > count_rous; ")
//                        .append("\n").append("END LOOP; ").append("\n").append("iterator:=1; ")
//                        .append("\n").append(" RAISE NOTICE 'page not exist'; ").append("\n").append("END IF; ")
//                        .append("\n").append("DROP TABLE buffer; ").append("\n").append("iterator:=1;").append("\n")
//                        .append(" END $$; ").append("\n")
//                        .toString();
        nativeQuerySecond = nativeQuerySecond.replaceAll(":=", "\\\\:=");
        nativeQueryFirst = nativeQueryFirst.replaceAll(":=", "\\\\:=");
        List<String> stringList = new ArrayList<>();
        stringList.add(nativeQueryFirst);
        stringList.add(nativeQuerySecond);
        return stringList;

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
