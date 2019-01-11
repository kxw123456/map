package com.flag.git_test;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {
    private MapView myMapView = null;
    private BaiduMap myBaiduMap;
    private LocationClient mylocationClient;
    private MylocationListener mylistener;
    private Context context;

    private double myLatitude;
    private double myLongitude;
    private float myCurrentX;

    private BitmapDescriptor myIconLocation1;

    private MyOrientationListener myOrientationListener;

    private MyLocationConfiguration.LocationMode locationMode;

    private LinearLayout myLinearLayout1;
    private LinearLayout myLinearLayout2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        this.context = this;
        initView();
        initLoaction();
    }

    private void initView(){
        myMapView = (MapView) findViewById(R.id.baiduMapView);

        myBaiduMap = myMapView.getMap();

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);
        myBaiduMap.setMapStatus(msu);
    }

    private void initLoaction(){
        locationMode = MyLocationConfiguration.LocationMode.NORMAL;

        mylocationClient = new LocationClient(this);
        mylistener = new MylocationListener();

        mylocationClient.registerLocationListener(mylistener);
        LocationClientOption moption = new LocationClientOption();
        moption.setCoorType("bd09ll");
        moption.setIsNeedAddress(true);
        moption.setOpenGps(true);

//        int span = 1000;
//        moption.setScanSpan(span);
        mylocationClient.setLocOption(moption);

        myIconLocation1 = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_mark);

        MyLocationConfiguration configuration
                = new MyLocationConfiguration(locationMode,true,myIconLocation1);
        myBaiduMap.setMyLocationConfiguration(configuration);
        myOrientationListener = new MyOrientationListener(context);

        //通过接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                myCurrentX = x;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_mylocation:
                getLocationByLL(myLatitude, myLongitude);
                break;
            case R.id.menu_item_IIsearch:
                myLinearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
                myLinearLayout1.setVisibility(View.VISIBLE);
                final EditText myEditText_lg = (EditText) findViewById(R.id.editText_lg);
                final EditText myEditText_la = (EditText) findViewById(R.id.editText_la);
                Button button_ll = (Button) findViewById(R.id.button_IIsearch);

                button_ll.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        final double mylg = Double.parseDouble(myEditText_lg.getText().toString());
                        final double myla = Double.parseDouble(myEditText_la.getText().toString());

                        getLocationByLL(myla,mylg);
                        myLinearLayout1.setVisibility(View.GONE);
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                });
                break;
            case R.id.menu_item_sitesearch:
                myLinearLayout2 = (LinearLayout) findViewById(R.id.linearLayout2);
                myLinearLayout2.setVisibility(View.VISIBLE);
                final EditText myEditText_site = (EditText) findViewById(R.id.editText_site);
                Button button_site = (Button) findViewById(R.id.button_sitesearch);

                button_site.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        final String site_str = myEditText_site.getText().toString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                AddressToLatitudeLongitude at = new AddressToLatitudeLongitude(site_str);
                                at.getLatAndLngByAddress();
                                getLocationByLL(at.getLatitude(), at.getLongitude());
                            }
                        }).start();
                        //隐藏前面地址输入区域
                        myLinearLayout2.setVisibility(View.GONE);
                        //隐藏输入法键盘
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getLocationByLL(double la, double lg) {
        LatLng latLng = new LatLng(la, lg);

        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        myBaiduMap.setMapStatus(msu);
    }

    public class MylocationListener implements BDLocationListener{
        private boolean isFirstIn = true;
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //BDLocation 回调的百度坐标类，内部封装了如经纬度、半径等属性信息
            //MyLocationData 定位数据,定位数据建造器
            /*
            * 可以通过BDLocation配置如下参数
            * 1.accuracy 定位精度
            * 2.latitude 百度纬度坐标
            * 3.longitude 百度经度坐标
            * 4.satellitesNum GPS定位时卫星数目 getSatelliteNumber() gps定位结果时，获取gps锁定用的卫星数
            * 5.speed GPS定位时速度 getSpeed()获取速度，仅gps定位结果时有速度信息，单位公里/小时，默认值0.0f
            * 6.direction GPS定位时方向角度
            * */
            myLatitude = bdLocation.getLatitude();
            myLongitude = bdLocation.getLongitude();
            MyLocationData data = new MyLocationData.Builder()
                    .direction(myCurrentX)
                    .accuracy(bdLocation.getRadius())
                    .latitude(myLatitude)
                    .longitude(myLongitude)
                    .build();
            myBaiduMap.setMyLocationData(data);

            if(isFirstIn){
                getLocationByLL(myLatitude,myLongitude);
                isFirstIn = false;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        myBaiduMap.setMyLocationEnabled(true);
        if (!mylocationClient.isStarted()) {
            mylocationClient.start();
        }
        myOrientationListener.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        myBaiduMap.setMyLocationEnabled(false);
        mylocationClient.stop();
        myOrientationListener.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        myMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myMapView.onDestroy();
    }
}
