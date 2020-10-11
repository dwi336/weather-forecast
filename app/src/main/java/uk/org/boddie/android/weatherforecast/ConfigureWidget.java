/*
 * Copyright (C) 2019 David Boddie <david@boddie.org.uk>
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

import java.util.Calendar;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

public class ConfigureWidget extends RelativeLayout{
    private int background;
    private SizeControl dateControl;
    private DateWidget dateWidget;
    private Header header;
    private SizeControl headerControl;
    private ConfigureListener listener;
    private LinearLayout middleLayout;
    private SymbolWidget symbolWidget;
    private HashMap<String, Integer> symbols;
    private SizeControl tempControl;
    private SizeControl timeControl;
    private TimeWidget timeWidget;
    public SizeControl windControl;

    public ConfigureWidget(final Context context, final HashMap<String, Integer> symbols, final ConfigureListener listener) {
        super(context);

        this.symbols = symbols;
        this.listener = listener;

        this.background = context.getResources().getColor(android.R.color.background_light);
        final View headerLine = new View(context);
        headerLine.setBackgroundColor(this.background);
        final LinearLayout.LayoutParams headerLineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1); // 1 pixel in height

        final LinearLayout headerLayout = new LinearLayout(context);
        headerLayout.setOrientation(LinearLayout.VERTICAL);

        headerLayout.setId(R.id.configurewidget_headerlayout_id);
        headerLayout.addView(headerLine, headerLineParams);

        // Middle

        this.middleLayout = new LinearLayout(context);
        this.middleLayout.setOrientation(LinearLayout.VERTICAL); 
        this.middleLayout.setId(R.id.configurewidget_middlelayout_id);
        this.updateSample();

        // Overall footer layout

        // Add controls to allow the sizes of the labels and symbol to be
        // customised.
        final ScrollView scrollView = new ScrollView(context);
        final LinearLayout scrollLayout = new LinearLayout(context);
        scrollLayout.setOrientation(LinearLayout.VERTICAL); 

        this.headerControl = this.addSizeControl("Place Name", null, scrollLayout);
        this.dateControl = this.addSizeControl("Date", null, scrollLayout);
        this.timeControl = this.addSizeControl("Time", null, scrollLayout);
        this.tempControl = this.addSizeControl("Temperature", null, scrollLayout);
        this.windControl = this.addSizeControl("Wind speed", null, scrollLayout);
        this.updateControls();
        scrollView.addView(scrollLayout);
        scrollView.setId(R.id.configurewidget_scrollview_id);

        // Layout parameters
        final RelativeLayout.LayoutParams headerParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        headerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        headerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        final RelativeLayout.LayoutParams middleParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        middleParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        middleParams.addRule(RelativeLayout.BELOW, R.id.configurewidget_headerlayout_id);

        final RelativeLayout.LayoutParams footerParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        footerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        footerParams.addRule(RelativeLayout.BELOW, R.id.configurewidget_middlelayout_id);

        this.addView(headerLayout, headerParams);
        this.addView(this.middleLayout, middleParams);
        this.addView(scrollView, footerParams);
    }

    public SizeControl addSizeControl(final String text, final Adjustable adjustable, final LinearLayout layout) {
        final TextView textView = new TextView(this.getContext());
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);

        final SizeControl sizeControl = new SizeControl(this.getContext(), textView.getTextSize(), adjustable, this);
        layout.addView(textView);
        layout.addView(sizeControl);

        return sizeControl;
    }

    public void restore(final SharedPreferences preferences) {
        this.header.restore(preferences);
        this.dateWidget.restore(preferences);
        this.timeWidget.restore(preferences);
        this.symbolWidget.getTempWidget().restore(preferences);
        this.symbolWidget.getWindWidget().restore(preferences);        
    }

    public void save(final SharedPreferences.Editor editor) {
        this.header.save(editor);
        this.dateWidget.save(editor);
        this.timeWidget.save(editor);
        this.symbolWidget.getTempWidget().save(editor);
        this.symbolWidget.getWindWidget().save(editor);
    }

    public void updateConfiguration() {
        // Save the preferences, recreate the sample forecast and the controls
        // for its widgets, then apply the preferences to the widgets.
        this.listener.savePreferences();
        this.updateSample();
        this.updateControls();
        this.listener.restorePreferences();
    }

    public void updateControls() {
        this.headerControl.setAdjustable(this.header);
        this.dateControl.setAdjustable(this.dateWidget);
        this.timeControl.setAdjustable(this.timeWidget);
        this.tempControl.setAdjustable(this.symbolWidget.getTempWidget());
        this.windControl.setAdjustable(this.symbolWidget.getWindWidget());
    }

    public void updateSample() {
        final Context context = this.getContext();

        // Include a place name header, date, time and a forecast to allow the
        // user to configure the appearance of the forecast view.

        this.header = new Header(context);
        this.header.setText("Place Name");

        final Calendar calendar = Calendar.getInstance();
        final int value = calendar.get(Calendar.DAY_OF_MONTH);

        final Forecast forecast = new Forecast();
        forecast.date = calendar.getTime();
        final Integer n = (Integer) this.symbols.get("rain");
        if (n == null) {
            throw new RuntimeException();
        }
        forecast.symbol = n.intValue();
        forecast.temperatureUnit = "celsius";
        forecast.temperature = "12";
        forecast.windSpeed = "3";
        forecast.windUnit = "m/s";

        this.dateWidget = new DateWidget(context, calendar, value);
        this.timeWidget = new TimeWidget(context, forecast, calendar);
        this.symbolWidget = new SymbolWidget(context, forecast);

        final LinearLayout.LayoutParams centreParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        final View footerLine = new View(context);
        footerLine.setBackgroundColor(this.background);
        final LinearLayout.LayoutParams footerLineParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 1); // 1 pixel in height

        this.middleLayout.removeAllViews();
        this.middleLayout.addView(new Space(context), 0, 12);
        this.middleLayout.addView(this.header, centreParams);
        this.middleLayout.addView(this.dateWidget, centreParams);
        this.middleLayout.addView(this.timeWidget, centreParams);
        this.middleLayout.addView(this.symbolWidget, centreParams);
        this.middleLayout.addView(new Space(context), 0, 12);
        this.middleLayout.addView(footerLine, footerLineParams);
        this.middleLayout.addView(new Space(context), 0, 12);
    }
}
