package com.jibestreamtest.simpleworldmap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Priyal Divakala on 14/07/2018.
 */

public class SearchActivity extends AppCompatActivity implements
        PlaceAutocompleteAdapter.PlaceAutoCompleteInterface, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, View.OnClickListener, PlaceSelectionListener {

    Context mContext;
    GoogleApiClient mGoogleApiClient;
    private RecyclerView mRecyclerView;
    LinearLayoutManager llm;
    PlaceAutocompleteAdapter mAdapter;
    EditText mSearchEditText;
    ImageView mClear;
    private String TAG = SearchActivity.class.getSimpleName();

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mContext = SearchActivity.this;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();
        initViews();
    }

    /*
   Initialize Views
    */
    private void initViews() {
        mRecyclerView = findViewById(R.id.list_search);
        mRecyclerView.setHasFixedSize(true);
        llm = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(llm);

        mSearchEditText = findViewById(R.id.search_et);
        mClear = findViewById(R.id.clear);
        mClear.setOnClickListener(this);

        mAdapter = new PlaceAutocompleteAdapter(this, R.layout.view_placesearch,
                mGoogleApiClient, null);
        mRecyclerView.setAdapter(mAdapter);

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    if (mAdapter != null) {
                        mRecyclerView.setAdapter(mAdapter);
                    }
                }
                if (!s.toString().equals("") && mGoogleApiClient.isConnected()) {
                    mAdapter.getFilter().filter(s.toString());
                } else if (!mGoogleApiClient.isConnected()) {
                    Log.e(TAG, "NOT CONNECTED");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v == mClear) {
            if (!mSearchEditText.getText().toString().equals("")) {
                mSearchEditText.setText("");
                if (mAdapter != null) {
                    mAdapter.clearList();
                }
            } else {
                this.finish();
            }

        }
    }
    //Method overridden from PlaceAutocompleteAdapter.PlaceAutoCompleteInterface
    @Override
    public void onPlaceClick(ArrayList<PlaceAutocompleteAdapter.PlaceAutocomplete> mResultList, int position) {
        if (mResultList != null) {
            try {
                final String placeId = String.valueOf(mResultList.get(position).placeId);
                //Issue a request to the Places Geo Data API
                // to retrieve a Place object with additional details about the place.
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        if (places.getCount() == 1) {
                            Intent data = new Intent();
                            int zoomLevel = 0;
                            List<Integer> placeTypes = places.get(0).getPlaceTypes();
                            if(placeTypes!=null) {
                                if (placeTypes.contains(Place.TYPE_COUNTRY) || placeTypes.contains(Place.TYPE_POLITICAL))
                                    zoomLevel = 5;
                                else if (placeTypes.contains(Place.TYPE_POINT_OF_INTEREST) || placeTypes.contains(Place.TYPE_ESTABLISHMENT))
                                    zoomLevel = 18;
                                else
                                    zoomLevel = 14;
                            }//1013//34
                            data.putExtra("lat", String.valueOf(places.get(0).getLatLng().latitude));
                            data.putExtra("lng", String.valueOf(places.get(0).getLatLng().longitude));
                            data.putExtra("zoomlevel", zoomLevel);
                            if(places.get(0).getName().length() > 16)
                              data.putExtra("placename", String.valueOf(places.get(0).getName().subSequence(0,16))+"...");
                            else
                                data.putExtra("placename", String.valueOf(places.get(0).getName()));

                            setResult(SearchActivity.RESULT_OK, data);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG,e.getMessage());
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onPlaceSelected(Place place) {
        try {
            Intent data = new Intent();
            data.putExtra("lat", String.valueOf(place.getLatLng().latitude));
            data.putExtra("lng", String.valueOf(place.getLatLng().longitude));
            setResult(SearchActivity.RESULT_OK, data);
            finish();
        } catch (Exception e) {
            Log.e(TAG,"Error in sending data to MapsActivity");
            Toast.makeText(mContext,"Error in selecting a place",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(Status status) {
        Log.e(TAG,"Error in place selection: "+status.getStatusMessage());
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG,"Connection to Google Maps API failed: "+connectionResult.getErrorMessage());
    }
}
