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
import android.content.SharedPreferences;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import uk.org.boddie.android.weatherforecast.R;

public class ForecastWidget extends RelativeLayout{
    private TextView creditLabel;
    private LinearLayout forecastLayout;
    private List<Forecast> forecasts;
    private Header header;
    private String place_name = "";
    private ScrollView scrollView;

    @SuppressWarnings("deprecation")
    public ForecastWidget(Context context){

        super(context);

        this.forecasts = null;

        int background = 0;
        // This getColor call deprecated in API level 23.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            background = context.getColor(android.R.color.background_light);
        } else {
            background = context.getResources().getColor(android.R.color.background_light);
        }

        // Header
        this.header = new Header(context);

        View headerLine = new View(context);
        headerLine.setBackgroundColor(background);
        LinearLayout.LayoutParams headerLineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1); // 1 pixel in height

        LinearLayout headerLayout = new LinearLayout(context);
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setId(R.id.forecastwidget_header_id);

        headerLayout.addView(this.header);
        headerLayout.addView(headerLine, headerLineParams);

        // Middle - containing the forecast layout
        this.scrollView = new ScrollView(context);
        this.scrollView.setId(R.id.forecastwidget_scrollView_id);

        // Footer
        LinearLayout footer = new LinearLayout(context);
        footer.setOrientation(LinearLayout.VERTICAL);
        footer.setId(R.id.forecastwidget_footer_id);

        View footerLine = new View(context);
        footerLine.setBackgroundColor(background);
        LinearLayout.LayoutParams footerLineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1); // 1 pixel in height

        this.creditLabel = new TextView(context);
        this.creditLabel.setGravity(Gravity.CENTER);
        
        footer.addView(footerLine, footerLineParams);
        footer.addView(this.creditLabel);

        // The forecast layout
        this.forecastLayout = new LinearLayout(context);
        this.forecastLayout.setOrientation(LinearLayout.VERTICAL);
        this.scrollView.addView(this.forecastLayout);

        // Layout parameters
        RelativeLayout.LayoutParams headerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        headerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        RelativeLayout.LayoutParams scrollParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scrollParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        scrollParams.addRule(RelativeLayout.BELOW, R.id.forecastwidget_header_id);
        scrollParams.addRule(RelativeLayout.ABOVE, R.id.forecastwidget_footer_id);

        RelativeLayout.LayoutParams footerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        footerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        addView(headerLayout, headerParams);
        addView(this.scrollView, scrollParams);
        addView(footer, footerParams);    
    }

    public void addForecasts(String place_name, List<Forecast> forecasts, final SharedPreferences preferences) {

        this.forecastLayout.removeAllViews();
        this.scrollView.scrollTo(0, 0);

        if ( (forecasts == null) || (forecasts.size() == 0) ) {
            return;
        }

        this.header.setText(place_name);
        this.creditLabel.setText("Data from MET Norway");

        Date firstDate = forecasts.get(0).date;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(firstDate);

        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        Context context = getContext();

        Forecast forecast;
        for (int i=0; i < forecasts.size(); i++) {
            forecast = forecasts.get(i);

            // Get the day of the month.
            Date date = forecast.date;
            calendar.setTime(date);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Add an item for the date for the first item and any item
            // following a day change.
            if ( (date == firstDate) || (day != currentDay) ) {
                DateWidget dateWidget = new DateWidget(context, calendar, day);
                dateWidget.restore(preferences);
                this.forecastLayout.addView(dateWidget, this.rowLayout());
            }
            currentDay = day;

            // Time
            TimeWidget timeWidget = new TimeWidget(context, forecast, calendar);
            timeWidget.restore(preferences);
            this.forecastLayout.addView(timeWidget, this.rowLayout());

            // Symbol, temperature, description and wind
            SymbolWidget symbolWidget = new SymbolWidget(context, forecast);
            symbolWidget.getTempWidget().restore(preferences);
            symbolWidget.getWindWidget().restore(preferences);
            this.forecastLayout.addView(symbolWidget, this.rowLayout());
        }
        this.place_name = place_name;
        this.forecasts = forecasts;
    }

    public void restore(final SharedPreferences preferences) {
        this.header.restore(preferences);
        if (this.forecasts != null) {
            this.addForecasts(this.place_name, this.forecasts, preferences);
        }
    }

    private LinearLayout.LayoutParams rowLayout() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
