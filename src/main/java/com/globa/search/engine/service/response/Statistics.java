package com.globa.search.engine.service.response;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Site;
import com.globa.search.engine.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
@Component
public class Statistics  {
    @Autowired
    private SiteDataService dataService;
    private Total total;
    private List<Detailed> detailed;


    public Statistics() {

    }




    private void setTotal() {
        Long sites = Long.valueOf(dataService.finedAllSites().size());
        Long lemmas = dataService.countOfLemma();
        Long pages = dataService.countOfPage();
        boolean isIndexing = isSitesIndexing();
        this.total=new Total(sites,pages,lemmas,isIndexing);

    }


    private Detailed setDetailed(Site site) {
        String url = site.getUrl().substring(0,site.getUrl().length()-1);
        String name = site.getName();
        Status status = site.getStatus();

        long statusTime =(java.util.Date
                .from(site.getStatus_time().atZone(ZoneId.systemDefault())
                        .toInstant())).getTime();
        String error = site.getLast_error();
        Long pages = dataService.countPagesOnSite(site.getId());
        System.out.println(""+statusTime+" = "+site.getStatus_time());
        Long lemmas = dataService.countLemmasOnSite(site.getId());
        return new Detailed(url, name, status, statusTime, error, pages, lemmas);
    }

    public void setDetailed() {
        detailed = new ArrayList<>();
        for (Site site:dataService.finedAllSites()) {
            detailed.add(setDetailed(site));
        }

    }

    private boolean isSitesIndexing() {
        boolean is = true;
        for (Site s :dataService.finedAllSites()) {
            if (s.getStatus().equals(Status.INDEXED)) {
                is = false;
                break;
            }
        }
        return is;
    }

    public void setTotal(Total total) {
        this.total = total;
    }

    public void setDetailed(List<Detailed> detaileds) {
        this.detailed = detaileds;
    }

    public Total getTotal() {
        return total;
    }

    public List<Detailed> getDetailed() {
        return detailed;
    }

    public void getStatistic() {
        setDetailed();
        setTotal();

    }
}