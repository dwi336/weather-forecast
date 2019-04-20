# Weather Forecast

This application displays weather forecasts from the yr.no service.
It is a Java port of the original Serpentine implementation, which 
is published under

https://bitbucket.org/dboddie/weather-forecast-android

![A screenshot of the application](docs/Screenshot_2019-04-20-09-55-49.png)

** The yr.no service tries to build up secure connections with TLS 1.2
without any lower version down grading. On older Android devices,
running versions earlier than Android 5 (Lollipop) the application
will switch to open HTTP communication, if TLS 1.2 would not have
been supported.**

## Usage 

### Adding, removing and selecting locations

The application is not configured to use any default locations, but a list of
preset locations are supplied. Select the text field at the bottom of the
screen, start entering a place name, and suggestions for the location will be
presented. You can optionally select one of the suggestions. Press the Add
button to add the location to the list. Since the list of known locations
is limited, your preferred location may not be available; see below for a
workaround for this problem.

To remove a location from the list, press and hold the relevant item in the
list until the Remove and Cancel buttons appear below the list. Press Remove to
remove the item or Cancel to keep it.

To view a forecast for a location, tap the relevant item. If a connection to
the yr.no server can be established, the location list will be hidden and the
forecast will be shown. Otherwise, a "No connection" message will be displayed.

### Adding custom locations

The app contains a list of known locations which I obtained from the yr.no
service, but yr.no itself knows about many more places than these. If your
preferred location is not recognised by the application but can be found at
yr.no then the following workaround can be used to add it to your list of
locations.

The app stores its locations in a file in the external storage of your device.
If you use a file browser on your phone, you can find a file called
`locations.txt` in the `Download/WeatherForecast` directory. For example, it
might be found in the following location:

  SD Card
    Download
      WeatherForecast
        locations.txt

The corresponding path is `/sdcard/Download/WeatherForecast/locations.txt` if
you access it via a shell.

The file contains a list of locations which are taken from parts of the URLs
you can visit at yr.no. For example, the forecast for Cairo can be found at
this URL:

  https://www.yr.no/place/Egypt/Cairo/Cairo/

The `locations.txt` file contains a location on each line. The part of the
above URL that should be included on a line in the `locations.txt` file is
this:

  Egypt/Cairo/Cairo

Each line of the file contains an entry like this, with names of countries,
regions and cities separated by slashes. Some locations are specified using
more than three parts, particularly those in Norway.

Update the file to include the custom locations you want and restart the
Weather Forecast application. The new locations should now be available.

## Building 

The development is done with Eclipse and andmore-Plugin. A direct gradle build is also supported.

### API Reference

Mininum SDK: 14
Target SDK: 28

## License

The source code of Weather Forecast is licensed under the GPLv3.

Copyright (C) 2017 David Boddie <david@boddie.org.uk>
Copyright (C) 2019 Dietmar Wippig <dwi336.dev@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


The icons are licensed under The MIT License (MIT):

Copyright (c) 2015-2017 Yr

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


The place names included with the application are licensed under the terms of the Creative Commons
Attribution 4.0 International (CC BY 4.0) license [CC BY 4.0] (http://creativecommons.org/licenses/by/4.0/)
In addition to them the app uses icons from [Google Design Material Icons](https://design.google.com/icons/index.html) licensed under Apache License Version 2.0.


The weather forecasts are obtained from the service at yr.no and the
application tries to follow the terms, conditions and guidelines for use of
that service:

http://om.yr.no/verdata/vilkar/

