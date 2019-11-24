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
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import java.util.HashMap;

import uk.org.boddie.android.weatherforecast.R;

public class AddWidget extends LinearLayout implements View.OnClickListener, AdapterView.OnItemClickListener{
    private ArrayAdapter<String> adapter;
    private Button cancelButton;
    private ModeHandler handler;
    private AddLocationListener listener;
    private AutoCompleteTextView locationEdit;
    private HashMap<String, String> places;

    public AddWidget(final Context context, final AddLocationListener listener, final ModeHandler handler) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
        this.listener = listener;
        this.handler = handler;

        // Read the lists of place names and place specifications from the
        // application's resources, creating a dictionary from the two lists.
        this.places = new HashMap<String, String>();
        Resources resources = context.getResources();
        String[] place_names = resources.getStringArray(R.array.place_names);
        String[] places = resources.getStringArray(R.array.places);

        int j = Math.min(place_names.length, places.length);
        for (int i = 0; i < j; i++){
            this.places.put(place_names[i], places[i]);
        }

        // Use a specialised adapter to provide filtered lists of data for an
        // auto-complete-enabled text view.
        this.adapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, place_names);

        this.locationEdit = new AutoCompleteTextView(context);
        this.locationEdit.setAdapter(this.adapter);
        this.locationEdit.setOnItemClickListener(this);

        this.cancelButton = new Button(context);
        this.cancelButton.setText("X");
        this.cancelButton.setOnClickListener(this);

        addWeightedView(this.locationEdit, 2);
        addWeightedView(this.cancelButton, 0);
    }

    public void addWeightedView(View view, float weight){
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, weight));
        addView(view);
    }

    @Override
    public void onClick(View view) {
        if (view == this.cancelButton) {
            this.handler.enterMenuMode();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String text = this.adapter.getItem(position);
        this.addLocation(text);
        this.locationEdit.clearFocus();
    }

    public void addLocation(String text) {
        String name = text.trim();
        String spec;
        try{
            spec = (String)this.places.get(name);

            // Remove the country from the name.
            name = name.substring(0,name.indexOf(", "));

        } catch (Exception e) {

            // Replace spaces with underscores.
            spec = name.replace(" ", "_");

            // Split the name into pieces to check whether the user specified a
            // specifier directly.
            String[] pieces = name.split("/");
            if (pieces.length < 3) {
                return;
            }

            name = pieces[(pieces.length - 1)];

            // Potentially validate the specifier by requesting a page from
            // yr.no.
        }
        this.listener.addLocation(name, spec);
        this.locationEdit.setText("");
    }
}
