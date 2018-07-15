package com.jibestreamtest.simpleworldmap;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, View.OnClickListener {

    private static GoogleMap mMap;
    private Marker mMarker;
    private EditText mEditText;
    private SupportMapFragment mapFragment;
    /**
     * Default marker position when the activity is first created. Pointing to Jibestream currently
     */
    private static final LatLng DEFAULT_MARKER_POSITION = new LatLng(43.654337999999996, -79.42628100000002);
    /**
     * List of hues to use for the marker
     */
    private static final float[] MARKER_HUES = new float[]{
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_AZURE
    };
    // Bundle keys.
    private static final String OTHER_OPTIONS = "options";
    private static final String MARKER_POSITION = "markerPosition";
    private String MARKER_TITLE = "markerTitle";
    private static final String MARKER_INFO = "markerInfo";
    private LatLng mMarkerPosition;
    private String mPlaceName = "Jibestream";
    private MarkerInfo mMarkerInfo;
    private boolean mMoveCameraToMarker;
    private static String TAG = MapsActivity.class.getSimpleName();
    private int EDIT_TEXT_CODE = 1;
    private GoogleMap MAP_OBJECT;


    /**
     * Extra info about a marker.
     */
    static class MarkerInfo implements Parcelable {

        public static final Parcelable.Creator<MarkerInfo> CREATOR =
                new Parcelable.Creator<MarkerInfo>() {
                    @Override
                    public MarkerInfo createFromParcel(Parcel in) {
                        return new MarkerInfo(in);
                    }

                    @Override
                    public MarkerInfo[] newArray(int size) {
                        return new MarkerInfo[size];
                    }
                };

        float mHue;

        MarkerInfo(float color) {
            mHue = color;
        }

        private MarkerInfo(Parcel in) {
            mHue = in.readFloat();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeFloat(mHue);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        formatToolBar();
        if (savedInstanceState == null) {
            // Activity created for the first time.
            mMarkerPosition = DEFAULT_MARKER_POSITION;
            mMarkerInfo = new MarkerInfo(BitmapDescriptorFactory.HUE_RED);
            mMoveCameraToMarker = true;
        } else {
            // - Objects from the API (eg. LatLng, MarkerOptions, etc.) were stored directly in
            //   the savedInsanceState Bundle.
            // - Custom Parcelable objects were wrapped in another Bundle.
            mMarkerPosition = savedInstanceState.getParcelable(MARKER_POSITION);
            mPlaceName = savedInstanceState.getString(MARKER_TITLE);
            Bundle bundle = savedInstanceState.getBundle(OTHER_OPTIONS);
            if (bundle != null) {
                mMarkerInfo = bundle.getParcelable(MARKER_INFO);
            }
            mMoveCameraToMarker = false;
        }
        initObjects();
    }

    private void initObjects()
    {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mEditText = findViewById(R.id.edt_search_query);
        mEditText.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && requestCode == 1) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                mPlaceName = bundle.getString("placename");
                Double lat = Double.parseDouble(bundle.getString("lat"));
                Double lng = Double.parseDouble(bundle.getString("lng"));
                mMarkerPosition = new LatLng(lat, lng);
                if(mMap!=null)
                    setSelectedPlace();
            }
        }
    }

    private void formatToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // All Parcelable objects of the API  (eg. LatLng, MarkerOptions, etc.) can be set
        // directly in the given Bundle.
        outState.putParcelable(MARKER_POSITION, mMarkerPosition);
        outState.putString(MARKER_TITLE, mPlaceName);
        // All other custom Parcelable objects must be wrapped in another Bundle
        Bundle bundle = new Bundle();
        bundle.putParcelable(MARKER_INFO, mMarkerInfo);
        outState.putBundle(OTHER_OPTIONS, bundle);
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
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(mMarkerPosition)
                    .icon(BitmapDescriptorFactory.defaultMarker(mMarkerInfo.mHue))
                    .draggable(true);
            mMarker = mMap.addMarker(markerOptions);
            mMap.setOnMarkerDragListener(this);
            mMap.setOnMarkerClickListener(this);
            if (mMoveCameraToMarker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(mMarkerPosition));
            }
    }

    public void setSelectedPlace() {
        Log.i(TAG, "Place: " + mPlaceName);
        Toast.makeText(MapsActivity.this, "Place selected: " + mPlaceName, Toast.LENGTH_SHORT).show();
        mEditText.setText(mPlaceName);
        mMoveCameraToMarker = true;
        if (mMarker != null)
            mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions()
                .position(mMarkerPosition)
                .title(mPlaceName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .draggable(true);

        mMarker = mMap.addMarker(markerOptions);
        pointToPosition(mMarkerPosition);
    }

    //Points to required position with animating camera
    private void pointToPosition(LatLng position) {
        //Build camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .zoom(5).build();
        //Zoom in and animate the camera.
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    public void onError(Status status) {
        Log.i(TAG, "An error occurred: " + status);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        float newHue = MARKER_HUES[new Random().nextInt(MARKER_HUES.length)];
        mMarkerInfo.mHue = newHue;
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(newHue));
        marker.setTitle(mPlaceName);
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent,EDIT_TEXT_CODE);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }


}
