package com.globa.search.engine.service.response;
import com.globa.search.engine.data.SiteDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AddingOrUpdatingPage {
    @Autowired
    SiteDataService dataService;

    private boolean uriInDataBase(String uri){
        Pattern pattern=Pattern.compile("^(http(s)?://)?[a-z0-9-]+\\.(.[a-z0-9-]+)+(:[0-9]+)?(/.*)?$");
        Matcher matcher= pattern.matcher(uri);
       if(matcher.matches()) {
           URL url;
           try {
               url = new URL(uri);
           } catch (MalformedURLException e) {
               e.printStackTrace();
               return false;
           }
           String path = url.getPath();
           String sitePath = uri.substring(0, uri.indexOf(path));
           sitePath += "/";
           System.out.println("==========="+sitePath);
           if (!(dataService.finedSiteByUrl(sitePath) == null)) {
               return true;
           }
           else{
               return false;
           }
       }
            else{
                return false;
            }

    }
    public boolean getResult(String uri){
      return uriInDataBase(uri);
    }
}
