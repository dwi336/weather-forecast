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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import uk.org.boddie.android.weatherforecast.R;

public class WeatherForecastActivity extends Activity implements ConfigureListener, LocationListener{
    private Map<String, CacheItem> cache = new HashMap<String, CacheItem>();
    public HashMap<String, Coordinates> coordinates;
    private SharedPreferences preferences;
    private TLSSocketFactory socketFactory;
    private ConfigureWidget configureWidget;
    private long current_time;
    private LocationWidget entryWidget;
    private ForecastWidget forecastWidget;
    private ForecastParser parser;
    public String place;
    public String place_name;
    private String state = "entry";
    private HashMap<String, Integer> symbols = new HashMap<String, Integer>();
    private Task task;

    public WeatherForecastActivity() {
        super();
    }

    public void onCreate(Bundle paramBundle){

        super.onCreate(paramBundle);
        setTheme(android.R.style.Theme_DeviceDefault);

        // Obtain the keys and values to be used to create the symbols
        // dictionary from the application's resources.
        Resources resources = getResources();
        String[] symbols = resources.getStringArray(R.array.symbols);
        TypedArray resourceIDs = resources.obtainTypedArray(R.array.resourceIDs);

        int j = Math.min(symbols.length, resourceIDs.length());
        for (int i = 0; i < j; i++){
            this.symbols.put(symbols[i], resourceIDs.getResourceId(i, -1));
        }
        resourceIDs.recycle();

        this.parser = new ForecastParser(this.symbols);

        // Map place specifications to coordinates so that when another component
        // provides a specification, it can be used to obtain a forecast.
        final String[] places = resources.getStringArray(R.array.places);
        final String[] latitudes = resources.getStringArray(R.array.latitudes);
        final String[] longitudes = resources.getStringArray(R.array.longitudes);
        final String[] altitudes = resources.getStringArray(R.array.altitudes);

        this.coordinates = new HashMap<String, Coordinates>();

        int i = 0;
        while (i < latitudes.length) {
            final Coordinates c = new Coordinates();
            c.latitude = latitudes[i];
            c.longitude = longitudes[i];
            c.altitude = altitudes[i];
            this.coordinates.put(places[i].toLowerCase(), c);
            i++;
        }

        this.entryWidget = new LocationWidget(this,this,this);
        this.configureWidget = new ConfigureWidget(this, this.symbols, this);
        this.forecastWidget = new ForecastWidget(this);
        setContentView(this.entryWidget);

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
        this.preferences = this.getSharedPreferences("WeatherForecast", 0);
        this.restorePreferences();
    }

    @Override
    public void restorePreferences() {
        this.configureWidget.restore(this.preferences);
    }

    @Override
    public void savePreferences() {
        final SharedPreferences.Editor editor = this.preferences.edit();
        this.configureWidget.save(editor);
        editor.commit();
    }

    public void onPause(){
        super.onPause();
        this.entryWidget.writeLocations();
    }

    public void locationEntered(String name, String location){

        if (this.state.equals("fetching")) {
            return;
        }

        this.current_time = System.currentTimeMillis();
        this.place = location;
        this.place_name = name;
        location = location.toLowerCase();

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

        Coordinates coordinates;
        try {
            coordinates = this.coordinates.get(location);
            if (coordinates == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            this.showError("");
            this.state = "entry";
            return;
        }

        this.state = "fetching";

        // Normal operation:
        this.task = new Task(this, this.socketFactory);
        Coordinates[] array = new Coordinates[1];
        array[0] = coordinates;
        this.task.execute(array);

        // Testing using sample data:
        //Forecasts forecasts = new Forecasts(this.parser.parse(this.getSampleStream()));
        //this.showForecasts(forecasts.forecasts, "Invalid sample input");
    }

    public void showForecasts(List<Forecast> forecasts, String errorMessage){

        if ( (forecasts == null) || forecasts.size() == 0) {
            this.showError(errorMessage);
            this.state = "entry";
            return;
        }

        this.cache.put( this.place, new CacheItem(this.current_time, forecasts) );

        try {
            this.forecastWidget = new ForecastWidget(this);
            this.forecastWidget.restore(this.preferences);
            this.forecastWidget.addForecasts(this.place_name, forecasts, this.preferences);

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

    public void startConfiguration() {
        this.state = "configure";
        this.setContentView(this.configureWidget);
    }

    public void finishConfiguration(boolean save) {
        if (save) {
            this.savePreferences();
        }
        this.restorePreferences();
        this.state = "entry";
        this.setContentView(this.entryWidget);
    }

    public void onBackPressed(){
        if ( this.state.equals("forecast") || this.state.equals("configure") ){
            // Return to the entry widget.
            this.state = "entry";
            setContentView(this.entryWidget);
        } else if (this.state.equals("entry")){
            // If already showing the entry widget then exit.
            super.onBackPressed();
        }
    }

    //  Params  Progress Result
    private static class Task extends AsyncTask<Coordinates, Integer, Forecasts> {
        private WeakReference<WeatherForecastActivity> activityReference;
        private TLSSocketFactory socketFactory;
        private String errorMessage;

        protected Task(WeatherForecastActivity activity, TLSSocketFactory socketFactory){
            this.activityReference =  new WeakReference<>(activity);
            this.socketFactory = socketFactory;
            this.errorMessage = "";
        }

        @Override
        protected Forecasts doInBackground(Coordinates... params){

            // get a reference to the activity if it is still there
            WeatherForecastActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            // Unpack the location from the array.
            Coordinates location = (Coordinates) params[0];

            Forecasts forecasts;
            try{
                forecasts = fetchData(location,activity);
            } catch (Exception e){
                this.errorMessage = e.toString();
                return activity.new Forecasts(null);
            }
            return forecasts;
        }

        private Forecasts fetchData(Coordinates coordinates, WeatherForecastActivity activity){
            URL url = null;

            // Try to send an HTTPS request even if we have already warned about the
            // lack of support when the application started. Sending an HTTP request
            // will fail due to a 301 Moved Permanently response.
            try {
                url = new URL("https://api.met.no/weatherapi/locationforecast/2.0/compact?" + 
                    "altitude=" + coordinates.altitude +
                    "&lat=" + coordinates.latitude +
                    "&lon="+ coordinates.longitude);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedInputStream stream;
            Forecasts forecasts = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(true);          
                connection.setRequestProperty("User-Agent", "uk.org.boddie.android.weatherforecast");
                connection.setRequestProperty("Accept", "application/json");

                stream = new BufferedInputStream(connection.getInputStream());
                forecasts = activity.new Forecasts(activity.parser.parse(stream));
                stream.close();
            } catch (JSONException | IOException e) {
                return activity.new Forecasts();
            }

            return forecasts;
        }

        @Override
        protected void onPostExecute(Forecasts forecasts){
            // get a reference to the activity if it is still there
            WeatherForecastActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (forecasts != null) {
                activity.showForecasts(forecasts.forecasts, this.errorMessage);
            }
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

    private class Coordinates{
        public String altitude;
        public String latitude;
        public String longitude;

        public Coordinates() {
            super();
        }
    }
    
    private class Forecasts{
        private List<Forecast> forecasts;

        protected Forecasts(){
            this.forecasts = new ArrayList<Forecast>();
        }

        protected Forecasts(List<Forecast> forecasts){
            this.forecasts = forecasts;
        }
    }

}
