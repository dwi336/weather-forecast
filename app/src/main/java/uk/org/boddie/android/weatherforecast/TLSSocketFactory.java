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

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TLSSocketFactory extends SSLSocketFactory{

    private SSLContext context;
    private SSLSocketFactory _socketFactory;

    public TLSSocketFactory(){
        super();

        try {
            this.context = SSLContext.getInstance("TLSv1.2");
            this.context.init(null, null, null);
            this._socketFactory = SSLContext.getDefault().getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    SSLSocket useTLS(SSLSocket socket) {
        Log.i("DUCK", "UseTLS");
        
        String[] protocols = socket.getEnabledProtocols();
        for (int i=0; i < protocols.length; i++) {
            Log.i("DUCK", protocols[i]);
        }

        socket.setEnabledProtocols(new String[] { "TLSv1.2" } );
        return socket;
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws UnknownHostException, IOException {
        Log.i("DUCK", "createSocket String, int, InetAddress, int");
        SSLSocket socket = (SSLSocket) this._socketFactory.createSocket(host, port, localHost, localPort);

        return this.useTLS(socket);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        Log.i("DUCK", "createSocket InetAddress, int, InetAddress, int");
        SSLSocket socket = (SSLSocket) this._socketFactory.createSocket(address, port, localAddress, localPort);

        return this.useTLS(socket);
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        Log.i("DUCK", "createSocket InetAddress, int");
        SSLSocket socket = (SSLSocket) this._socketFactory.createSocket(host, port);

        return this.useTLS(socket);
    }

    public Socket createSocket(String host, int port) throws UnknownHostException, IOException {
        Log.i("DUCK", "createSocket String, int");
        SSLSocket socket = (SSLSocket) this._socketFactory.createSocket(host, port);

        return this.useTLS(socket);
    }

    public Socket createSocket() throws IOException {
        Log.i("DUCK", "createSocket");
        SSLSocket socket = (SSLSocket) this._socketFactory.createSocket();

        return this.useTLS(socket);
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        Log.i("DUCK", "createSocket Socket, String, int, bool");
        SSLSocket socket = (SSLSocket) this._socketFactory.createSocket(s, host, port, autoClose);

        return this.useTLS(socket);
    }

    public String[] getDefaultCipherSuites() {
        Log.i("DUCK", "getDefaultCipherSuites");
        return this._socketFactory.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        Log.i("DUCK", "getSupportedCipherSuites");
        return this._socketFactory.getSupportedCipherSuites();
    }

}
