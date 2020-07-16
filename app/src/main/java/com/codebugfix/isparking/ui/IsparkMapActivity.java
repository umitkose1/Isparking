package com.codebugfix.isparking.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.codebugfix.isparking.R;
import com.codebugfix.isparking.model.IsparkDetail;
import com.codebugfix.isparking.model.IsparkList;
import com.codebugfix.isparking.network.RetrofitClient;
import com.codebugfix.isparking.network.ServiceAPI;
import com.codebugfix.isparking.util.Constants;
import com.codebugfix.isparking.util.NetworkRequestManager;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.SupportMapFragment;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.CameraPosition;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.LatLngBounds;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.maps.model.Polyline;
import com.huawei.hms.maps.model.PolylineOptions;
import com.huawei.hms.maps.util.LogM;
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IsparkMapActivity extends AppCompatActivity   {

    private static final String TAG = "MapViewDemoActivity";

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private LinearLayout timelineBottomSheet;
    private AnchorBottomSheetBehavior timelineBottomSheetBehaviorIspark;
    private TextView isparkNametextView,isparkLocationtextView,isparkCapacitytextView,isparkEmptytextView,isparkTypetextView,isparkAddresstextView;
    private Button navigateButton,showRouteButton;
    private double lat,lng;
    private double myLat,myLng;
    private int PERMISSION_ID = 44;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SettingsClient settingsClient;
    private LocationRequest mLocationRequest;

    private HuaweiMap hmap;
    private MapView mMapView;

    private List<List<LatLng>> mPaths = new ArrayList<>();

    private LatLngBounds mLatLngBounds;
    private Marker mMarkerOrigin;

    private Marker mMarkerDestination;
    private List<Polyline> mPolylines = new ArrayList<>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    renderRoute(mPaths, mLatLngBounds);
                    break;
                case 1:
                    Bundle bundle = msg.getData();
                    String errorMsg = bundle.getString("errorMsg");
                    Toast.makeText(IsparkMapActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private static final String[] RUNTIME_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogM.d(TAG, "map onCreate:");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isparkmap);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        assert supportMapFragment != null;


        isparkNametextView = findViewById(R.id.textview_isparkName);
        isparkLocationtextView = findViewById(R.id.textview_locationName);
        isparkCapacitytextView = findViewById(R.id.textview_capacity);
        isparkEmptytextView = findViewById(R.id.textview_emptyCapacity);
        isparkTypetextView = findViewById(R.id.textview_parkType);
        isparkAddresstextView = findViewById(R.id.textview_address);
        navigateButton = findViewById(R.id.button_navigate);
        showRouteButton = findViewById(R.id.button_showroute);
        mMapView = findViewById(R.id.mapView);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(IsparkMapActivity.this);
        settingsClient = LocationServices.getSettingsClient(this);
        getLastLocation();

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);

        // get map by async method
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(HuaweiMap map) {
                Log.d(TAG, "onMapReady: ");
                // after call getMapAsync method ,we can get HuaweiMap instance in this call back method
                hmap = map;
                // move camera by CameraPosition param ,latlag and zoom params can set here
                LatLng turkey = new LatLng(Constants.TURKEY_LAT,Constants.TURKEY_LNG);
                CameraPosition build = new CameraPosition.Builder().target(turkey).zoom(5).build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(build);
                hmap.animateCamera(cameraUpdate);

                ServiceAPI service = RetrofitClient.getClient().create(ServiceAPI.class);
                Call<List<IsparkList>> isparkList = service.getIspark();
                isparkList.enqueue(new Callback<List<IsparkList>>() {
                    @Override
                    public void onResponse(Call<List<IsparkList>> call, Response<List<IsparkList>> response) {

                        if (response != null){

                            List<IsparkList> repoList=new ArrayList<>();
                            repoList=response.body();
                            for (int i = 0 ;  i<repoList.size(); i++){
                                if (repoList.get(i).getBosKapasite() == 0)
                                {
                                    hmap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(repoList.get(i).getLatitude()),Double.valueOf(repoList.get(i).getLongitude()))).title(repoList.get(i).getParkAdi())
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.park_icon_dolu)).snippet(String.valueOf(repoList.get(i).getParkID()))
                                            .clusterable(true));
                                }
                                else if (repoList.get(i).getBosKapasite() >= (repoList.get(i).getKapasitesi() / 2))
                                {
                                    hmap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(repoList.get(i).getLatitude()),Double.valueOf(repoList.get(i).getLongitude()))).title(repoList.get(i).getParkAdi())
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.park_icon_bo)).snippet(String.valueOf(repoList.get(i).getParkID()))
                                            .clusterable(true));
                                }
                                else
                                {
                                    hmap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(repoList.get(i).getLatitude()),Double.valueOf(repoList.get(i).getLongitude()))).title(repoList.get(i).getParkAdi())
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.park_icon_yar_m_dolu)).snippet(String.valueOf(repoList.get(i).getParkID()))
                                            .clusterable(true));
                                }
                            }
                            // Set markers clusterable
                            hmap.setMarkersClustering(true);

                            hmap.setOnMarkerClickListener(new HuaweiMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    if (marker.getTitle() != null && !marker.getSnippet().startsWith("latitude")){

                                        ServiceAPI service = RetrofitClient.getClient().create(ServiceAPI.class);
                                        int result = Integer.parseInt(marker.getSnippet());

                                        Call<IsparkDetail> isparkDetailModel = service.getIsparkDetail(result);

                                        final ProgressBar progressBar;
                                        progressBar = new ProgressBar(IsparkMapActivity.this);
                                        progressBar.setMax(100);
                                        progressBar.setVisibility(View.VISIBLE);

                                        isparkDetailModel.enqueue(new Callback<IsparkDetail>() {
                                            @Override
                                            public void onResponse(Call<IsparkDetail> call, Response<IsparkDetail> response) {
                                                if (response!=null){


                                                    isparkNametextView.setText(response.body().getParkAdi());
                                                    isparkLocationtextView.setText(response.body().getIlce());
                                                    isparkCapacitytextView.setText(String.valueOf(response.body().getKapasitesi()));
                                                    isparkEmptytextView.setText(String.valueOf(response.body().getBosKapasite()));
                                                    isparkTypetextView.setText(response.body().getParkTipi());
                                                    isparkAddresstextView.setText(response.body().getAdres());
                                                    lat = Double.valueOf(response.body().getLatitude());
                                                    lng = Double.valueOf(response.body().getLongitude());
                                                }
                                                //   progressDoalog.dismiss();
                                                progressBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onFailure(Call<IsparkDetail> call, Throwable t) {
                                                //   progressDoalog.dismiss();
                                                progressBar.setVisibility(View.GONE);


                                            }
                                        });


                                        timelineBottomSheetBehaviorIspark.setState(AnchorBottomSheetBehavior.STATE_EXPANDED);
                                        timelineBottomSheetBehaviorIspark.addBottomSheetCallback(new AnchorBottomSheetBehavior.SimpleBottomSheetCallback() {
                                            @Override
                                            public void onStateChanged(@NonNull View bottomSheet, int oldState, int newState) {
                                                switch (newState) {
                                                    case AnchorBottomSheetBehavior.STATE_ANCHORED:
                                                        break;
                                                    case AnchorBottomSheetBehavior.STATE_COLLAPSED:
                                                        break;
                                                    case AnchorBottomSheetBehavior.STATE_EXPANDED:
                                                        break;
                                                }                                    }

                                            @Override
                                            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                                                super.onSlide(bottomSheet, slideOffset);
                                            }
                                        });
                                    }
                                    return false;
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<IsparkList>> call, Throwable t) {
                    }
                });
            }
        });

        timelineBottomSheet = findViewById(R.id.bottom_sheet);
        timelineBottomSheetBehaviorIspark = AnchorBottomSheetBehavior.from(timelineBottomSheet);
        timelineBottomSheetBehaviorIspark.setHideable(true);
        timelineBottomSheetBehaviorIspark.setAnchorOffset((int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.382));
        timelineBottomSheetBehaviorIspark.setAllowUserDragging(true);

        showRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDrivingRouteResult();
            }
        });

        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoNavigation(myLat,myLng);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }


    @Override
    protected void onPause() {
        mMapView.onPause();
        if (checkPermissions()) {
            getLastLocation();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
        mMapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (fusedLocationProviderClient.getLastLocation().getResult() == null)
                {
                    requestNewLocationData();
                }
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new com.huawei.hmf.tasks.OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(com.huawei.hmf.tasks.Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {

                            myLat = location.getLatitude();
                            myLng = location.getLongitude();
                            if (hmap != null){
                                hmap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(myLat,myLng) , 12.0f) );
                                hmap.setMyLocationEnabled(true);
                            }
                        }
                    }
                });

            } else {

                Toast.makeText(getApplicationContext(),"Lütfen konumunuzu aktif ediniz.",Toast.LENGTH_LONG).show();

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){


        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);

        requestLocationUpdatesWithCallback();

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            myLat = mLastLocation.getLatitude();
            myLng = mLastLocation.getLongitude();
            if (hmap != null){
                hmap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(myLat,myLng) , 12.0f) );
                hmap.setMyLocationEnabled(true);
            }
      //      hmap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(myLat,myLng) , 12.0f) );

        }
    };
    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(IsparkMapActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    private void gotoNavigation(double latitude,double longtitude){

        String uri = String.format(Locale.ENGLISH,"yandexnavi://build_route_on_map?lat_from=%f&lon_from=%f&lat_to=%f&lon_to=%f",latitude,longtitude,lat,lng);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("ru.yandex.yandexnavi");

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        if (isIntentSafe) {
            startActivity(intent);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=ru.yandex.yandexnavi"));
            startActivity(intent);
        }
    }

    private void requestLocationUpdatesWithCallback() {
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();
            // check devices settings before request location updates.
            settingsClient.checkLocationSettings(locationSettingsRequest)
                    .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            Log.i(TAG, "check location settings success");
                            //request location updates
                            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "requestLocationUpdatesWithCallback onSuccess");
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            Log.e(TAG,
                                                    "requestLocationUpdatesWithCallback onFailure:" + e.getMessage());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "checkLocationSetting onFailure:" + e.getMessage());
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(IsparkMapActivity.this, 0);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Log.e(TAG, "PendingIntent unable to execute request.");
                                    }
                                    break;
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "requestLocationUpdatesWithCallback exception:" + e.getMessage());
        }
    }

    public void getDrivingRouteResult() {
        removePolylines();
        LatLng latLngDest = new LatLng(lat,lng);
        LatLng latLngOrign = new LatLng(myLat,myLng);

        NetworkRequestManager.getDrivingRoutePlanningResult(latLngOrign, latLngDest,
                new NetworkRequestManager.OnNetworkListener() {
                    @Override
                    public void requestSuccess(String result) {
                        generateRoute(result);
                    }

                    @Override
                    public void requestFail(String errorMsg) {
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putString("errorMsg", errorMsg);
                        msg.what = 1;
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                });
    }


    private void generateRoute(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray routes = jsonObject.optJSONArray("routes");
            if (null == routes || routes.length() == 0) {
                return;
            }
            JSONObject route = routes.getJSONObject(0);

            // get route bounds
            JSONObject bounds = route.optJSONObject("bounds");
            if (null != bounds && bounds.has("southwest") && bounds.has("northeast")) {
                JSONObject southwest = bounds.optJSONObject("southwest");
                JSONObject northeast = bounds.optJSONObject("northeast");
                LatLng sw = new LatLng(southwest.optDouble("lat"), southwest.optDouble("lng"));
                LatLng ne = new LatLng(northeast.optDouble("lat"), northeast.optDouble("lng"));
                mLatLngBounds = new LatLngBounds(sw, ne);
            }

            // get paths
            JSONArray paths = route.optJSONArray("paths");
            for (int i = 0; i < paths.length(); i++) {
                JSONObject path = paths.optJSONObject(i);
                List<LatLng> mPath = new ArrayList<>();

                JSONArray steps = path.optJSONArray("steps");
                for (int j = 0; j < steps.length(); j++) {
                    JSONObject step = steps.optJSONObject(j);

                    JSONArray polyline = step.optJSONArray("polyline");
                    for (int k = 0; k < polyline.length(); k++) {
                        if (j > 0 && k == 0) {
                            continue;
                        }
                        JSONObject line = polyline.getJSONObject(k);
                        double lat = line.optDouble("lat");
                        double lng = line.optDouble("lng");
                        LatLng latLng = new LatLng(lat, lng);
                        mPath.add(latLng);
                    }
                }
                mPaths.add(i, mPath);
            }
            mHandler.sendEmptyMessage(0);

        } catch (JSONException e) {
            Log.e(TAG, "JSONException" + e.toString());
        }
    }
    private void removePolylines() {
        for (Polyline polyline : mPolylines) {
            polyline.remove();
        }

        mPolylines.clear();
        mPaths.clear();
        mLatLngBounds = null;
    }


    /**
     * Render the route planning result
     *
     * @param paths
     * @param latLngBounds
     */
    private void renderRoute(List<List<LatLng>> paths, LatLngBounds latLngBounds) {
        if (null == paths || paths.size() <= 0 || paths.get(0).size() <= 0) {
            return;
        }

        for (int i = 0; i < paths.size(); i++) {
            List<LatLng> path = paths.get(i);
            PolylineOptions options = new PolylineOptions().color(Color.BLUE).width(5);
            for (LatLng latLng : path) {
                options.add(latLng);
            }

            Polyline polyline = hmap.addPolyline(options);
            mPolylines.add(i, polyline);
        }

        addOriginMarker(paths.get(0).get(0));
        addDestinationMarker(paths.get(0).get(paths.get(0).size() - 1));

        if (null != latLngBounds) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 5);
            hmap.moveCamera(cameraUpdate);
        } else {
            hmap.moveCamera(CameraUpdateFactory.newLatLngZoom(paths.get(0).get(0), 13));
        }

    }
    private void addOriginMarker(LatLng latLng) {
        if (null != mMarkerOrigin) {
            mMarkerOrigin.remove();
        }
        mMarkerOrigin = hmap.addMarker(new MarkerOptions().position(latLng)
                .anchor(0.5f, 0.9f)
                // .anchorMarker(0.5f, 0.9f)
                .title("Origin")
                .snippet(latLng.toString()));
    }

    private void addDestinationMarker(LatLng latLng) {
        if (null != mMarkerDestination) {
            mMarkerDestination.remove();
        }
        mMarkerDestination = hmap.addMarker(
                new MarkerOptions().position(latLng).anchor(0.5f, 0.9f).title("Destination").snippet(latLng.toString()));
    }
}