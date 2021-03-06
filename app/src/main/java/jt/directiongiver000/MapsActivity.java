package jt.directiongiver000;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.text.Text;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageButton;

public class MapsActivity extends RPGConversationActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final double[] TODO = null;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private double userX;
    private double userY;
    private TextView char_text;
    private String destination;
    private PolylineOptions polyOnMap;
    private PolylineOptions user_polyOnMap;
    private Marker myLocation;
    static Marker roadLocation;
    public static String[] roadInfo;
    public static location[] roadLatLng;
    private int roadInfoTop = 1;
    private TextView roadInfoText;
    private int navStatus = -1;
    private ArrayList<Stores> nearByShops;
    private String[] nearByName;
    Polyline user_line;
    LatLng myPosition;
    static String[] polyline;
    private GoogleApiClient googleApiClient;
    private String userEmail;
    private ArrayList<LatLng> bestLine = new ArrayList<>();
    private String history_ID;
    private double userJiaoDu = 0;
    private TextView leftTime;
    private ArrayList<NPC> npcOnMap = new ArrayList<>();
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("導航模式");

        //載入使用者的商家過濾選擇
        checkShopFilter();

        roadInfoText = (TextView)findViewById(R.id.listview);
        nearByShops = new ArrayList<>();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //拿GOOGLE資料
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mLocationManager.requestLocationUpdates
                (LocationManager.GPS_PROVIDER, 100,1, LocationChange);

        user_polyOnMap = new PolylineOptions();
        button = (GifImageButton) findViewById(R.id.charactor);

        //設定角色
        super.initCharactor();
        //設定音量控制
        super.checkVoiceControl();

        char_text = (TextView)findViewById(R.id.char_text);
        leftTime = (TextView)findViewById(R.id.leftTime);

        Speech("請點我說出想去的地方");

        GifImageButton button = (GifImageButton) findViewById(R.id.charactor);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                speech.startListening(recognizerIntent);
            }
        });
    }
    private void getDirection()
    {
        //清除商家的資料，一切重新開始～
        nearByShops.clear();
        final String inputToString = destination;
        System.out.println(inputToString);
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                String url = "http://140.121.197.130:8100/DG/DGGiver?start="+userY+","+userX+"&end="+inputToString;
                System.out.println(url);
                String url2 = new String();
                try
                {
                    url2 = functionList.stringParser(url);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                System.out.println(url2);
                roadInfoGetJSON task = new roadInfoGetJSON();
                task.execute(new String[]{url2});
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            Intent intent = new Intent();
            intent.setClass(MapsActivity.this, MainActivity.class);
            startActivity(intent);
            MapsActivity.this.finish();
            mLocationManager.removeUpdates(LocationChange);
        }
    }
    //更新定位Listener
    public LocationListener LocationChange = new LocationListener() {
        public void onLocationChanged(Location mLocation)
        {

            //印出我的座標-經度緯度
            Log.d("TAG", "我的座標改變囉！ - 經度 : " + mLocation.getLongitude() + "  , 緯度 : " + mLocation.getLatitude());
            userJiaoDu = functionList.GetJiaoDu(userX,userY,mLocation.getLongitude(),mLocation.getLatitude());
            userX = mLocation.getLongitude();
            userY = mLocation.getLatitude();
            myLocation.remove();
            myPosition = new LatLng(userY, userX);
            addMyPosition(myPosition,"你現在在這裡");
            //加到polyline內
            user_line.remove();
            user_polyOnMap.add(myPosition);
            user_line = mMap.addPolyline(user_polyOnMap.color(0x99E74C3C));
            System.out.println("User's Polyline : " + user_line.getPoints());
            System.out.println("User's LatLng : " + user_polyOnMap.getPoints());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
            moveMap(myPosition);
            if(navStatus == -1)
            {
                navStatus = 0;
            }
            else
                renewRoadInfo();
        }


        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    public void renewRoadInfo()
    {
        if(navStatus != 0)
        {
            if(roadInfoTop < roadLatLng.length-1)
            {
                if(distanceCheck(roadLatLng[roadInfoTop]))
                {
                    System.out.println("下一條路確認：" + roadInfo[roadInfoTop]);
                    Speech(roadInfo[roadInfoTop]);
                    roadInfoText.setText(roadInfo[roadInfoTop]);
                    //修改路線上的標誌
                    LatLng position = new LatLng(roadLatLng[roadInfoTop+1].Y,roadLatLng[roadInfoTop+1].X);
                    addMapPosition(position,"下一條轉彎路線",1);
                    roadInfoTop++;
                }
                else
                {
                    System.out.println("距離下一條路線：" + distanceCheck(roadLatLng[roadInfoTop]));
                }
            }
            else
            {
                if(distanceCheck(roadLatLng[roadInfoTop]))
                {
                    Speech("抵達目的地");
                    roadInfoText.setText("抵達目的地");
                    navStatus = 0;
                }
                else
                {
                    System.out.println("距離目的地：" + distanceCheck(roadLatLng[roadInfoTop]));
                }
            }
            //傳歷史紀錄給DB
            Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    String url = "http://140.121.197.130:8100/UpdateUserLineServlet/UpdateServlet?userline="+userX+","+userY+";&history_ID="+history_ID;
                    System.out.println(url);
                    StringBuilder output = new StringBuilder("");
                    InputStream stream;
                    try {
                        url = functionList.stringParser(url);
                        URL url2 = new URL(url);
                        URLConnection connection = url2.openConnection();
                        HttpURLConnection httpConnection = (HttpURLConnection) connection;
                        httpConnection.setRequestMethod("GET");
                        httpConnection.connect();
                        stream = httpConnection.getInputStream();
                        BufferedReader builder = new BufferedReader(new InputStreamReader(stream,"utf-8"));
                        output.append(builder.readLine());
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        getNearBy();
    }

    private void getNearBy()
    {
        final String inputToString = destination;
        System.out.println(inputToString);
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                String url = "http://140.121.197.130:8100/NearByServlet/NearServlet?longitude="+userX+"&latitude="+userY;
                System.out.println(url);
                String url2 = new String();
                try
                {
                    url2 = functionList.stringParser(url);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                System.out.println(url2);
                nearByGetJSON task = new nearByGetJSON();
                task.execute(new String[]{url2});
            }
        });
        searchNPC();
    }

    private boolean distanceCheck(location roadLL)
    {
        System.out.println("進行距離比對");
        System.out.println(userX + "," + userY + " 到 " + roadLL.X + "," + roadLL.Y);
        int distance = (int)functionList.GetDistance(userX,userY,roadLL.X,roadLL.Y);
        System.out.println("還差：" + distance +"公尺");
        if(distance > 60)
            leftTime.setText("約" + distance/60 + "分");
        else
            leftTime.setText("約" + distance + "秒");
        if(distance <= 50)
        {
            if(distance % 20 < 2)
            {
                Speech("再走" + distance + "秒就到下一個路線！");
            }
        }
        else
        {
            if(distance % 60 < 2)
            {
                Speech("再走" + distance/60 + "分鐘就到下一個路線！");
            }
        }
        if(distance < 10)
            return true;
        return false;
    }

    private double[] getGPS()
    {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);


/* 迴圈讀取providers,如果有位址資訊, 退出迴圈*/
        Location l = null;


        for (int i = providers.size() - 1; i >= 0; i--) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return TODO;
            }
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }


        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude(); //緯度
            gps[1] = l.getLongitude();//經度
        }
        return gps;
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final double[] position = this.getGPS();
        userY = position[0];
        userX = position[1];
        checkDestination();
        checkToilet();
        System.out.println(position[0] + "," + position[1]);
        // Add a marker in Sydney and move the camera
        myPosition = new LatLng(position[0], position[1]);
        addMyPosition(myPosition,"你現在在這裡");
        user_polyOnMap.add(myPosition);
        user_line = mMap.addPolyline(user_polyOnMap.color(0x99E74C3C));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
        moveMap(myPosition);
    }
    private void moveMap(LatLng place) {
        // 建立地圖攝影機的位置物件
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(place)
                        .zoom(17)
                        .build();

        // 使用動畫的效果移動地圖
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class roadInfoGetJSON extends AsyncTask<String, Void, String>
    {    //<doInBackground()傳入的參數, doInBackground() 執行過程中回傳給UI thread的資料, 傳回執行結果>
        private String[] distanceCheck;
        private String[] durationCheck;
        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;        //傳給onPostExecute()
        }

        private String getOutputFromUrl(String url) {

            StringBuilder output = new StringBuilder("");
            try {
                InputStream stream = getHttpConnection(url);
                BufferedReader builder = new BufferedReader(new InputStreamReader(stream));
                output.append(builder.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(output.toString());
            return output.toString();
        }

        private InputStream getHttpConnection(String urlString) throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            System.out.println(url.toString());
            URLConnection connection = url.openConnection();
            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();
                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stream;
        }

        @Override
        protected void onPostExecute(String output)
        {
            System.out.println(output);
            if(!output.equals("[]"))
            {
                System.out.println("開始解析字串");
                getJSON(output);
            }
            else
            {
                char_text.setText("找不到前往" + destination + "的路線！");
                Speech("找不到前往" + destination + "的路線！");
                roadInfoText.setText("找不到路線！");
            }
        }

        private void getJSON(String jsonString)
        {

            System.out.println("StartGettingJson");
            JSONArray jsonArr = null;
            try {
                jsonArr = new JSONArray(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println(jsonArr.toString());
            for(int top = 0;top < jsonArr.length();top++)
            {
                //System.out.println("=====路線" + (top+1) + "=====");
                JSONArray jsonItem = null;//選第top個Object(路線資訊所在處)
                try
                {
                    jsonItem = jsonArr.getJSONArray(top);
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }

                distanceCheck = new String[jsonItem.length()];
                durationCheck = new String[jsonItem.length()];
                roadInfo = new String[jsonItem.length()];
                polyline = new String[jsonItem.length()];
                roadLatLng = new location[jsonItem.length()+1];
                polyOnMap = new PolylineOptions();

                //重新設定地圖
                initMap();

                for (int i = 0; i < jsonItem.length(); i++)
                {
                    try
                    {
                        // JSONObject modFamily = jsonArr.getJSONObject(i);
                        distanceCheck[i] = jsonItem.getJSONObject(i).getString("distance");
                        durationCheck[i] = jsonItem.getJSONObject(i).getString("duration");
                        roadInfo[i] = jsonItem.getJSONObject(i).getString("html_instructions");
                        polyline[i] = jsonItem.getJSONObject(i).getString("polyline");
                        roadLatLng[i] = new location(jsonItem.getJSONObject(i).getJSONObject("start_location").getDouble("X"),jsonItem.getJSONObject(i).getJSONObject("start_location").getDouble("Y"));

                        //新增新路線圖示
                        ArrayList<LatLng> polyOnMap_LatLng = decodePolyPoints(polyline[i]);
                        for(LatLng k: polyOnMap_LatLng)
                        {
                            polyOnMap.add(k);
                            bestLine.add(k);
                            roadLatLng[jsonItem.length()] = new location(k.longitude,k.latitude);
                        }
                        user_polyOnMap = new PolylineOptions();
                        mMap.addPolyline(polyOnMap.color(0x993498DB));

                        System.out.println(roadInfo[i]);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }

            }

            //傳歷史紀錄給DB
            Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    Geocoder gc = new Geocoder(MapsActivity.this, Locale.TRADITIONAL_CHINESE); 	//地區:台灣
                    //自經緯度取得地址
                    List<Address> lstAddress = null;
                    try {
                        lstAddress = gc.getFromLocation(userY, userX, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final String returnAddress = lstAddress.get(0).getAddressLine(0);
                    System.out.println(returnAddress);
                    String url = "http://140.121.197.130:8100/HistoryServlet/WriteInServlet?account="+userEmail+"&start="+returnAddress+"&destination="+destination+"&bestline="+polyToBestLine();
                    System.out.println(url);
                    StringBuilder output = new StringBuilder("");
                    InputStream stream;
                    try {
                        url = functionList.stringParser(url);
                        URL url2 = new URL(url);
                        URLConnection connection = url2.openConnection();
                        HttpURLConnection httpConnection = (HttpURLConnection) connection;
                        httpConnection.setRequestMethod("GET");
                        httpConnection.connect();
                        stream = httpConnection.getInputStream();
                        BufferedReader builder = new BufferedReader(new InputStreamReader(stream,"utf-8"));
                        output.append(builder.readLine());
                        history_ID = output.toString();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

            System.out.println(history_ID);
            //直接顯示第一條路線
            roadInfoText.setText(roadInfo[0]);
            Speech(roadInfo[0]);
            LatLng position = new LatLng(roadLatLng[roadInfoTop].Y,roadLatLng[roadInfoTop].X);
            addMapPosition(position,"下一個轉彎路口",1);

            System.out.println(roadLatLng[roadLatLng.length-1].Y + "," + roadLatLng[roadLatLng.length-1].X);
            LatLng destinationPosition = new LatLng(roadLatLng[roadLatLng.length-1].Y, roadLatLng[roadLatLng.length-1].X);
            addMapPosition(destinationPosition,"目的地",2);

            navStatus = 1;
        }
        private void initMap()
        {
            //清除地圖上所有東西
            mMap.clear();

            //加入定位標誌
            myPosition = new LatLng(userY, userX);
            addMyPosition(myPosition,"出發！");
        }

        public ArrayList<LatLng> decodePolyPoints(String encodedPath){
            int len = encodedPath.length();

            final ArrayList<LatLng> path = new ArrayList<LatLng>();
            int index = 0;
            int lat = 0;
            int lng = 0;

            while (index < len) {
                int result = 1;
                int shift = 0;
                int b;
                do {
                    b = encodedPath.charAt(index++) - 63 - 1;
                    result += b << shift;
                    shift += 5;
                } while (b >= 0x1f);
                lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

                result = 1;
                shift = 0;
                do {
                    b = encodedPath.charAt(index++) - 63 - 1;
                    result += b << shift;
                    shift += 5;
                } while (b >= 0x1f);
                lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

                path.add(new LatLng(lat * 1e-5, lng * 1e-5));
            }

            return path;
        }
    }

    public class nearByGetJSON extends AsyncTask<String, Void, String>
    {    //<doInBackground()傳入的參數, doInBackground() 執行過程中回傳給UI thread的資料, 傳回執行結果>
        private String[] nearByID;
        private String[] nearByDescription;
        private String[] nearByAddress;
        private location[] nearByLocation;
        private String[] nearByClass;
        private String[] nearByRating;
        private double[] nearByJiaoDu;
        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;        //傳給onPostExecute()
        }

        private String getOutputFromUrl(String url) {

            StringBuilder output = new StringBuilder("");
            try
            {
                InputStream stream = getHttpConnection(url);
                BufferedReader builder = new BufferedReader(new InputStreamReader(stream));
                output.append(builder.readLine());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(output.toString());
            return output.toString();
        }

        private InputStream getHttpConnection(String urlString) throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            System.out.println(url.toString());
            URLConnection connection = url.openConnection();
            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();
                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stream;
        }

        @Override
        protected void onPostExecute(String output)
        {
            System.out.println(output);
            if(!output.equals("[]"))
            {
                System.out.println("進來了");
                getJSON(output);
            }
        }

        private void getJSON(String jsonString)
        {

            System.out.println("StartGettingJson");

            JSONArray jsonArr = null;
            try {
                jsonArr = new JSONArray(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            nearByID = new String[jsonArr.length()];
            nearByName = new String[jsonArr.length()];
            nearByAddress = new String[jsonArr.length()];
            nearByDescription = new String[jsonArr.length()];
            nearByLocation = new location[jsonArr.length()];
            nearByClass = new String[jsonArr.length()];
            nearByRating = new String[jsonArr.length()];
            nearByJiaoDu = new double[jsonArr.length()];

            System.out.println(jsonArr.toString());
            for (int i = 0; i < jsonArr.length(); i++)
            {
                try
                {
                    // JSONObject modFamily = jsonArr.getJSONObject(i);
                    nearByID[i] = jsonArr.getJSONObject(i).getString("ID");
                    nearByName[i] = jsonArr.getJSONObject(i).getString("NAME");
                    nearByAddress[i] = jsonArr.getJSONObject(i).getString("ADDRESS");
                    nearByDescription[i] = jsonArr.getJSONObject(i).getString("DESCRIPTION");
                    nearByLocation[i] = new location(jsonArr.getJSONObject(i).getDouble("PX"),jsonArr.getJSONObject(i).getDouble("PY"));
                    nearByClass[i] = jsonArr.getJSONObject(i).getString("CLASS");
                    //nearByRating[i] =  jsonArr.getJSONObject(i).getString("RATING");
                    nearByJiaoDu[i] = jsonArr.getJSONObject(i).getDouble("jiaoDu");

                    //檢查是否有在地圖上了
                    System.out.println("開始檢查！");

                    //新增商家到地圖上，並朗讀～ 新增了店家的filter
                    if(checkShops(nearByName[i],nearByLocation[i]) && functionList.shopFilter(nearByClass[i],shopUserselect))
                    {
                        final int k = i;
                        final LatLng shopPosition = new LatLng(nearByLocation[i].Y, nearByLocation[i].X);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                mMap.addMarker(
                                        new MarkerOptions().position(shopPosition).title(nearByName[k]).snippet(nearByClass[k]).icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant)).zIndex(0));

                                //設定商家Button
                                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(Marker marker)
                                    {
                                        setMarkerOnclickListener(marker);
                                        // Add the button
                                        return false;
                                    }
                                });

                                double jiaoDuTmp = nearByJiaoDu[k] - userJiaoDu;
                                if(jiaoDuTmp < 0)
                                {
                                    jiaoDuTmp = jiaoDuTmp + 360;
                                }
                                String jiaoDu = functionList.getNearByJiaoDu(jiaoDuTmp);
                                Stores tempStore = new Stores(nearByName[k],nearByLocation[k],nearByID[k],nearByAddress[k],nearByClass[k],nearByDescription[k],"X");
                                nearByShops.add(tempStore);
                                Speech("我找到一家店了！" + nearByName[k] + "，在您的" + jiaoDu);
                                char_text.setText(nearByDescription[k]);
                            }
                        });
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
        private boolean checkShops(String shopName,location location)
        {
            if(nearByShops != null)
            {
                System.out.println("發現地圖上已經有東西！");
                for(Stores tmp : nearByShops)
                {
                    if(tmp.getName().equals(shopName))
                    {
                        if(tmp.getLocation().toString().equals(location.toString()))
                        {
                            System.out.println(shopName + "有出現過！不加！");
                            //如果有在表內代表已出現過！
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }
    @Override
    void RPGConversation(ArrayList<String> text)
    {
        final String tmpText = text.get(0);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                //使用API.AI分析使用者講的話
                String url = "http://140.121.197.130:8100/AIConversation/AIConversationServlet?userInput=" + tmpText;
                System.out.println(url);
                InputStream stream = null;
                StringBuilder output = new StringBuilder("");
                try {
                    url = functionList.stringParser(url);
                    URL url2 = new URL(url);
                    URLConnection connection = url2.openConnection();
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    httpConnection.setRequestMethod("GET");
                    httpConnection.connect();
                    if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        stream = httpConnection.getInputStream();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                BufferedReader builder = null;
                try {
                    builder = new BufferedReader(new InputStreamReader(stream, "utf-8"));
                    String tmpText = null;
                    while ((tmpText = builder.readLine()) != null) {
                        System.out.println(tmpText);
                        output.append(tmpText);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String[] tokens = output.toString().split(";");
                int tokenTop = 0;
                String status = null;
                String text1 = null;
                String text2 = null;
                for (String token : tokens) {
                    switch (tokenTop) {
                        case 0:
                            status = token;
                            break;
                        case 1:
                            text1 = token;
                            break;
                        case 2:
                            text2 = token;
                            break;
                    }
                    tokenTop++;
                }

                if (status.equals("weatherNothing"))
                {
                    String date = dateFormat.format(dateToday);
                    double[] position = getGPS();
                    Geocoder gc = new Geocoder(MapsActivity.this, Locale.TRADITIONAL_CHINESE); 	//地區:台灣
                    List<Address> lstAddress = null;
                    try {
                        lstAddress = gc.getFromLocation(position[0], position[1], 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final String returnAddress = lstAddress.get(0).getAddressLine(0);
                    String location = returnAddress.substring(5,8);
                    String result = null;
                    try {
                        result = functionList.getWeather(location,date);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final String resultForUI = result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText(resultForUI);
                        }
                    });
                    Speech(result);
                } else if (status.equals("weatherNotDate")) {
                    //final String result = "沒有說日期，" + text1 + "的天氣狀態";
                    String date = dateFormat.format(dateToday);
                    String result = null;
                    try {
                        result = functionList.getWeather(text1,date);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final String resultForUI = result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText(resultForUI);
                        }
                    });
                    Speech(result);
                } else if (status.equals("whereToGo"))
                {
                    if(text1.equals("便利商店"))
                    {
                        goConvenienceStore();
                    }
                    else
                    {
                        final String temp = "前往" + text1;
                        Speech(temp);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                char_text.setText(temp);
                            }
                        });
                        destination = text1;
                        getDirection();
                    }
                } else if (status.equals("weatherNotLocation")) {
                    double[] position = getGPS();
                    Geocoder gc = new Geocoder(MapsActivity.this, Locale.TRADITIONAL_CHINESE); 	//地區:台灣
                    List<Address> lstAddress = null;
                    try {
                        lstAddress = gc.getFromLocation(position[0], position[1], 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final String returnAddress = lstAddress.get(0).getAddressLine(0);
                    System.out.println("查詢地址:"+returnAddress);
                    String location = returnAddress.substring(5,8);
                    System.out.println("查詢資料:"+location);
                    String result = null;
                    try {
                        result = functionList.getWeather(location,text1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //final String result = "沒有說地點，" + text1 + "的天氣狀態";
                    final String resultForUI = result;
                            runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText(resultForUI);
                        }
                    });
                    Speech(result);
                } else if (status.equals("whatIsShopData")) {
                    final String result = "找尋" + text1 + "的商家介紹";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText(result);
                        }
                    });
                    Speech("找尋" + text1 + "的商家介紹");
                } else if (status.equals("defaultWelcomeIntent")) {
                    final String result = text1;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText(result);
                        }
                    });
                    Speech(text1);
                } else if (status.equals("howManyTime")) {
                    final int distance = (int)functionList.GetDistance(userX,userY,roadLatLng[roadLatLng.length-1].X,roadLatLng[roadLatLng.length-1].Y);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText("差不多還要" + distance/60 + "分鐘");

                        }
                    });
                    Speech("差不多還要" + distance/60 + "分鐘");
                } else if (status.equals("weatherComposite")) {
                    String result = null;
                    try {
                        result = functionList.getWeather(text1,text2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final String resultForUI = result;
                    //final String result = text1 + text2 + "的天氣狀態";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText(resultForUI);
                        }
                    });
                    Speech(result);
                }
                else if(status.equals("filterDirtyWords"))
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText("喂！不要說髒話！");
                        }
                    });
                    Speech("喂！不要說髒話！");
                }
                else if(status.equals("goToilet"))
                {
                    goToilet();
                }
                else if(status.equals("ConvenienceStore"))
                {
                    goConvenienceStore();
                }
                else//聽不懂
                {
                    final String result = text1;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText(result);
                        }
                    });
                    Speech(text1);
                }
            }
        });
        thread.start();
    }
    private void addMyPosition(final LatLng position,final String name)
    {
        //先把先前的刪除
        if(navStatus > 0)
        {
            myLocation.remove();
        }
        //根據選擇的角色加入icon
        if(charactor == 0.5)//如果是鹿憨
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myLocation = mMap.addMarker(
                            new MarkerOptions()
                                    .position(position)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.luhen_ico)));
                    myLocation.setZIndex(3);
                }
            });
        }
        else if(charactor == 1)//如果是小鹿
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myLocation = mMap.addMarker(
                            new MarkerOptions()
                                    .position(position)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.xiaolu_ico)));
                    myLocation.setZIndex(3);
                }
            });
        }
        else//不然就是瑞迪
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myLocation = mMap.addMarker(
                            new MarkerOptions()
                                    .position(position)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.reindeer_ico)));
                    myLocation.setZIndex(3);
                }
            });
        }
    }
    private void addMapPosition(final LatLng position,final String roadName,int check)
    {
        if(check == 1)//代表是下一條路，加橘色的
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    roadLocation = mMap.addMarker(
                            new MarkerOptions()
                                    .position(position)
                                    .title(roadName)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.next_road)));
                    roadLocation.setZIndex(4);
                }
            });
        }
        else if(check == 1)//代表不是下一條路，加灰色
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    roadLocation = mMap.addMarker(
                            new MarkerOptions()
                                    .position(position)
                                    .title(roadName)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.untouch_road)));
                    roadLocation.setZIndex(4);
                }
            });
        }
        else if(check == 2)//代表是目的地 加目的地
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    roadLocation = mMap.addMarker(
                            new MarkerOptions()
                                    .position(position)
                                    .title(roadName)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_ico)));
                    roadLocation.setZIndex(4);
                }
            });
        }
    }
    private String polyToBestLine()
    {
        String bestLineAll = "";
        for(LatLng k : bestLine)
        {
            bestLineAll += k.longitude + "," + k.latitude;
            bestLineAll += ";";
        }
        return bestLineAll;
    }
    @Override
    protected void onStart()
    {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if(opr.isDone()){
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    private void handleSignInResult(GoogleSignInResult result)
    {
        if(result.isSuccess()){

            GoogleSignInAccount account = result.getSignInAccount();
            userEmail = account.getEmail();

        } else {
            goLogInScreen();
        }
    }

    private void goLogInScreen()
    {
        Intent intent = new Intent(this, SetActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void checkDestination()
    {
        String Message;
        try {
            FileInputStream fileInputStream = openFileInput("destination");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            while ((Message = bufferedReader.readLine()) != null) {
                stringBuffer.append(Message);
                if(stringBuffer.toString().equals("便利商店"))
                {
                    String clean = "";
                    String file_name = "destination";
                    try {
                        FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_PRIVATE);
                        fileOutputStream.write(clean.getBytes());
                        fileOutputStream.close();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    goConvenienceStore();
                }
                else if(stringBuffer.toString().equals("廁所"))
                {
                    stringBuffer.append(Message);
                    char_text.setText("看看附近有沒有廁所吧～");
                    String temp = "尋找廁所中";
                    String clean = "";
                    String file_name = "toilet";
                    try {
                        FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_PRIVATE);
                        fileOutputStream.write(clean.getBytes());
                        fileOutputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
                    Speech(temp);
                    //現在要在map上新增廁所圖示
                    goToilet();
                }
                else {
                    char_text.setText("前往" + stringBuffer.toString());
                    String temp = "前往" + stringBuffer.toString();
                    destination = stringBuffer.toString();
                    String clean = "";
                    String file_name = "destination";
                    try {
                        FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_PRIVATE);
                        fileOutputStream.write(clean.getBytes());
                        fileOutputStream.close();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
                    Speech(temp);
                    getDirection();
                }
            }
        }
     catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkToilet()
    {
        String Message;
        try {
            FileInputStream fileInputStream = openFileInput("toilet");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            while ((Message = bufferedReader.readLine()) != null)
            {
                stringBuffer.append(Message);
                char_text.setText("看看附近有沒有廁所吧～");
                String temp = "尋找廁所中";
                String clean = "";
                String file_name = "toilet";
                try {
                    FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_PRIVATE);
                    fileOutputStream.write(clean.getBytes());
                    fileOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
                Speech(temp);
                //現在要在map上新增廁所圖示
                goToilet();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToilet()
    {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        char_text.setText("看看附近有沒有廁所吧～");
                    }
                });
                String temp = "尋找廁所中";
                String clean = "";
                String file_name = "toilet";
                try {
                    FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_PRIVATE);
                    fileOutputStream.write(clean.getBytes());
                    fileOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Speech(temp);
                //現在要在map上新增廁所圖示
                Toilet[] toilets = new Toilet[0];
                try {
                    toilets = functionList.getToilet(userX, userY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final int total = toilets.length;
                if (total > 0)
                {
                    for (int top = 0; top < total; top++)
                    {
                        final String toiletName = toilets[top].getName();
                        final LatLng toiletPosition = toilets[top].getPosition();
                        //*****廁所還有address和grade需要加上*****

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run() {
                                System.out.println("建立第一個廁所Mark:"+toiletName);
                                mMap.addMarker(
                                        new MarkerOptions()
                                                .position(toiletPosition)
                                                .title(toiletName)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_wc))
                                                .zIndex(2)
                                );
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText("我找到這些廁所啦");
                        }
                    });
                    Speech("我找到這些廁所啦");
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText("附近沒有任何廁所QQ");
                        }
                    });
                    Speech("附近沒有任何廁所QQ");
                }
            }
        });
        thread.start();
    }

    public void goConvenienceStore()
    {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        char_text.setText("看看附近有沒有便利商店吧～");
                    }
                });
                String temp = "尋找便利商店中";
                String clean = "";
                String file_name = "ConvenienceStore";
                try {
                    FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_PRIVATE);
                    fileOutputStream.write(clean.getBytes());
                    fileOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Speech(temp);
                //現在要在map上新增便利商店圖示
                ConvenienceStore[] convenienceStore = new ConvenienceStore[0];
                try {
                    convenienceStore = functionList.getConvenienceStore(userX, userY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final int total = convenienceStore.length;
                if (total > 0)
                {
                    for (int top = 0; top < total; top++)
                    {
                        final String convenienceName = convenienceStore[top].getName();
                        final LatLng convenienceStorePosition = convenienceStore[top].getPosition();
                        final String ID = convenienceStore[top].getID();
                        final String address = convenienceStore[top].getAddress();

                        //*****廁所還有address和grade需要加上*****

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                System.out.println("建立第一個便利商店Mark:"+convenienceName);
                                mMap.addMarker(
                                        new MarkerOptions()
                                                .position(convenienceStorePosition)
                                                .title(convenienceName)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant))
                                                .zIndex(1)
                                );
                                location tmpLocation = new location(convenienceStorePosition.latitude,convenienceStorePosition.longitude);
                                Stores tmpStores = new Stores(convenienceName,tmpLocation,ID,address,"便利商店","便利商店","X");
                                nearByShops.add(tmpStores);
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText("我找到這些便利商店啦");
                        }
                    });
                    Speech("我找到這些便利商店啦");
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            char_text.setText("附近沒有任何廁所QQ");
                        }
                    });
                    Speech("附近沒有任何廁所QQ");
                }
            }
        });
        thread.start();
    }

    public View getShopView(String shopID,String shopName,String shopKind,String shopRating,String shopAddress,String shopDescription)
    {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.shop_info, null);

        //shopID
        TextView ID = (TextView)view.findViewById(R.id.shopID);
        ID.setText(shopID);

        //shopName
        TextView Name =  (TextView)view.findViewById(R.id.shopName);
        Name.setText(shopName);

        //kind
        TextView kind = (TextView)view.findViewById(R.id.kind);
        kind.setText(shopKind);

        //rating
        TextView rating = (TextView)view.findViewById(R.id.rating);
        rating.setText(shopRating);

        //address
        TextView address = (TextView)view.findViewById(R.id.address);
        address.setText(shopAddress);

        //description
        TextView description = (TextView)view.findViewById(R.id.description);
        description.setText(shopDescription);

        return view;
    }


    public void searchNPC() //尋找附近的NPC並放到地圖上
    {
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                //開始找NPC
                NPC[] npc = new NPC[0];
                try
                {
                    //拿到附近的NPC資訊了
                    npc = functionList.getNPC(userX, userY);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                final int total = npc.length;
                if (total > 0)
                {
                    for (int top = 0; top < total; top++)
                    {
                        //先比對是否有在地圖上出現過
                        if(checkNPC(npc[top].getId()))
                        {
                            final String npcName = npc[top].getName();
                            final LatLng npcPosition = npc[top].getPosition();

                            //抓NPC圖片
                            final Bitmap bm = npc[top].getPic();

                            //要將原本的圖片轉成ico大小，先拿原本的長寬
                            int oldWidth = bm.getWidth();
                            int oldHeight = bm.getHeight();
                            float newWidth = 128;
                            float newHeight = (float)oldHeight * (newWidth/oldWidth);
                            System.out.println("將原本的長寬 : " + oldHeight +"," + oldWidth +" 轉為 " + newHeight + "," + newWidth);
                            Matrix matrix = new Matrix();
                            matrix.postScale((float)newWidth/oldWidth, (float)newHeight/oldHeight);
                            final Bitmap npcICO = Bitmap.createBitmap(bm, 0, 0, oldWidth,oldHeight, matrix, true);


                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {

                                    //抓到圖片之後直接設定marker 讚讚
                                    Marker npc = mMap.addMarker(
                                            new MarkerOptions()
                                                    .position(npcPosition)
                                                    .title(npcName)
                                                    .icon(BitmapDescriptorFactory.fromBitmap(npcICO))
                                    .zIndex(2));

                                    //設定點擊NPC圖示就會資訊跳出來~
                                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(Marker marker)
                                        {
                                            setMarkerOnclickListener(marker);
                                            // Add the button
                                            return false;
                                        }
                                    });

                                    char_text.setText("發現野生的"+npcName);
                                }
                            });
                            Speech("發現野生的"+npcName);
                            npcOnMap.add(npc[top]);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    private boolean checkNPC(String npcID)  //確認NPC是否有在地圖上出現過
    {
        if(npcOnMap != null)
        {
            System.out.println("發現地圖上已經有npc！");
            for(NPC tmp : npcOnMap)
            {
                if(tmp.getId().equals(npcID))
                {
                    return false;
                }
            }
        }
        return true;
    }

    //利用z-index來判斷marker身分
    private void setMarkerOnclickListener(Marker marker)
    {
        System.out.println("判斷marker狀態 : "+(int)marker.getZIndex());
        switch((int)marker.getZIndex())
        {
            case 0://商家
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                String shopName = marker.getTitle();
                View view = null;

                for(Stores tmp : nearByShops)
                {
                    if(tmp.getName().equals(shopName))
                    {
                        view = getShopView(tmp.getID(),tmp.getName(),tmp.getKind(),tmp.getRating(),tmp.getAddress(),tmp.getDescription());
                    }
                }
                //設定商家頁面，並呈現
                builder.setView(view);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });

                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            }
            case 1://廁所
            {

                break;
            }
            case 2://NPC
            {
                String npcName = marker.getTitle();
                for(NPC tmp : npcOnMap)
                {
                    if(tmp.getName().equals(npcName))
                    {
                        final String mp3URL = tmp.getMp3URL();
                        //播放NPC音檔囉
                        Thread threadForMP3 = new Thread(new Runnable()
                        {
                            public void run()
                            {
                                try {
                                    playAudio(mp3URL);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        threadForMP3.start();

                        final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        final String nameStr = tmp.getName();
                        final Bitmap picBitmap = tmp.getPic();
                        final NPCStory[] storyNPCStory = tmp.getStory();

                        View mView = getLayoutInflater().inflate(R.layout.npc_info, null);
                        //設定大Name抬頭
                        TextView name = (TextView) mView.findViewById(R.id.name);
                        name.setText(nameStr);

                        //設定大頭貼
                        ImageView photo = (ImageView) mView.findViewById(R.id.npc_photo);
                        photo.setImageBitmap(picBitmap);
                        //設定intro文字
                        TextView intro = (TextView) mView.findViewById(R.id.npc_intro);
                        intro.setText(tmp.getIntro());
                        Button story = (Button) mView.findViewById(R.id.story);
                        Button task = (Button) mView.findViewById(R.id.task);
                        Button cancel = (Button) mView.findViewById(R.id.cancel);

                        builder.setView(mView);
                        // Create the AlertDialog
                        final AlertDialog dialog = builder.create();
                        dialog.show();

                        story.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int count = 0;
                                //設定每個分頁，最後一頁為知道了
                                setStoryBoard(count,storyNPCStory.length,storyNPCStory,nameStr,picBitmap);
                            }
                        });

                        task.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                //按下取消對話建，就關閉視窗
                                dialog.dismiss();
                            }
                        });
                    }
                }

                break;
            }
            case 3://自己
            {

                break;
            }
            case 4://路線圖標
            {

                break;
            }
        }

    }

    //用來播放server抓下來的mp3檔
    private void playAudio(String url) throws Exception
    {
        if(mediaPlayer!=null)
        {
            try
            {
                mediaPlayer.release();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(url);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    private void setStoryBoard(int count,int max,NPCStory[] storyNPCStory,String nameStr,Bitmap picBitmap)
    {
        if(count != max-1)
        {
            AlertDialog.Builder build = new AlertDialog.Builder(MapsActivity.this);
            //說故事囉
            final String mp3URL = storyNPCStory[count].getMp3URL();
            //播放NPC音檔囉
            Thread threadForMP3 = new Thread(new Runnable()
            {
                public void run()
                {try {
                    playAudio(mp3URL);
                } catch (Exception e) {
                    e.printStackTrace();
                }}
            });
            threadForMP3.start();
            View view = getLayoutInflater().inflate(R.layout.npc_story, null);
            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(nameStr);
            ImageView photo = (ImageView) view.findViewById(R.id.npc_photo);
            photo.setImageBitmap(picBitmap);
            TextView content = (TextView) view.findViewById(R.id.npc_story);
            content.setText(storyNPCStory[count].getContent());
            build.setView(view);

            //下一頁要用的變數宣告
            final int countNext = count+1;
            final int maxNext = max;
            final NPCStory[] storyNPCStoryNext = storyNPCStory;
            final String nameStrNext = nameStr;
            final Bitmap picBitmapNext = picBitmap;

            build.setPositiveButton("下一頁", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    setStoryBoard(countNext,maxNext,storyNPCStoryNext,nameStrNext,picBitmapNext);
                }
            });
            AlertDialog dialog = build.create();
            dialog.show();
        }
        else
        {
            AlertDialog.Builder build = new AlertDialog.Builder(MapsActivity.this);
            //說故事囉
            final String mp3URL = storyNPCStory[count].getMp3URL();
            //播放NPC音檔囉
            Thread threadForMP3 = new Thread(new Runnable()
            {
                public void run()
                {try {
                    playAudio(mp3URL);
                } catch (Exception e) {
                    e.printStackTrace();
                }}
            });
            threadForMP3.start();
            View view = getLayoutInflater().inflate(R.layout.npc_story, null);
            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(nameStr);
            ImageView photo = (ImageView) view.findViewById(R.id.npc_photo);
            photo.setImageBitmap(picBitmap);
            TextView content = (TextView) view.findViewById(R.id.npc_story);
            content.setText(storyNPCStory[count].getContent());
            build.setView(view);

            build.setPositiveButton("結束對話", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    mediaPlayer.release();
                }
            });
            AlertDialog dialog = build.create();
            dialog.show();
        }

    }
}
