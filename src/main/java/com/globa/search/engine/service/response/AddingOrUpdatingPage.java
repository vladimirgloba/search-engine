package com.globa.search.engine.service.response;

import com.globa.search.engine.data.SiteDataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AddingOrUpdatingPage {

    private static final Logger logger = LogManager.getLogger(AddingOrUpdatingPage.class);
    @Autowired
    SiteDataService dataService;

    private boolean uriInDataBase(String uri) {
        Pattern pattern = Pattern.compile("^(http(s)?://)?[a-z0-9-]+\\.(.[a-z0-9-]+)+(:[0-9]+)?(/.*)?$");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.matches()) {
            URL url;
            try {
                url = new URL(uri);
            } catch (MalformedURLException e) {
                logger.error((char) 27 + "[31mWarning! " + "ошибка при проверке URL \n" + e.getMessage() + (char) 27 + "[0m");
                e.printStackTrace();
                return false;
            }
            String path = url.getPath();
            String sitePath = uri.substring(0, uri.indexOf(path));
            sitePath += "/";
            System.out.println("===========" + sitePath);
            return !(dataService.finedSiteByUrl(sitePath) == null);
        } else {
            return false;
        }
    }

    public boolean getResult(String uri) {
        return uriInDataBase(uri);
    }
}
