package com.jibestreamtest.simpleworldmap;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private static GoogleMap mMap;
    private Marker mMarker;
    private EditText mEditText;
    private ImageView mImageViewEarth;
    private ImageView mImageViewClear;
    private static final LatLng DEFAULT_MARKER_POSITION = new LatLng(56.130365999999995,-106.34677099999999);
    /**
     * List of hues to use for the marker
     */
    private static final float[] MARKER_HUES = new float[]{
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_ORANGE
    };
    // Bundle keys.
    private static final String OTHER_OPTIONS = "options";
    private static final String MARKER_POSITION = "markerPosition";
    private String MARKER_TITLE = "markerTitle";
    private static final String MARKER_INFO = "markerInfo";
    private LatLng mMarkerPosition;
    private String mPlaceName = "---";
    private int ZOOM_LEVEL = 1;
    private MarkerInfo mMarkerInfo;
    private static final String SELECTED_STYLE = "selected_style";
    // Stores the ID of the currently selected style, so that we can re-apply it when
    // the activity restores state, for example when the device changes orientation.
    private int mSelectedStyleId = R.string.style_default;
    // These are simply the string resource IDs for each of the style names. We use them
    // as identifiers when choosing which style to apply.
    private int mStyleIds[] = {
            R.string.style_label_retro,
            R.string.style_label_night,
            R.string.style_label_grayscale,
            R.string.style_default
    };
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
        float getHue() {
            return mHue;
        }
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
            mSelectedStyleId = savedInstanceState.getInt(SELECTED_STYLE);
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
        outState.putInt(SELECTED_STYLE, mSelectedStyleId);
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

        mImageViewClear = findViewById(R.id.iv_clear);
        mImageViewClear.setOnClickListener(this);

        mImageViewEarth = findViewById(R.id.iv_earth);
        mImageViewEarth.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && requestCode == 1) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                mPlaceName = bundle.getString("placename");
                Double lat = Double.parseDouble(bundle.getString("lat"));
                Double lng = Double.parseDouble(bundle.getString("lng"));
                ZOOM_LEVEL = (bundle.getInt("zoomlevel") == 0) ? 5 : bundle.getInt("zoomlevel");
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
            mMap.setOnMarkerClickListener(this);
            setSelectedStyle();
            if(!mPlaceName.equalsIgnoreCase("---")) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(mMarkerPosition)
                        .icon(BitmapDescriptorFactory.defaultMarker(mMarkerInfo.mHue))
                        .draggable(true);
                mMarker = mMap.addMarker(markerOptions);

                if (mMoveCameraToMarker) {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(mMarkerPosition)
                            .zoom(ZOOM_LEVEL).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
            else {
                pointToPosition(DEFAULT_MARKER_POSITION);
            }

    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(mMarkerInfo.getHue() == MARKER_HUES[0])
            mMarkerInfo.mHue = MARKER_HUES[1];
        else
            mMarkerInfo.mHue = MARKER_HUES[0];
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(mMarkerInfo.mHue));
        marker.setTitle(mPlaceName);
        marker.showInfoWindow();
        return true;
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
                .zoom(ZOOM_LEVEL).build();
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
    private void showStylesDialog() {
        // mStyleIds stores each style's resource ID, and we extract the names here, rather
        // than using an XML array resource which AlertDialog.Builder.setItems() can also
        // accept. We do this since using an array resource would mean we would not have
        // constant values we can switch/case on, when choosing which style to apply.
        List<String> styleNames = new ArrayList<>();
        for (int style : mStyleIds) {
            styleNames.add(getString(style));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.style_choose));
        builder.setItems(styleNames.toArray(new CharSequence[styleNames.size()]),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedStyleId = mStyleIds[which];
                        String msg = getString(R.string.style_set_to, getString(mSelectedStyleId));
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);
                        setSelectedStyle();
                    }
                });
        builder.show();
    }
    //Will be called when EditText or 'X' button is clicked
    @Override
    public void onClick(View v) {
        if(v == mImageViewClear) {
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha);
            mImageViewClear.startAnimation(animation);
            mEditText.setText("");
            mPlaceName = "";
            mMarkerPosition = new LatLng(0,0);
            mMap.clear();
        }
        else if(v == mImageViewEarth)
        {
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha);
            mImageViewEarth.startAnimation(animation);
            mSelectedStyleId = mStyleIds[new Random().nextInt(mStyleIds.length)];
            setSelectedStyle();
        }
        else {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivityForResult(intent, 1);
        }
    }
    /**
     * Creates a {@link MapStyleOptions} object via loadRawResourceStyle() (or via the
     * constructor with a JSON String), then sets it on the {@link GoogleMap} instance,
     * via the setMapStyle() method.
     */
    private void setSelectedStyle() {
        MapStyleOptions style;
        switch (mSelectedStyleId) {
            case R.string.style_label_retro:
                // Sets the retro style via raw resource JSON.
                style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_retro);
                break;
            case R.string.style_label_night:
                // Sets the night style via raw resource JSON.
                style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night);
                break;
            case R.string.style_label_grayscale:
                // Sets the grayscale style via raw resource JSON.
                style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_grayscale);
                break;
            case R.string.style_default:
                // Removes previously set style, by setting it to null.
                style = null;
                break;
            default:
                return;
        }
        mMap.setMapStyle(style);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.styled_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_style_choose) {
            showStylesDialog();
        }
        return true;
    }

}
