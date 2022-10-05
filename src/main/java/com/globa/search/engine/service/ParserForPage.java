package com.globa.search.engine.service;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Page;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ParserForPage {
    @Autowired
    private UserAgentProperties userAgentProperties;
   @Autowired
    SiteDataService dataService;
    private String error="";
    private String bufferPath;
    private HashSet<String> pathList = new HashSet<>();
    private List<Page> pageList = new ArrayList<>();
    private String parentPath = "";
    String content = "";
    Long idSite = 0L;
    int code = 0;


    public ParserForPage() {

    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getBufferPath() {
        return bufferPath;
    }

    public void setBufferPath(String bufferPath) {
        this.bufferPath = bufferPath;
    }

    public List<Page> getPageList() {
        return pageList;
    }

    public void setPageList(List<Page> pageList) {
        this.pageList = pageList;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public Long getIdSite() {
        return idSite;
    }

    public void setIdSite(Long idSite) {
        this.idSite = idSite;
    }

    //проверка на валидность адреса
    private boolean isValidUrl(String url) {
        Pattern pattern = Pattern.compile("^(http(s)?://)?[a-z0-9-]+\\.(.[a-z0-9-]+)+(:[0-9]+)?(/.*)?$");
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }


    public void recursiveSiteCrawling() {

        if (pageList.size() >= 100 ) {
            dataService.addAllPage(pageList);
            pageList=new ArrayList<>();
        }
        URL url = null;

        try {
            url = new URL(bufferPath);
            System.out.println(bufferPath);
        } catch (MalformedURLException e) {
            error=error+e.getMessage()+"не соответствует формат адреса - "+bufferPath+"\n";
        }
        if (isValidUrl(bufferPath) && pathList.add(url.getPath())) {
            System.out.println(url.getPath());
            Connection.Response response = null;
            try {
                response = Jsoup.connect(bufferPath).userAgent(userAgentProperties.getUserAgent())
                        .timeout(5000)
                        .execute();
            } catch (Exception e) {
                e.printStackTrace();
                error=error+e.getMessage()+" код = "+code+" on path = "+bufferPath+"\n";
                content=null;
            }
            code = response.statusCode();
            if (code != 200) {
                content = null;
                error=error=error+"код ответа = "+code+"  при открытии адреса - "+bufferPath+"\n";
            } else {

                try {
                    content = response.parse().toString();
                } catch (UnsupportedMimeTypeException e) {
                    e.printStackTrace();
                    content = null;
                    error= error=error+"ошибка при открытии контента страницы"+" код = "+code+" on path = "+bufferPath+"\n";
                } catch (IOException e) {
                    e.printStackTrace();
                    content = null;
                    error= error=error+"ошибка при открытии контента страницы"+" код = "+code+" on path = "+bufferPath+"\n";
                }
            }
            pageList.add(new Page(url.getPath(), code, content, idSite));
            System.out.println("in pageList size of list = "+pageList.size()+" url = "+bufferPath +"content size = "+content.length()+" code = "+code);
            if (content != null) {
                Document document = Jsoup.parse(content);
                System.out.println("парсинг для "+bufferPath);

                Elements linksOnPage = document.select("a[href]");
                for (Element element:linksOnPage){
                    boolean ff=element.attr("abs:href").contains(parentPath);
                    System.out.println(element.attr("abs:href")+" = "+parentPath+" "+ff+ "=" +isValidUrl(element.attr("abs:href")));
                }
                for (Element page1 : linksOnPage) {

                    bufferPath = page1.attr("abs:href");

                    if (isValidUrl(bufferPath) && bufferPath.indexOf(parentPath)!=-1) {

                        recursiveSiteCrawling();
                    }
                }
            }

        }
    }
}

