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

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class LocationAdapter extends BaseAdapter{
    public List<String> items;

    public LocationAdapter(List<String> strings){
        this.items = strings;
    }

    @Override
    public int getCount(){
        int i;
        if (this.items != null) {
            i = this.items.size();
        } else {
            i = 0;
        }
        return i;
    }

    @Override
    public Object getItem(int position){
      return this.items.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    public void add(String item) {
        this.items.add(item);
    }

    public void remove(String item) {
        this.items.remove(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        // If convertView is not null then reuse it.
        if (convertView != null) {
            return convertView;
        }

        TextView view = new TextView(parent.getContext());
        view.setText((String) this.items.get(position));
        view.setTextSize((float)(view.getTextSize() * 1.25D));
        return view;
    }

}

