package com.jibestreamtest.simpleworldmap;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, PlaceSelectionListener {

    private GoogleMap mMap;
    private Marker mMarker;
    /** Default marker position when the activity is first created. */
    private static final LatLng DEFAULT_MARKER_POSITION = new LatLng(48.858179, 2.294576);
    /** List of hues to use for the marker */
    private static final float[] MARKER_HUES = new float[]{
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_YELLOW,
            BitmapDescriptorFactory.HUE_GREEN,
            BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_AZURE,
            BitmapDescriptorFactory.HUE_BLUE,
            BitmapDescriptorFactory.HUE_VIOLET,
            BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_ROSE,
    };
    // Bundle keys.
    private static final String OTHER_OPTIONS = "options";

    private static final String MARKER_POSITION = "markerPosition";

    private static final String MARKER_INFO = "markerInfo";

    private LatLng mMarkerPosition;

    private String mPlaceName;

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

        if (savedInstanceState == null) {
            // Activity created for the first time.
            mMarkerPosition = DEFAULT_MARKER_POSITION;
            mMarkerInfo = new MarkerInfo(BitmapDescriptorFactory.HUE_RED);
            mMoveCameraToMarker = true;

        } else {
            // Extract the state of the MapFragment:
            // - Objects from the API (eg. LatLng, MarkerOptions, etc.) were stored directly in
            //   the savedInsanceState Bundle.
            // - Custom Parcelable objects were wrapped in another Bundle.

            mMarkerPosition = savedInstanceState.getParcelable(MARKER_POSITION);

            Bundle bundle = savedInstanceState.getBundle(OTHER_OPTIONS);
            if (bundle != null) {
                mMarkerInfo = bundle.getParcelable(MARKER_INFO);
            }
            mMoveCameraToMarker = false;
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // All Parcelable objects of the API  (eg. LatLng, MarkerOptions, etc.) can be set
        // directly in the given Bundle.
        outState.putParcelable(MARKER_POSITION, mMarkerPosition);

        // All other custom Parcelable objects must be wrapped in another Bundle. Indeed,
        // failing to do so would throw a ClassNotFoundException. This is due to the fact that
        // this Bundle is being parceled (losing its ClassLoader at this time) and unparceled
        // later in a different ClassLoader.
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
        mMarker.showInfoWindow();
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);

        if (mMoveCameraToMarker) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mMarkerPosition));
        }
    }

    @Override
    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place: " + place.getName());
        Toast.makeText(MapsActivity.this,"Place selected: "+place.getName(),Toast.LENGTH_SHORT).show();
        mMarkerPosition  = place.getLatLng();
        mPlaceName = place.getName().toString();
        if(mMarker!=null)
            mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions()
                .position(mMarkerPosition)
                .title(mPlaceName)
                .draggable(true);
        mMarker = mMap.addMarker(markerOptions);
        mMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        pointToPosition(mMarkerPosition);
    }

    @Override
    public void onError(Status status) {
        Log.i(TAG, "An error occurred: " + status);
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        float newHue = MARKER_HUES[new Random().nextInt(MARKER_HUES.length)];
        mMarkerInfo.mHue = newHue;
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(newHue));
        marker.showInfoWindow();
        return true;
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
    //check method
    private void pointToPosition(LatLng position) {
        //Build camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .zoom(5).build();
        //Zoom in and animate the camera.
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

}
