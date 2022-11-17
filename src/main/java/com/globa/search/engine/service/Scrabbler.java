package com.globa.search.engine.service;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Page;
import com.globa.search.engine.model.Site;
import com.globa.search.engine.model.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//этот класс был использован первоначально для создания БД, но минус скорость из-за JPA
@Service
public class Scrabbler extends RecursiveAction {
    @Autowired
    private SiteDataService dataService;

    @Autowired
    private UserAgentProperties userAgentProperties;

    @Autowired
    private ListOfObjectProperties list;

    private static final Logger logger = LogManager.getLogger(ResultService.class);
    private HashSet<String> links=new HashSet<>();
    private String nameOffSite;
    private String path;
    private Connection.Response response;
    private String mainUrl;
    public void setURL(String path) {
        this.path = path;
    }
    public String getURL() {
        return path;
    }
    private  long idSite;

    public Scrabbler() {

    }

    public HashSet<String> getLinks() {
        return links;
    }

    public void setLinks(HashSet<String> links) {
        this.links = links;
    }

    public void setNameOffSite(String nameOffSite) {
        this.nameOffSite = nameOffSite;
    }

    public long getIdSite() {
        return idSite;
    }

    public long setIdSite(String nameOffSite) {
        Status status=Status.INDEXING;
        LocalDateTime dateTime=LocalDateTime.now();
        String lastError="null";
        String name=nameOffSite;
        String uri=path;
        Site site=new Site(status,dateTime,lastError,name,path);
        dataService.addSite(status,dateTime,lastError,name,mainUrl+"/");
        return dataService.getIdSite(site);
    }

    private ForkJoinTask<?> linksOnPage() {
        URL url = null;
        int status=0;
        System.out.println(path);
        try {
            url = new URL(path);
        } catch (MalformedURLException e) {
            logger.error((char) 27 + "[31mWarning! ошибка :\n"+e.getMessage() + (char)27 + "[0m");
        }
        if (!links.contains(url.getPath()) && (checkUrl(path))) {
            System.out.println("В список = "+path);
            links.add(url.getPath());
            Page page;
            try {
                response = Jsoup.connect(path)
                        .userAgent(userAgentProperties.getUserAgent())
                        .timeout(5000)
                        .execute();
                Document document = Jsoup.connect(path).get();
if(response.statusCode()==200) {
  StringBuilder buffer=new StringBuilder();
    try {
        buffer.append(document);
    }
     catch (Exception e) {
        buffer=null;
         logger.error((char) 27 + "[31mWarning! ошибка :\n"+e.getMessage() + (char)27 + "[0m");

    }
    dataService.add(new Page(url.getPath(), response.statusCode(),
            buffer.toString(),
            idSite));
    System.out.println("занесено в базу");
}else{
    dataService.add(new Page(url.getPath(), response.statusCode(), null, idSite));
}

                Elements linksOnPage = document.select("a[href]");
                for (Element page1 : linksOnPage) {

                    path = page1.attr("abs:href");

                    if (checkHtml(path,mainUrl)) {
                        System.out.println("проверка удачно path= "+path+" mainUrl = "+mainUrl);
                        linksOnPage();
                    }else System.out.println("проверка неудачно path= "+path+" mainUrl = "+mainUrl);
                }



            } catch (IOException e) {
                e.printStackTrace();
                dataService.add(new Page(url.getPath(), response.statusCode(), null,idSite));
                logger.error((char) 27 + "[31mWarning! ошибка :\n"+e.getMessage() + (char)27 + "[0m");

                return null;
            }


            }else System.out.println("уже в базе ="+path);
        return null;
    }


    //Реализуем метод compute().
    @Override
    protected void compute() {
for(ListOfObjectProperties.Service service:list.getServices()) {
this.path=service.getUrl()+"/";
this.mainUrl=service.getUrl();
idSite=setIdSite(service.getName());
links=new HashSet<>();
try{
    linksOnPage();
    System.out.println("перед изменением таблицы url = "+mainUrl+"/");
    System.out.println(list.getServices().size());
    Status status=Status.INDEXED;
    dataService.updateSite(status,LocalDateTime.now(),"no error",mainUrl+"/");}
catch (Exception e){
    System.out.println(list.getServices().size());
    Status status=Status.FAILED;
    dataService.updateSite(status,LocalDateTime.now(),e.getMessage(),mainUrl+"/");
    logger.error((char) 27 + "[31mWarning! ошибка :\n"+e.getMessage() + (char)27 + "[0m");
}
}
    }


    private boolean checkUrl(String str){
        Pattern pattern=Pattern.compile("^(https:\\/\\/|http:\\/\\/)([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$");
        Matcher matcher= pattern.matcher(str);
        return   matcher.matches();
    }
    private boolean checkHtml(String str,String mainUrl){
        String subString="";
        mainUrl=mainUrl.toLowerCase();
        if(mainUrl.contains("www.")){
            int firstIndex=mainUrl.indexOf(".");
            subString=mainUrl.substring(firstIndex+1);
        }
        else{
            int firstIndex=mainUrl.indexOf("//");
            subString=mainUrl.substring(firstIndex+2);
        }
//        System.out.println("subString = " + subString);
//        System.out.println( "in check method = "+mainUrl);
//        String regexp="^(https:\\/\\/|http:\\/\\/)(www\\.)?"+subString+"(.+?)$";
//
//        Pattern pattern=Pattern.compile(regexp);
//        Matcher matcher=pattern.matcher(str);

//        return matcher.matches();
        return str.contains(subString) && !str.contains("news");
    }




}





