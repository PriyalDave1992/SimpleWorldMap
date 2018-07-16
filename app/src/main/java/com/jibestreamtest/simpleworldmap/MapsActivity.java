package com.jibestreamtest.simpleworldmap;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
    /**
     * Default marker position when the activity is first created. Pointing to Jibestream, Toronto by default
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
            // Objects from the API (eg. LatLng, MarkerOptions, etc.) were stored directly in
            //  the saveInstanceState Bundle. Custom Parcelable objects were wrapped in another Bundle
            mMarkerPosition = savedInstanceState.getParcelable(MARKER_POSITION);
            mPlaceName = savedInstanceState.getString(MARKER_TITLE);
            Bundle bundle = savedInstanceState.getBundle(OTHER_OPTIONS);
            if (bundle != null) {
                mMarkerInfo = bundle.getParcelable(MARKER_INFO);
            }
            mMoveCameraToMarker = false;
        }
        initViews();
    }
    //Called when device is rotated
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
    private void initViews()
    {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mEditText = findViewById(R.id.edt_search_query);
        mEditText.setOnClickListener(this);

        ImageView ivClear = findViewById(R.id.iv_clear);
        ivClear.setOnClickListener(this);
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used
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
    @Override
    public boolean onMarkerClick(Marker marker) {
        float newHue = MARKER_HUES[new Random().nextInt(MARKER_HUES.length)];
        mMarkerInfo.mHue = newHue;
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(newHue));
        marker.setTitle(mPlaceName);
        marker.showInfoWindow();
        return true;
    }
    //Will be called when EditText or 'X' button is clicked
    @Override
    public void onClick(View v) {
        if(v instanceof ImageView) {
            mEditText.setText("");
            mPlaceName = "";
            mMarkerPosition = new LatLng(0,0);
            mMap.clear();
        }
        else {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivityForResult(intent, 1);
        }
    }
    //Will be called when place from second screen is selected
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

    //Makes the toolbar and status bar transparent
    private void formatToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
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
