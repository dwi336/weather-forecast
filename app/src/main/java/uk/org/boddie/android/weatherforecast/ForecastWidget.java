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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uk.org.boddie.android.weatherforecast.R;

public class ForecastWidget extends RelativeLayout{
    private TextView creditLabel;
    private int darkText;
    private LinearLayout forecastLayout;
    private int lightBackground;
    private TextView placeLabel;
    private ScrollView scrollView;

    @SuppressWarnings("deprecation")
    public ForecastWidget(Context context){

        super(context);

        // This getColor call deprecated in API level 23.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.lightBackground = context.getColor(android.R.color.background_light);
        } else {
            this.lightBackground = context.getResources().getColor(android.R.color.background_light);
        }
        this.darkText = Color.BLACK;

        // Header
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setId(R.id.forecastwidget_header_id);

        this.placeLabel = new TextView(context);
        this.placeLabel.setTextSize((float)(this.placeLabel.getTextSize() * 1.5D));
        this.placeLabel.setGravity(Gravity.CENTER);

        View headerLine = new View(context);
        headerLine.setBackgroundColor(this.lightBackground);
        LinearLayout.LayoutParams headerLineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1); // 1 pixel in height

        header.addView(this.placeLabel);
        header.addView(headerLine, headerLineParams);

        // Middle - containing the forecast layout
        this.scrollView = new ScrollView(context);
        this.scrollView.setId(R.id.forecastwidget_scrollView_id);

        // Footer
        LinearLayout footer = new LinearLayout(context);
        footer.setOrientation(LinearLayout.VERTICAL);
        footer.setId(R.id.forecastwidget_footer_id);

        View footerLine = new View(context);
        footerLine.setBackgroundColor(this.lightBackground);
        LinearLayout.LayoutParams footerLineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1); // 1 pixel in height

        this.creditLabel = new TextView(context);

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

        addView(header, headerParams);
        addView(this.scrollView, scrollParams);
        addView(footer, footerParams);    
    }

    public void addForecasts(List<Forecast> forecasts) {

        this.forecastLayout.removeAllViews();
        this.scrollView.scrollTo(0, 0);
        
        if ( (forecasts == null) || (forecasts.size() == 0) ) {
            return;
        }

        this.placeLabel.setText(forecasts.get(0).place);
        this.creditLabel.setText(forecasts.get(0).credit);

        Date firstDate = forecasts.get(0).from_;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(firstDate);

        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        Context context = getContext();

        Forecast forecast;
        for (int i=0; i < forecasts.size(); i++) {
            forecast = forecasts.get(i);

            //             Date
            // Temperature Symbol Description
            //                    Wind

            // Get the day of the month.
            Date date = forecast.from_;
            calendar.setTime(date);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Add an item for the date for the first item and any item
            // following a day change.
            if ( (date == firstDate) || (day != currentDay) ) {

                TextView dateView = new TextView(context);
                dateView.setText( 
                    calendar.getDisplayName(Calendar.DAY_OF_WEEK,
                                            Calendar.LONG, 
                                            Locale.getDefault()) + 
                    " " + day + " " + 
                    calendar.getDisplayName(Calendar.MONTH,
                                            Calendar.LONG, 
                                            Locale.getDefault()) + 
                    " " + calendar.get(Calendar.YEAR));

                dateView.setGravity(Gravity.CENTER);
                dateView.setTypeface(Typeface.create("", Typeface.BOLD));
                dateView.setBackgroundColor(this.lightBackground);
                dateView.setTextColor(Color.BLACK);

                this.forecastLayout.addView(dateView, this.rowLayout());

            }
            currentDay = day;

            // Time
            String timeString = String.format("%02d:%02d:%02d - ",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));

            date = forecast.to_;
            calendar.setTime(date);

            timeString += String.format("%02d:%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));

            TextView timeView = new TextView(context);
            timeView.setText(timeString);

            timeView.setGravity(Gravity.CENTER);
            timeView.setTypeface(Typeface.create("", Typeface.BOLD));

            this.forecastLayout.addView(timeView, this.rowLayout());

            // Symbol, temperature, description and wind
            RelativeLayout row = new RelativeLayout(context);

            // Symbol
            RelativeLayout.LayoutParams lp = this.itemLayout();
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);

            if (forecast.symbol != -1) {
                ImageView imageView = new ImageView(context);
                imageView.setImageResource(forecast.symbol);
                row.addView(imageView, lp);	
            } else {
                Space spacer = new Space(context);
                row.addView(spacer, lp);
            }

            // Temperature
            TextView tempView = new TextView(context);
            tempView.setTextSize(tempView.getTextSize() * 2);

            if ( forecast.temperatureUnit.equals("celsius") ) {
                tempView.setText(forecast.temperature + "℃");
            } else {
                tempView.setText(forecast.temperature + " " + forecast.temperatureUnit);
            }

            lp = this.itemLayout();
            lp.addRule(RelativeLayout.CENTER_VERTICAL);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            row.addView(tempView, lp);

            // Description and wind speed
            LinearLayout descLayout = new LinearLayout(context);
            descLayout.setOrientation(LinearLayout.VERTICAL);

            TextView descView = new TextView(context);
            descView.setText(forecast.description);
            descLayout.addView(descView, lp);

            TextView windView = new TextView(context);
            windView.setText(forecast.windSpeed);
            descLayout.addView(windView, lp);

            lp = this.itemLayout();
            lp.addRule(RelativeLayout.CENTER_VERTICAL);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            row.addView(descLayout, lp);

            this.forecastLayout.addView(row, this.rowLayout());

        }
    }

    private LinearLayout.LayoutParams rowLayout() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private RelativeLayout.LayoutParams itemLayout() {
        return new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

}
