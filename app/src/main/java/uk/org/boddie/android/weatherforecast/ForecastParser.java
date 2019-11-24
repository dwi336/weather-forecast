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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ForecastParser{
    private HashMap<String, Integer> symbols;

    public ForecastParser(HashMap<String, Integer> hashMap) {
        this.symbols = hashMap;
    }

    public List<Forecast> parse(InputStream stream) throws XmlPullParserException, IOException{

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);

        int eventType = parser.getEventType();
        String section = "";
        String sections[] = {"location","credit","tabular"};

        // According to the specification, all times are local to the place:
        // https://hjelp.yr.no/hc/en-us/articles/360009342913-XML-specification-of-forecast-xml
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        String place = "";
        String credit = "";
        TimeZone timezone = TimeZone.getDefault();
        ArrayList<Forecast> forecasts = new ArrayList<Forecast>();
        Forecast forecast = new Forecast();

        while ( eventType != XmlPullParser.END_DOCUMENT ){

            eventType = parser.next();

            if (eventType == XmlPullParser.START_TAG){

                String name = parser.getName();

                boolean found = false;
                for (int element = 0 ; element < sections.length; element++) {
                    if ( name.equals(sections[element]) ){
                        found = true;
                    }
                }

                if (found == true){
                     section = name;
                } else if (!section.equals("")){

                    if (name.equals("name")){
                        while (eventType != XmlPullParser.TEXT) {
                            eventType = parser.next();
                        }

                        place = parser.getText();

                    } else if (name.equals("timezone")) {
                        timezone = TimeZone.getTimeZone(parser.getAttributeValue(null, "id"));

                    } else if (name.equals("link")) {
                        credit = parser.getAttributeValue(null, "text");

                    } else if (name.equals("time")) {

                        forecast = new Forecast();
                        forecast.place = place;
                        forecast.credit = credit;

                        String from_ = parser.getAttributeValue(null, "from");
                        String to_ = parser.getAttributeValue(null, "to");

                        forecast.from_ = dateFormat.parse(from_, new ParsePosition(0));
                        forecast.to_ = dateFormat.parse(to_, new ParsePosition(0));

                    } else if (name.equals("symbol")) {

                        forecast.description = parser.getAttributeValue(null, "name");
                        String symbol = parser.getAttributeValue(null, "var");

                        forecast.midDate = new Date(forecast.from_.getTime() / 2 + forecast.to_.getTime() / 2);

                        try{
                            forecast.symbol = (this.symbols.get(symbol)).intValue();
                        } catch (Exception e) {
                            forecast.symbol = -1;
                        }

                  } else if (name.equals("windSpeed")) {
                      forecast.windSpeed = parser.getAttributeValue(null, "name");

                  } else if (name.equals("temperature")) {
                      forecast.temperature = parser.getAttributeValue(null, "value");
                      forecast.temperatureUnit = parser.getAttributeValue(null, "unit");

                  }
              }
          } else if (eventType == XmlPullParser.END_TAG) {

              String name = parser.getName();

              boolean found = false;
              for (int element = 0 ; element < sections.length; element++) {
                  if ( name.equals(sections[element]) ){
                      found = true;
                  }
              }

              if ( name.equals(section) && (found == true) ) {
                  section = "";
              } else if (section.equals("tabular") && name.equals("time")) {
                  forecasts.add(forecast);
              }
          }
      }
      return forecasts;
    }
}

