// google mapsの短縮URLから緯度経度を調べる
// AndroidのIntentで得られるURLが対象
// Web版mapから得られるURLにはcidが無いので無理

package org.shokai.gmapstars;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GMapUtil {

    public static final String TAG = "GMapUtil";

    public static class Location{
        public double latitude;
        public double longitude;
        public String name;
        public URL url;
        public String cid;
        public String toString(){
            return name+" "+url.toString()+" [lat:"+latitude+",lon:"+longitude+",cid:"+cid+"]";
        }
    }

    public interface LocationCallback {
        void onSuccess(Location location);
        void onError(Exception ex);
    }

    public static void getLocation(Intent intent, final LocationCallback callback){
        if(intent == null){
            callback.onError(new Exception("intent is null"));
            return;
        }
        if(!intent.getAction().equals(Intent.ACTION_SEND)){
            callback.onError(new Exception("invalid intent"));
            return;
        }
        final Location location = new Location();

        Bundle extras = intent.getExtras();
        location.name = extras.getString(Intent.EXTRA_SUBJECT); // 地名
        String text = extras.getString(Intent.EXTRA_TEXT);
        getLocationFromIntentText(text, new LocationCallback() {
            @Override
            public void onSuccess(Location _location) {
                location.url = _location.url;
                location.latitude = _location.latitude;
                location.longitude = _location.longitude;
                location.cid = _location.cid;
                callback.onSuccess(location);
            }

            @Override
            public void onError(Exception ex) {
                callback.onError(ex);
            }
        });
    }

    public static void getLocationFromIntentText(String text, final LocationCallback callback){
        final Location location = new Location();
        URL shortUrl;
        try {
            shortUrl = getUrlFromIntentText(text);
        }
        catch (Exception ex){
            callback.onError(ex);
            return;
        }
        location.url = shortUrl;

        new GetCidFromShortUrlTask(new GetCidFromShortUrlTask.Callback() {
            @Override
            public void onSuccess(String cid) {
                location.cid = cid;
                new GetLocationFromCidTask(new GetLocationFromCidTask.Callback() {
                    @Override
                    public void onSuccess(Location _location) {
                        location.longitude = _location.longitude;
                        location.latitude = _location.latitude;
                        callback.onSuccess(location);
                    }

                    @Override
                    public void onError(Exception ex) {
                        callback.onError(ex);
                    }
                }).execute(cid);
            }

            @Override
            public void onError(Exception ex) {
                callback.onError(ex);
            }
        }).execute(shortUrl);
    }

    public static URL getUrlFromIntentText(String text) throws MalformedURLException{
        Matcher m = Pattern.compile("\\s+(https?://[^\\s]+)$").matcher(text);
        if (m.find()) return new URL(m.group(1));
        return null;
    }

    public static class GetCidFromShortUrlTask extends AsyncTask<URL, Void, String>{

        public interface Callback{
            void onSuccess(String cid);
            void onError(Exception ex);
        }

        private Callback callback;

        public GetCidFromShortUrlTask(Callback callback){
            super();
            this.callback = callback;
        }

        protected String doInBackground(URL... args) {
            URL shortUrl = args[0];
            try {
                HttpURLConnection http = (HttpURLConnection) shortUrl.openConnection();
                http.setRequestMethod("HEAD");
                http.setInstanceFollowRedirects(false);
                http.connect();
                String redirectTo = http.getHeaderField("location");
                Matcher m = Pattern.compile("cid=(\\d+)").matcher(redirectTo);
                if(m.find()) return m.group(1);
            }
            catch(Exception ex){
                Log.e(TAG, ex.getMessage());
                if(callback != null) callback.onError(ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String cid) {
            if(callback == null) return;
            if(cid == null) {
                callback.onError(new Exception("cannot find cid"));
                return;
            }
            callback.onSuccess(cid);
        }
    }

    public static class GetLocationFromCidTask extends AsyncTask<String, Void, Location>{

        public interface Callback{
            void onSuccess(Location location);
            void onError(Exception ex);
        }

        private Callback callback;

        public GetLocationFromCidTask(Callback callback){
            super();
            this.callback = callback;
        }

        protected Location doInBackground(String... args){
            String cid = args[0];
            final Location location = new Location();
            try{
                URL url = new URL("https://maps.google.com/maps?cid="+cid+"&hl=ja&output=json");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.connect();

                int code = http.getResponseCode();
                if(code != 200){
                    if(callback != null) callback.onError(new Exception("status code is "+code));
                    return null;
                }
                char[] buf = new char[1024];
                new InputStreamReader(http.getInputStream(), "UTF-8").read(buf);
                String body = new String(buf);

                Matcher m = Pattern.compile("lat:(\\-?[\\d\\.]+),lng:(\\-?[\\d\\.]+)").matcher(body);
                if(m.find()){
                    location.latitude = Double.parseDouble(m.group(1));
                    location.longitude = Double.parseDouble(m.group(2));
                    return location;
                }
            }
            catch (Exception ex){
                Log.e(TAG, ex.getMessage());
                if(callback != null) callback.onError(ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Location location){
            if(callback == null) return;
            if(location == null){
                callback.onError(new Exception("cannot get location"));
                return;
            }
            callback.onSuccess(location);
        }
    }
}
