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
import android.view.Gravity;
import java.util.Calendar;
import java.util.Locale;

public class DateWidget extends AdjustableText{
    public DateWidget(final Context context, final Calendar calendar, final int day) {
        super(context, "date adjustment");

        final int background = context.getResources().getColor(android.R.color.background_light);

        this.setText( 
                calendar.getDisplayName(Calendar.DAY_OF_WEEK,
                                        Calendar.LONG, 
                                        Locale.getDefault()) + 
                " " + day + " " + 
                calendar.getDisplayName(Calendar.MONTH,
                                        Calendar.LONG, 
                                        Locale.getDefault()) + 
                " " + calendar.get(Calendar.YEAR));

        this.setGravity(Gravity.CENTER);
        this.setTypeface(Typeface.create((String)null, Typeface.BOLD));
        this.setBackgroundColor(background);
        this.setTextColor(Color.BLACK);
        this.defaultSize = 3;
    }
}
