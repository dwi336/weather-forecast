/*
 * Copyright (C) 2017 David Boddie <david@boddie.org.uk>
 * Copyright (C) 2019 Dietmar Wippig <dwi336.dev@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package uk.org.boddie.android.weatherforecast;

import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.xmlpull.v1.XmlPullParserException;

import uk.org.boddie.android.weatherforecast.R;

public class WeatherForecastActivity extends Activity implements LocationListener{
    private Map<String, CacheItem> cache = new HashMap<String, CacheItem>();
    private TLSSocketFactory socketFactory;

    private long current_time;
    private LocationWidget entryWidget;
    private ForecastWidget forecastWidget;
    private ForecastParser parser;
    private String place;
    private String state = "entry";
    private Task task;

    public WeatherForecastActivity() {
        super();
    }

    public void onCreate(Bundle paramBundle){

        super.onCreate(paramBundle);

        this.entryWidget = new LocationWidget(this);
        this.entryWidget.setLocationListener(this);
        this.forecastWidget = new ForecastWidget(this);
        setContentView(this.entryWidget);
        this.parser = new ForecastParser(getResources());

        // By default, don't use a custom socket factory. We need one to handle
        // communications for versions of Android before Lollipop (5), apparently.
        this.socketFactory = null;

        try {
            if ( (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) &&
                 (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) ) {
                // For versions with possible TLSv1.2 support, use a custom
                // socket factory.
                this.socketFactory = new TLSSocketFactory();
                HttpsURLConnection.setDefaultSSLSocketFactory(this.socketFactory);
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                // We cannot use TLSv1.2 with earlier versions.
                //raise ValueError()	
            }
        } catch (Exception e) {
            // Tell the user about the lack of secure networking.
            Toast.makeText(this, "Cannot use a secure protocol.\nFalling back to insecure networking.", Toast.LENGTH_LONG).show();	
        }
    }

    public void onPause(){
        super.onPause();
        this.entryWidget.writeLocations();
    }

    public void locationEntered(String location){

        if (this.state.equals("fetching")) {
            return;
        }

        this.current_time = System.currentTimeMillis();
        this.place = location;

        try{
            CacheItem item = (CacheItem)this.cache.get(location);
            if (item != null) {
                if ( (this.current_time - item.time) < 3600000) { // 60 minutes
                    showForecasts(item.forecasts, "");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.state = "fetching";

        this.task = new Task(this, this.socketFactory);
        String[] array = new String[1];
        array[0] = location;
        this.task.execute(array);
        //this.parseForecasts(self.getSampleStream());
    }


    public void showForecasts(List<Forecast> forecasts, String errorMessage){

        if ( (forecasts == null) || forecasts.size() == 0) {
            this.showError(errorMessage);
            this.state = "entry";
            return;
        }

        this.cache.put( this.place, new CacheItem(this.current_time, forecasts) );

        try {
            this.forecastWidget.addForecasts(forecasts);

            this.state = "forecast";
            this.setContentView(this.forecastWidget);

        } catch (Exception e){
            this.state = "entry";
            this.showError("");
        }
    }

    public void showError(String errorMessage){

        if (!errorMessage.equals("")) {
            android.util.Log.w("DUCK", errorMessage);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to read weather forecast.", Toast.LENGTH_SHORT).show();
        }
    }

    public InputStream getSampleStream(){

        Resources ressources = this.getResources();
        return ressources.openRawResource(R.raw.sample);
    }

    public void onBackPressed(){

        if (this.state.equals("forecast")){
            // Return to the entry widget.
            this.state = "entry";
            setContentView(this.entryWidget);
        } else if (this.state.equals("entry")){
            // If already showing the entry widget then exit.
            super.onBackPressed();
        }
    }

    //  Params  Progress Result
    private static class Task extends AsyncTask<String, Integer, Forecasts> {
        private WeakReference<WeatherForecastActivity> activityReference;
        private TLSSocketFactory socketFactory;
        private String errorMessage;

        protected Task(WeatherForecastActivity activity, TLSSocketFactory socketFactory){
            this.activityReference =  new WeakReference<>(activity);
            this.socketFactory = socketFactory;
            this.errorMessage = "";
        }

        @Override
        protected Forecasts doInBackground(String...params){

            // get a reference to the activity if it is still there
            WeatherForecastActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            // Unpack the location from the array.
            String location = (String) params[0];

            Forecasts forecasts;
            try{
                forecasts = fetchData(location,activity);
            } catch (Exception e){
                this.errorMessage = e.toString();
                return activity.new Forecasts(null);
            }
            return forecasts;
        }

        private Forecasts fetchData(String place, WeatherForecastActivity activity){
            URL url = null;

            try {
                if (this.socketFactory != null) {
                    url = new URL("https://www.yr.no/place/" + place + "/forecast.xml");
                } else {
                    // Fall back to HTTP. We will have already warned about this when
                    // the application started.
                    url = new URL("http://www.yr.no/place/" + place + "/forecast.xml");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedInputStream stream;
            Forecasts forecasts = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(true);
                stream = new BufferedInputStream(connection.getInputStream());
                forecasts = activity.new Forecasts(activity.parser.parse(stream));
                stream.close();
            } catch (XmlPullParserException | IOException e1) {
                e1.printStackTrace();
            }

            return forecasts;
        }

        @Override
        protected void onPostExecute(Forecasts forecasts){
            // get a reference to the activity if it is still there
            WeatherForecastActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.showForecasts(forecasts.forecasts, this.errorMessage);
        }
    }

    private class CacheItem{
        private List<Forecast> forecasts;
        private long time;

        protected CacheItem(long time, List<Forecast> forecasts){
            this.time = time;
            this.forecasts = forecasts;
        }
    }

    private class Forecasts{
        private List<Forecast> forecasts;

        protected Forecasts(List<Forecast> forecasts){
            this.forecasts = forecasts;
        }
    }

}
