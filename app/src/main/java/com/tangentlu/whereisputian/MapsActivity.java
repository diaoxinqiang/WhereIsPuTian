package com.tangentlu.whereisputian;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.Circle;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;

public class MapsActivity extends FragmentActivity implements LocationSource,
        AMapLocationListener, RadioGroup.OnCheckedChangeListener, GeocodeSearch.OnGeocodeSearchListener, PoiSearch.OnPoiSearchListener {
    private int[] markers = {
            R.drawable.poi_marker_1,
            R.drawable.poi_marker_2,
            R.drawable.poi_marker_3,
            R.drawable.poi_marker_4,
            R.drawable.poi_marker_5,
            R.drawable.poi_marker_6,
            R.drawable.poi_marker_7,
            R.drawable.poi_marker_8,
            R.drawable.poi_marker_9,
            R.drawable.poi_marker_10
    };
    private ProgressDialog pd;
    private static final int DOWNLOAD_APK_PROGRESS = 521;
    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    //声明AMapLocationClient类对象
    //声明定位回调监听器
    public AMapLocationListener mLocationListener;
    private GeocodeSearch geocoderSearch;
    private ProgressDialog progDialog;
    private String addressName;
    private LatLonPoint latLonPoint1 = new LatLonPoint(22.572343,114.062353);
    private LatLonPoint latLonPoint2 = new LatLonPoint(22.551233,114.061463);
    private Circle circle;
    private SeekBar mColorBar;
    private SeekBar mAlphaBar;
    private SeekBar mWidthBar;
    private ArrayList<HospitalItem> hospitalList;
    private ArrayList<HospitalItem> NewHospitalList;
    private boolean isFirst=true;
    private int index = 0;
    ListView ls;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ls = (ListView) findViewById(R.id.lv);
        checkUpdate();
        String s = sHA1(getApplicationContext());
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        NewHospitalList = new ArrayList<HospitalItem>();
        init();
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            geocoderSearch = new GeocodeSearch(this);
            geocoderSearch.setOnGeocodeSearchListener(this);
            progDialog = new ProgressDialog(this);
            setUpMap();
        }
        // 绘制一个圆形
//        circle = aMap.addCircle(new CircleOptions().center(Constants.BEIJING)
//                .radius(4000).strokeColor(Color.argb(50, 1, 1, 1))
//                .fillColor(Color.argb(50, 1, 1, 1)).strokeWidth(25));
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // aMap.setMyLocationType()
    }
    /**
     * 显示进度条对话框
     */
    public void showDialog() {
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在获取地址");
        progDialog.show();
    }
    /**
     * 隐藏进度条对话框
     */
    public void dismissDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    /**
     * 响应逆地理编码
     */
    public void getAddress(final LatLonPoint latLonPoint) {
        showDialog();
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
    }
    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        dismissDialog();
        if (rCode == 1000) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                addressName = result.getRegeocodeAddress().getFormatAddress()
                        + "附近";
//                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                        AMapUtil.convertToLatLng(result.getRegeocodeQuery().getPoint()), 15));
               Marker regeoMarker = aMap.addMarker(new MarkerOptions().anchor(0.1f, 0.1f));
//                regeoMarker.setIcon(BitmapDescriptorFactory
//                        .fromBitmap(BitmapFactory.decodeResource(
//                                getResources(),
//                                markers[index])));
//                index++;
                regeoMarker.setIcon(BitmapDescriptorFactory
                        .fromBitmap(BitmapFactory.decodeResource(
                                getResources(),
                                R.drawable.poi_marker_pressed)));
//                NewHospitalList.add()
                regeoMarker.setPosition(AMapUtil.convertToLatLng(result.getRegeocodeQuery().getPoint()));
