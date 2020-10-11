/*
 * Copyright (C) 2017 David Boddie <david@boddie.org.uk>
 * Copyright (C) 2020 Dietmar Wippig <dwi336.dev@gmail.com>
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ForecastParser{
    private HashMap<String, Integer> symbols;

    public ForecastParser(HashMap<String, Integer> hashMap) {
        this.symbols = hashMap;
    }

    public List<Forecast> parse(InputStream stream) throws IOException, JSONException{

        // https://stackoverflow.com/questions/309424/how-do-i-read-convert-an-inputstream-into-a-string-in-java
        final ByteArrayOutputStream buf_out = new ByteArrayOutputStream();
        while (true) {
            final int b = stream.read();
            if (b == -1) {
                break;
            }
            buf_out.write(b);
        }

        final String json_text = buf_out.toString("UTF-8");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        final ArrayList<Forecast> forecasts = new ArrayList<Forecast>();

        final JSONObject obj = ((JSONObject)new JSONTokener(json_text).nextValue());
        final JSONObject properties = obj.getJSONObject("properties");
        final JSONObject units = properties.getJSONObject("meta").getJSONObject("units");
        final JSONArray timeseries = properties.getJSONArray("timeseries");

        int t = 0;
        while ( t < timeseries.length() ){   
            final JSONObject f = timeseries.getJSONObject(t);
            final Forecast forecast = new Forecast();
            final String time_text = f.getString("time");
            forecast.date = dateFormat.parse(time_text, new ParsePosition(0));
            final JSONObject data = f.getJSONObject("data");

            if (data.has("instant")) {
                final JSONObject instant = data.getJSONObject("instant");
                final JSONObject details = instant.getJSONObject("details");
                forecast.windSpeed = details.getString("wind_speed");
                forecast.windUnit = units.getString("wind_speed");
                forecast.temperature = details.getString("air_temperature");
                forecast.temperatureUnit = units.getString("air_temperature");
            }
            JSONObject summary;
            if (data.has("next_1_hours")) {
                summary = data.getJSONObject("next_1_hours").getJSONObject("summary");
            } else if (data.has("next_6_hours")) {
                summary = data.getJSONObject("next_6_hours").getJSONObject("summary");
            } else if (data.has("next_12_hours")) {
                summary = data.getJSONObject("next_12_hours").getJSONObject("summary");
            } else {
                summary = null;
                forecast.symbol = -1;
            }
            if (summary != null) {
                final String symbol = summary.getString("symbol_code");
                try {
                    final Integer n = this.symbols.get(symbol);
                    if (n == null) {
                        throw new Exception();
                    }
                    forecast.symbol = n.intValue();
                }
                catch (Exception e) {
                    forecast.symbol = -1;
                }
            }
            forecasts.add(forecast);
            t++;
        }
        return forecasts;
    }
}

