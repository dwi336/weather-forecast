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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

public class SymbolWidget extends RelativeLayout{
    private TemperatureWidget tempWidget;

    public SymbolWidget(final Context context, final Forecast forecast) {
        super(context);

        // Temperature Symbol Description
        //                    Wind

        // Symbol
        final RelativeLayout.LayoutParams itemLayout = this.itemLayout();
        itemLayout.addRule(RelativeLayout.CENTER_IN_PARENT);        

        if (forecast.symbol != -1){
            final ImageView imageView = new ImageView(context);
            imageView.setImageResource(forecast.symbol);
            this.addView(imageView, itemLayout);
        } else {
            Space spacer = new Space(context);
            this.addView(spacer, itemLayout);
        }

        // Temperature
        this.tempWidget = new TemperatureWidget(context);

        if ( forecast.temperatureUnit.equals("celsius") ) {
            this.tempWidget.setText(forecast.temperature + "℃");
        } else {
            this.tempWidget.setText(forecast.temperature + " " + forecast.temperatureUnit);
        }

        final RelativeLayout.LayoutParams itemLayout2 = this.itemLayout();
        itemLayout2.addRule(RelativeLayout.CENTER_VERTICAL);
        itemLayout2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        this.addView(this.tempWidget, itemLayout2);

        // Description and wind speed
        final LinearLayout descLayout = new LinearLayout(context);
        descLayout.setOrientation(LinearLayout.VERTICAL);

        final TextView descView = new TextView(context);
        descView.setText(forecast.description);
        descLayout.addView(descView, itemLayout2);

        final TextView windView = new TextView(context);
        windView.setText((CharSequence)forecast.windSpeed);
        descLayout.addView(windView, itemLayout2);

        final RelativeLayout.LayoutParams itemLayout3 = this.itemLayout();
        itemLayout3.addRule(RelativeLayout.CENTER_VERTICAL);
        itemLayout3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        this.addView(descLayout, itemLayout3);
    }

    public TemperatureWidget getTempWidget() {
        return this.tempWidget;
    }

    public RelativeLayout.LayoutParams itemLayout() {
        return new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
