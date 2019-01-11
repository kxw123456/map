package com.flag.git_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by acer on 2019/1/9.
 */

public class AddressToLatitudeLongitude {
    private String address = "哈尔滨";
    private double Latitude = 45.7732246332393;//纬度
    private double Longitude = 126.65771685544611;//经度

    public AddressToLatitudeLongitude(String addr_str) {
        this.address = addr_str;
    }

    public void getLatAndLngByAddress(){
        String addr = "";
        String lat = "";
        String lng = "";
        try {
            addr = java.net.URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = String.format("http://api.map.baidu.com/geocoder/v2/?"
                +"address=%s&ak=NUqQbHpRUUXn8PhhqVOSIEvsppsvMG0w&output=json",addr);
        URL myURL = null;
        URLConnection httpsConn = null;

        try {
            myURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            httpsConn = (URLConnection) myURL.openConnection();
            if (httpsConn != null) {
                InputStreamReader insr = new InputStreamReader(
                        httpsConn.getInputStream(), "UTF-8");
                BufferedReader br = new BufferedReader(insr);
                String data = null;
                if ((data = br.readLine()) !=null){
                    System.out.println(data);
                    lat = data.substring(data.indexOf("\"lat\":") + ("\"lat\":").length(), data.indexOf("},\"precise\""));
                    lng = data.substring(data.indexOf("\"lng\":") + ("\"lng\":").length(), data.indexOf(",\"lat\""));
                }
                insr.close();
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.Latitude = Double.parseDouble(lat);
        this.Longitude = Double.parseDouble(lng);
    }

    public Double getLatitude(){
        return this.Latitude;
    }

    public Double getLongitude(){
        return this.Longitude;
    }

}
