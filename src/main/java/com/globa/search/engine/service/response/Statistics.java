package com.globa.search.engine.service.response;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Site;
import com.globa.search.engine.model.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class Statistics {

    private static final Logger logger = LogManager.getLogger(Statistics.class);

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
        this.total = new Total(sites, pages, lemmas, isIndexing);

    }

    private Detailed setDetailed(Site site) {
        String url = site.getUrl().substring(0, site.getUrl().length() - 1);
        String name = site.getName();
        Status status = site.getStatus();

        long statusTime = (java.util.Date
                .from(site.getStatus_time().atZone(ZoneId.systemDefault())
                        .toInstant())).getTime();
        String error = site.getLast_error();
        Long pages = dataService.countPagesOnSite(site.getId());
        logger.info("\n" + "обработка информации по сайту: " + site.getName() + "\n" + "statusTime  = " + site.getStatus_time() + "\nin path =" + site.getUrl() + "\n");
        Long lemmas = dataService.countLemmasOnSite(site.getId());
        return new Detailed(url, name, status, statusTime, error, pages, lemmas);
    }

    public void setDetailed() {
        detailed = new ArrayList<>();
        for (Site site : dataService.finedAllSites()) {
            detailed.add(setDetailed(site));
        }
    }

    private boolean isSitesIndexing() {
        boolean is = true;
        for (Site s : dataService.finedAllSites()) {
            if (s.getStatus().equals(Status.INDEXED)) {
                is = false;
                break;
            }
        }
        return is;
    }

    public Total getTotal() {
        return total;
    }

    public void setTotal(Total total) {
        this.total = total;
    }

    public List<Detailed> getDetailed() {
        return detailed;
    }

    public void setDetailed(List<Detailed> detaileds) {
        this.detailed = detaileds;
    }

    public void getStatistic() {
        setDetailed();
        setTotal();

    }
}