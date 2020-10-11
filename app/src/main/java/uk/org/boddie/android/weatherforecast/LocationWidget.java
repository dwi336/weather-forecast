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

import android.content.Context;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ViewAnimator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import uk.org.boddie.android.weatherforecast.R;

public class LocationWidget extends RelativeLayout implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AddLocationListener, RemoveLocationListener, ModeHandler {
    private LocationAdapter adapter;
    private ConfigureListener configureHandler;
    private int currentItem = -1;
    private ListView listView;
    private LocationListener locationHandler;
    private HashMap<String, String> locations;
    private String mode = "menu";
    private LinkedList<String> order;
    private String previous_mode = "menu";
    private HashMap<String, Integer> view_indices;
    private ViewAnimator views;

    public LocationWidget(final Context context, final LocationListener locationHandler, final ConfigureListener configureHandler) {
        super(context);
        this.configureHandler = configureHandler;
        this.locationHandler = locationHandler;
        this.adapter = this.getAdapter();
        this.listView = new ListView(context);
        this.listView.setAdapter(this.adapter);
        this.listView.setOnItemClickListener(this);
        this.listView.setOnItemLongClickListener(this);

        this.views = new ViewAnimator(context);
        this.views.addView(new MenuWidget(context, this), 0, this.getViewParams());
        this.views.addView(new AddWidget(context, this, this), 1, this.getViewParams());
        this.views.addView(new RemoveWidget(context, this), 2, this.getViewParams());
        this.views.setDisplayedChild(0);
        this.views.setId(R.id.locationwidget_addwidget_id);

        final HashMap<String, Integer> view_indices = new HashMap<String, Integer>();
        view_indices.put("menu", Integer.valueOf(0));
        view_indices.put("add", Integer.valueOf(1));
        view_indices.put("remove", Integer.valueOf(2));
        this.view_indices = view_indices;

        RelativeLayout.LayoutParams listParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        listParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        listParams.addRule(RelativeLayout.ABOVE, R.id.locationwidget_addwidget_id);
        addView(this.listView, listParams);
        addView(this.views, this.getAddParams());
    }

    public void readLocations(){
        this.locations = new HashMap<String, String>();
        this.order = new LinkedList<String>();

        if (!Environment.getExternalStorageState().equals("mounted")) {
            return;
        }
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File subDir = new File(storageDir, "WeatherForecast");
        if (subDir.exists()) {
            File f = new File(subDir, "locations.txt");

            try{
                BufferedReader stream = new BufferedReader(new FileReader(f));
                for (;;){

                    String line = stream.readLine();
                    if (line == null) {
                        break;
                    }

                    String spec = line.trim();
                    String[] pieces = spec.split("/");
                    
                    if (pieces.length < 3) {
                        continue;
                    }

                    // Properly regenerate the name by converting underscores to
                    // spaces and stripping any trailing text that starts with a
                    // tilde.
                    String place = pieces[pieces.length - 1].replace("_", " ");

                    if (place.contains("~")) {
                        place = place.substring(0, place.indexOf("~"));
                    }
                    this.locations.put(place, spec);
                    this.order.add(place);
                }
                stream.close();
                return;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
    }

    public void writeLocations(){
        if (!Environment.getExternalStorageState().equals("mounted")) {
            return;
        }
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File subDir = new File(storageDir, "WeatherForecast");
        if (!subDir.exists()) {
            subDir.mkdirs();
        }
        if (subDir.exists()) {
            File f = new File(subDir, "locations.txt");

            try{
                FileWriter stream = new FileWriter(f);

                for (int i = 0; i < this.order.size(); i++) {
                    String key = this.order.get(i);
                    stream.write(this.locations.get(key) + "\n");
                }
                stream.flush();
                stream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public LocationAdapter getAdapter(){

        readLocations();

        final ArrayList<String> keys = new ArrayList<String>();

        for (int i = 0; i < this.order.size(); i++) {
            String location = this.order.get(i);
            keys.add(location);
        }

        return new LocationAdapter(keys);
    }

    public void onItemClick(AdapterView<?> parent, final View view, int position, long id){

        try{
            String location = (String)this.adapter.getItem((int)id);
            locationHandler.locationEntered(location, (String)this.locations.get(location));
        } catch (Exception e){
            throw new RuntimeException("Killed");
        }

        if (this.currentItem != -1) {
            enterAddMode();
        }
    }

    public void addLocation(String name, String spec){

        if (this.locations.containsKey(name)) {
            return;
        }

        this.locations.put(name, spec);
        this.order.add(name);

        this.adapter.add(name);
        this.listView.setAdapter(this.adapter);
        this.enterMenuMode();
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){

        if (!this.mode.equals("remove")){
            this.currentItem = position;
            enterRemoveMode();
        }

        return true;
    }

    public void removeLocation(){
        String place = (String)this.order.remove(this.currentItem);
        this.locations.remove(place);

        this.adapter.remove(place);
        this.listView.setAdapter(this.adapter);

        this.currentItem = -1;
        this.enterPreviousMode();
    }

    public void cancelRemove(){

        this.currentItem = -1;
        this.enterPreviousMode();
    }

    public void enterPreviousMode(){       
        final Integer n = (Integer) this.view_indices.get(this.previous_mode);
        if (n == null) {
            throw new RuntimeException();
        }
        this.views.setDisplayedChild(n.intValue());
        this.mode = this.previous_mode;
        
    }

    public void enterMenuMode() {
        this.views.setDisplayedChild(0);
        this.previous_mode = this.mode;
        this.mode = "menu";
    }

    public void enterAddMode() {
        this.views.setDisplayedChild(1);
        this.previous_mode = this.mode;
        this.mode = "add";
    }

    public void enterRemoveMode(){
        this.views.setDisplayedChild(2);
        this.previous_mode = this.mode;
        this.mode = "remove";
    }

    public void startConfiguration() {
        // This causes the LocationWidget to be hidden.
        this.configureHandler.startConfiguration();
    }

    public FrameLayout.LayoutParams getViewParams() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
    }

    public RelativeLayout.LayoutParams getAddParams(){

        RelativeLayout.LayoutParams addParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        addParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        return addParams;
    }

}
