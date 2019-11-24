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
import android.view.Gravity;
import android.widget.TextView;

public class AdjustableText extends TextView implements Adjustable{
    protected int defaultSize;
    private float originalTextSize;
    private String preferenceString;
    private int size;

    public AdjustableText(final Context context, final String preferenceString) {
        super(context);
        this.setGravity(Gravity.CENTER);
        this.preferenceString = preferenceString;
        this.originalTextSize = this.getTextSize();
    }

    public int getSize() {
        return this.size;
    }

    public void restore(final SharedPreferences sharedPreferences) {
        this.setSize(sharedPreferences.getInt(this.preferenceString, this.defaultSize));
    }

    public void save(final SharedPreferences.Editor sharedPreferencesEditor) {
        sharedPreferencesEditor.putInt(this.preferenceString, this.size);
    }

    public void setSize(final int size) {
        this.size = size;
        this.setTextSize((float)Math.max(0, (long)(this.originalTextSize / 4.0 * this.size)));
    }
}
