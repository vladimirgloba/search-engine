package com.globa.search.engine.service;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Page;

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
    @Autowired
   private SiteDataService dataService;
    @Autowired
    private ResultService resultService;
private int totalSize;
    private static final Logger logger = LogManager.getLogger(PageListFinder.class);
    public PageListFinder() {
    }




    private Map <String, Integer> sortedLemmasMap(String searchQuery,Long idSite) throws IOException {

        Map<String,Integer>buffer=LemmaFinder.getInstance().collectLemmas(searchQuery);
        if(!buffer.isEmpty()) {
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
            for(Map.Entry<String,Integer> entry:sortedMap.entrySet()){
                System.out.println(entry.getKey()+" = "+entry.getValue());
            }
            return sortedMap;
        }else return new HashMap<>();
    }

    public List<Map<String,Float>> searchWithNativeQuery(String searchQuery,String urlSite){
       Site site=dataService.finedSiteByUrl(urlSite);
       Long idSite= site.getId();
      List  <Map<String,Float>>resultList=new ArrayList<>();
        try {
            Map<String,Integer>buffer=sortedLemmasMap( searchQuery,idSite);
            if(!buffer.isEmpty()){
                Map.Entry actualValue = buffer.entrySet()
                        .stream()
                        .findFirst()
                        .get();
                while (actualValue.getValue()==(Integer)0){
                    buffer.remove(actualValue.getKey());
                    actualValue = buffer.entrySet()
                            .stream()
                            .findFirst()
                            .get();
                }
                List<String>lemmasList=new ArrayList<>();
                for(Map.Entry<String,Integer> entry:buffer.entrySet()){
                    lemmasList.add(entry.getKey());
                }
                resultList=dataService.finedResultList(actualValue.getKey().toString(),lemmasList,idSite);
                System.out.println("result list size = "+resultList.size());
            }
        } catch (IOException e) {
            logger.error((char) 27 + "[31mWarning! "+"ошибка :\n"+e.getMessage() + (char)27 + "[0m");
            return new ArrayList<>();
        }
        return resultList;
    }
    public Map<String,Float> sortedPagesMap(String searchQuery,Long idSite) throws IOException {
        String firstValue="";
        Map<String,Float> pagesMap=new HashMap<>();

        Map<String,Integer>buffer=sortedLemmasMap( searchQuery,idSite);
        if(!buffer.isEmpty()) {
            Map.Entry actualValue = buffer.entrySet()
                    .stream()
                    .findFirst()
                    .get();
   while (actualValue.getValue()==(Integer)0){
       buffer.remove(actualValue.getKey());
       actualValue = buffer.entrySet()
               .stream()
               .findFirst()
               .get();
   }
            firstValue=actualValue.getKey().toString();
            System.out.println("actualValue = "+actualValue.getKey().toString()+" = "+actualValue.getValue());
            Long idFirstLemma= dataService.finedIdByLemmaName(firstValue,idSite);
            System.out.println("IdLemma = "+idFirstLemma);
            List<Long> listIdPage=dataService.listIdPages(idFirstLemma);
            Map<Long,Float> mapIdPageAndRank=dataService.sortedPageMap(listIdPage,buffer,idSite);
            for(Map.Entry<Long,Float> entry:mapIdPageAndRank.entrySet()){
                System.out.println(entry.getKey()+" = "+entry.getValue());
            }
            List<Page> pages=dataService.finedListPageByIdPageList(listIdPage);
            Map<String,Float>mapPathAndRank=new HashMap<>();
            for(Page page:pages){
                mapPathAndRank.put(page.getPath(),mapIdPageAndRank.get(page.getId()));
            }
            Map<String, Float> sortedMap = mapPathAndRank.entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .collect(Collectors
                            .toMap(Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (e1, e2) -> e1,
                                    LinkedHashMap::new));
            for(Map.Entry<String,Float> entry:sortedMap.entrySet()){
                System.out.println(entry.getKey()+" = "+entry.getValue());
            }
            return sortedMap;
        }
else
return new HashMap<>();
    }
    public List<ResponseForSearchQueryFirstLevel> sortedPagesMapWithSQL(String searchQuery,String sitePath){
        List<ResponseForSearchQueryFirstLevel>searchList=new ArrayList<>();
        Site site= dataService.finedSiteByUrl(sitePath);
       Long idSite=site.getId();
        try {
            Map<String,Integer>buffer=sortedLemmasMap( searchQuery,idSite);
            if(buffer.size()>0){
                Map.Entry actualValue = buffer.entrySet()
                        .stream()
                        .findFirst()
                        .get();
                while ((actualValue.getValue()==(Integer)0)&&buffer.size()>1){
                    buffer.remove(actualValue.getKey());

                    actualValue = buffer.entrySet()
                            .stream()
                            .findFirst()
                            .get();}

if(buffer.size()==1){

    actualValue= (Map.Entry) buffer.entrySet().toArray()[0];

}
if(buffer.size()==1&&(actualValue.getValue()==(Integer)0)){
    return searchList;
}
                List<String>lemmasList=new ArrayList<>();
                for(Map.Entry<String,Integer> entry:buffer.entrySet()){
                    lemmasList.add(entry.getKey());
                }
                System.out.println("in lemmaList = "+lemmasList);
                searchList= resultService.displayAllContactSummary(actualValue.getKey().toString(),lemmasList,site,dataService,(int)20);

            }
        } catch (IOException e) {
            logger.error((char) 27 + "[31mWarning! "+"ошибка :\n"+e.getMessage() + (char)27 + "[0m");

        }
        this.totalSize=resultService.getTotalSize();
return searchList;
    }
public int getTotalSize(){
        return this.totalSize;
}
}
