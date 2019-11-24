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

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class SizeControl extends LinearLayout implements View.OnClickListener{
    private Adjustable adjustable;
    private ConfigureWidget configWidget;

    public SizeControl(final Context context, final float baseSize, final Adjustable adjustable, final ConfigureWidget configWidget) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);

        this.setGravity(Gravity.CENTER);

        this.adjustable = adjustable;
        this.configWidget = configWidget;

        final double step = baseSize / 4.0;
        for (int i=2; i<7; i++) {
            final Button button = new Button(context);
            button.setTextSize((float)(i * step));
            button.setText("A");
            // Use tag to store information about the font size associated
            // with the label.
            button.setTag(i);
            button.setOnClickListener(this);
            this.addView(button);
        }
    }

    public void onClick(final View view) {
        this.adjustable.setSize((Integer)view.getTag());
        this.configWidget.updateConfiguration();
    }

    public void setAdjustable(final Adjustable adjustable) {
        this.adjustable = adjustable;
    }
}
