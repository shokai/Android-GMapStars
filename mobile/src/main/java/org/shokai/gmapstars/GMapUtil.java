package org.shokai.gmapstars;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GMapUtil {

    public static URL getUrlFromIntentText(String text) throws MalformedURLException{
        Pattern pat = Pattern.compile("\\s+(https?://[^\\s]+)$");
        Matcher m = pat.matcher(text);
        if(m.find()) return new URL(m.group(1));
        return null;
    }

    public static URL getRedirectUrl(URL shortUri){
        return shortUri;
    }

    /*
    public static Pair<double, double> getLatLonFromUri(URL shortUri){

    }*/
}
