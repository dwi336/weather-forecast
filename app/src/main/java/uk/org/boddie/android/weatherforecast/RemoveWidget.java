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
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class RemoveWidget extends LinearLayout implements View.OnClickListener{
    private Button cancelButton;
    private RemoveLocationListener handler;
    private Button removeButton;

    public RemoveWidget(Context context) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);

        this.removeButton = new Button(context);
        this.removeButton.setText("Remove");
        this.removeButton.setOnClickListener(this);

        this.cancelButton = new Button(context);
        this.cancelButton.setText("Cancel");
        this.cancelButton.setOnClickListener(this);

        addWeightedView(this.removeButton, 1);
        addWeightedView(this.cancelButton, 1);
    }

    public RemoveWidget(Context context, AttributeSet attrs) {
        this(context);
    }

    public RemoveWidget(Context context, AttributeSet attrs, int defStyle) {
        this(context);
    }

    public void setRemoveLocationListener(RemoveLocationListener handler){
        this.handler = handler;
    }

    public void addWeightedView(View view, float weight){
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, weight));
        addView(view);
    }

    public void onClick(View view){

        if (view.equals(this.removeButton)) {
            this.handler.removeLocation();
        } else {
            this.handler.cancelRemove();
        }
    }
}