//                ToastUtil.show(MapsActivity.this, addressName);
            } else {
                ToastUtil.show(MapsActivity.this, "定位失败");
            }
        } else {
            ToastUtil.showerror(MapsActivity.this, rCode);
        }
    }

    private String getPosition(LatLonPoint point) {

        return null;
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                deactivate();
                String city  = amapLocation.getCity();
                if(isFirst) {
                    isFirst = false;
                    hospitalList = DBUtil.getItemsByCity(HospitalItem.class, city);
                    showLV(hospitalList);
                    for (int i = 0; i < hospitalList.size(); i++) {
                        HospitalItem hospitalItem = hospitalList.get(i);
                        if (hospitalItem.location_x != 0.0 && hospitalItem.location_y != 0.0) {
                            LatLonPoint latLonPoint = new LatLonPoint(hospitalItem.location_x, hospitalItem.location_y);
                            getAddress(latLonPoint);
                        }
                    }
                }
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    private void showLV(List<HospitalItem> hl) {
        HospitalLvItemAdapter adapter = new HospitalLvItemAdapter(getBaseContext(),hl);
        ls.setAdapter(adapter);
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
//            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    private void checkUpdate() {
        FIR.checkForUpdateInFIR("dcc117d0b6aee0f9024170784f303368", new VersionCheckCallback() {
            @Override
            public void onSuccess(String versionJson) {
                JSONObject myJsonObject = null;
                try {
                    myJsonObject = new JSONObject(versionJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String netVersion = myJsonObject.optString("versionShort");
                String localVersion = getLocalVersion();
                if (!netVersion.equals(localVersion)) {
//                    tv_update_version.setText("软件更新 (点击更新)");
//                    tv_update_version.setTextColor(Color.RED);
                } else {
//                    tv_update_version.setText("软件更新 (当前为最新版本)");
//                    view.findViewById(com.szridge.hxdapp.R.id.update_layout).setOnClickListener(null);
                }
            }
        });
    }

    public String getLocalVersion() {
        PackageManager pm = this.getPackageManager();//context为当前Activity上下文
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (pi != null)
            return pi.versionName;
        else
            return "";
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }

    public void aboutClick(View v) {
        updateVersion();
    }

    public void searchClick(View v) {
        doSearchQuery();
    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery() {
        List<HospitalItem> hl2 = DBUtil.getAllItem(HospitalItem.class);
//        List<HospitalItem> hl = new ArrayList<>();
//        for (int g=0;g<10;g++) {
//            hl.add(hl2.get(g));
//        }
        List<HospitalItem> dhl = new ArrayList<>();
        for (HospitalItem h : hl2) {
            if (h.queryHospitalName==null) {
                dhl.add(h);
            }
        }
        DBUtil.delete(dhl);
//        PoiSearch.Query query;// Poi查询条件类
//        PoiSearch poiSearch;
//        for (HospitalItem h : hl2) {
//            query = new PoiSearch.Query(h.hospitalName, "", h.city);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
////            query = new PoiSearch.Query("济南阳光女子医院", "", "");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
//            query.setPageSize(10);// 设置每页最多返回多少条poiitem
//            query.setPageNum(0);// 设置查第一页
//            query.setCityLimit(true);
//
//            poiSearch = new PoiSearch(this, query);
//            poiSearch.setOnPoiSearchListener(this);
//            poiSearch.searchPOIAsyn();
//        }
    }

    List<HospitalItem> afterHL = new ArrayList<>();

    /**
     * POI信息查询回调方法
     */
    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        if (rCode == 1000) {
            String queryName = result.getQuery().getQueryString();
            ArrayList<PoiItem> pl = result.getPois();
            PoiItem p = pl.get(0);
            String hName = p.getTitle();
            String[] ss = hName.split("");
//            String newHName = "";
//            for (int i=1;i<ss.length-2;i++) {
//                if (newHName.equals("")) {
//                    newHName = "%"+ss[i]+"%";
//                }else {
//                    newHName += ss[i] + "%";
//                }
//            }
//            List<HospitalItem> hl = DBUtil.getItemsByHospitalName(HospitalItem.class,newHName);

            List<HospitalItem> hl = DBUtil.getItemsByHospitalName(HospitalItem.class,queryName);

            if (hl.size() > 0) {
                HospitalItem h = hl.get(0);
                h.queryHospitalName = p.getTitle();
                h.city = p.getCityName();
                h.address = p.getProvinceName() + p.getCityName() + p.getAdName() + p.getSnippet();
                LatLonPoint latLonPoint = p.getLatLonPoint();
                h.location_x = latLonPoint.getLatitude();
                h.location_y = latLonPoint.getLongitude();
                afterHL.add(h);
            }
            DBUtil.saveItem(afterHL);
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem item, int rCode) {
        // TODO Auto-generated method stub
        String s = "";

    }


    private void updateVersion() {
        FIR.checkForUpdateInFIR("dcc117d0b6aee0f9024170784f303368", new VersionCheckCallback() {
            @Override
            public void onSuccess(String versionJson) {
                try {
                    JSONObject myJsonObject = new JSONObject(versionJson);
                    String netVersion = myJsonObject.optString("versionShort");
                    String localVersion = getLocalVersion();
                    if (!netVersion.equals(localVersion)) {
//						DownloadTask downloadTask = new DownloadTask();
//						downloadTask.execute(myJsonObject.optString("installUrl"));
                        myJsonObject.optString("installUrl");
                        //启动服务下载apk
                        // this is how you fire the downloader
                        Intent intent = new Intent(MapsActivity.this, DownloadService.class);
                        intent.putExtra("url", myJsonObject.optString("installUrl"));
                        final String fileName = "upData.apk";
                        File tmpFile = new File( Environment.getExternalStorageDirectory() + "/WhereIsPuTian/download/");
                        if (!tmpFile.exists()) {
                            tmpFile.mkdirs();
                        }
                        File file = new File(tmpFile, fileName);
                        //如果文件存在则删除
                        if (file.exists()) {
                            file.delete();
                        }
                        intent.putExtra("file", file);
                        final DownloadReceiver downloadReceiver = new DownloadReceiver(new Handler());
                        intent.putExtra(DownloadService.PROGRESS_FLAG, DOWNLOAD_APK_PROGRESS);
                        intent.putExtra("receiver", downloadReceiver);
                        startService(intent);
                        pd = new ProgressDialog(MapsActivity.this);
                        pd.setTitle("系统更新");
                        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        pd.setCancelable(false);
                        pd.setProgress(0);
                        pd.show();
                    } else {
                        Toast.makeText(MapsActivity.this, "已经是最新版本", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("fir", "check from fir.im success! " + "\n" + versionJson);
            }

            @Override
            public void onFail(Exception exception) {
                Log.i("fir", "check fir.im fail! " + "\n" + exception.getMessage());
            }

            @Override
            public void onStart() {
                Toast.makeText(MapsActivity.this, "正在获取", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
//				Toast.makeText(AboutActivity2.this, "获取完成", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("ParcelCreator")
    class DownloadReceiver extends ResultReceiver {

        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DOWNLOAD_APK_PROGRESS) {
                int progress = resultData.getInt("progress");
                pd.setProgress(progress);
                if (progress == 100) {
                    pd.dismiss();
                    //安装软件
                    final String fileName = "upData.apk";
                    File tmpFile = new File(Environment.getExternalStorageDirectory() + "/WhereIsPuTian/download/");
                    File file = new File(tmpFile, fileName);
                    openFile(file);
                }
            }
        }
    }
    //打开APK程序代码

    private void openFile(File file) {
        // TODO Auto-generated method stub
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);

            byte[] cert = info.signatures[0].toByteArray();

            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            return hexString.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
