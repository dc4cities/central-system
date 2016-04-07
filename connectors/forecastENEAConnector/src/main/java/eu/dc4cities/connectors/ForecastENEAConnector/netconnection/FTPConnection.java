/*
 * Copyright 2016 The DC4Cities author.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.dc4cities.connectors.ForecastENEAConnector.netconnection;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;


/**
 *
 */
public class FTPConnection extends NetConnection {
    private FTPClient ftpClient;

    private String host;
    private String usr;
    private String psw;

    private String workDir;


    public FTPConnection() {
        this.ftpClient = new FTPClient();
    }

    @Override
    public boolean connect(String host) {
        this.host = host;
        try {
            System.out.print("Connecting to the server " + host + "... ");
            this.ftpClient.connect(host);
            System.out.print("DONE\n\n");
            return true;
        } catch (IOException e) {
            System.out.print("FAIL\n\n");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean login(String usr, String psw) {
        this.usr = usr;
        this.psw = psw;
        try {
            System.out.print("User " + usr + " logging in... ");
            this.ftpClient.login(usr, psw);
            this.workDir = this.ftpClient.printWorkingDirectory();
            System.out.print("DONE\n\n");
            return true;
        } catch (IOException e) {
            System.out.print("FAIL\n\n");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean setWorkingDir(String remotePath) {
        try {
            System.out.print("Changing working dir... ");
            this.ftpClient.changeWorkingDirectory(remotePath);
            this.workDir = this.ftpClient.printWorkingDirectory();
            System.out.print("DONE\n\n");
            return true;
        } catch (IOException e) {
            System.out.print("FAIL\n\n");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getWorkingDir() {
        return this.workDir;
    }

    @Override
    public boolean logout() {
        try {
            System.out.print("User " + this.usr + " logging out... ");
            this.ftpClient.logout();
            System.out.print("DONE\n\n");
            return true;
        } catch (IOException e) {
            System.out.print("FAIL\n\n");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean disconnect() {
        try {
            System.out.print("Disconnecting from server " + this.host + "... ");
            this.ftpClient.disconnect();
            System.out.print("DONE\n\n");
            return true;
        } catch (IOException e) {
            System.out.print("FAIL\n\n");
            e.printStackTrace();
            return false;
        }
    }

    public FTPFile[] getFTPFileNames() {
        try {
            //System.out.print("Retreiving file list... ");
            FTPFile[] files = this.ftpClient.listFiles();
            System.out.println("originali:" + files.length);
            int index = 0;
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    /*
                    if(c==null)
                        c = files[i].getTimestamp();
                    else if(c.getTimeInMillis() < files[i].getTimestamp().getTimeInMillis())
                    {
                        c = files[i].getTimestamp();
                        index = i;
                    }
                            */
                    System.out.println(files[i].toString());
                    index++;
                }

            }
            System.out.println("filtrati: " + index);
            return files;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
