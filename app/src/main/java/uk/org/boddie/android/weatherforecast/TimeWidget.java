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
import android.graphics.Typeface;
import android.view.Gravity;
import java.util.Calendar;
import java.util.Date;

public class TimeWidget extends AdjustableText{
    public TimeWidget(final Context context, final Forecast forecast, final Calendar calendar) {
        super(context, "time adjustment");

        String timeString = String.format("%02d:%02d:%02d - ", 
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));

        Date date = forecast.to_;
        calendar.setTime(date);

        timeString += String.format("%02d:%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));

        this.setText(timeString);

        this.setGravity(Gravity.CENTER);
        this.setTypeface(Typeface.create("", Typeface.BOLD));
        this.defaultSize = 3;
    }
}
