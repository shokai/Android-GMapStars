package org.shokai.gmapstars;

import android.net.Uri;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sho on 2015/03/23.
 */
public class GMapUtil {

    public static Uri getUriFromIntentText(String text){
        Pattern pat = Pattern.compile("\\s+(https?://[^\\s]+)$");
        Matcher m = pat.matcher(text);
        if(m.find()) return Uri.parse(m.group(1));
        return null;
    }

    /*
    private Pair<double, double> getLatLonFromURL (String text){

    }
    */
}
