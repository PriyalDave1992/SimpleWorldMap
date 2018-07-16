package com.jibestreamtest.simpleworldmap;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by Priyal Divakala on 14/07/2018.
 */

public class PlaceAutocompleteAdapter extends RecyclerView.Adapter<PlaceAutocompleteAdapter.PlaceViewHolder>
        implements Filterable {

    public interface PlaceAutoCompleteInterface {
        void onPlaceClick(ArrayList<PlaceAutocomplete> mResultList, int position);
    }

    private Context mContext;
    private PlaceAutoCompleteInterface mListener;
    private static final String TAG = PlaceAutocompleteAdapter.class.getSimpleName();
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private ArrayList<PlaceAutocomplete> mResultList;

    private GoogleApiClient mGoogleApiClient;

    private int layout;

    private AutocompleteFilter mPlaceFilter;


    PlaceAutocompleteAdapter(Context context, int resource, GoogleApiClient googleApiClient,
                             AutocompleteFilter filter) {
        this.mContext = context;
        layout = resource;
        mGoogleApiClient = googleApiClient;
        mPlaceFilter = filter;
        this.mListener = (PlaceAutoCompleteInterface) mContext;
    }

    /*
    Clear List items
     */
    void clearList() {
        if (mResultList != null && mResultList.size() > 0) {
            mResultList.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    mResultList = getAutocomplete(constraint);
                    if (mResultList != null) {
                        // The API successfully returned results.
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    notifyDataSetChanged();
                }
            }
        };
    }

    private ArrayList<PlaceAutocomplete> getAutocomplete(CharSequence constraint) {
        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG, "Starting autocomplete query for: " + constraint);

            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                                    null, mPlaceFilter);

            // This method should have been called off the main UI thread. Block and wait for at most 60s
            // for a result from the API.
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);

            // Confirm that the query completed successfully, otherwise return null
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Log.e(TAG, "Error getting autocomplete prediction API call: " + status.toString());
                autocompletePredictions.release();
                return null;
            }
            Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");

            // Copy the results into our own data structure, because we can't hold onto the buffer.
            // AutocompletePrediction objects encapsulate the API response (place ID, primary text, secondary text)

            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList<PlaceAutocomplete> resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                // Get the details of this prediction and copy it into a new PlaceAutocomplete object
                StringBuilder primaryText = new StringBuilder();
                StringBuilder secondaryText = new StringBuilder();
                if (prediction.getPrimaryText(null).length() > 29) {
                    primaryText.append(prediction.getPrimaryText(null).subSequence(0, 29).toString());
                    primaryText.append("...");
                } else {
                    primaryText.append(prediction.getPrimaryText(null));
                }
                if (prediction.getSecondaryText(null).length() > 50) {
                    secondaryText.append(prediction.getSecondaryText(null).subSequence(0, 51).toString());
                    secondaryText.append("...");
                } else {
                    secondaryText.append(prediction.getSecondaryText(null));
                }
                resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                        primaryText.toString(), secondaryText.toString()));
            }
            // Release the buffer now that all data has been copied.
            autocompletePredictions.release();
            return resultList;
        }
        Log.e(TAG, "Google API client is not connected for autocomplete query.");
        return null;
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = null;
        if (layoutInflater != null) {
            convertView = layoutInflater.inflate(layout, viewGroup, false);
        }
        return new PlaceViewHolder(convertView);
    }


    @Override
    public void onBindViewHolder(PlaceViewHolder mPredictionHolder, final int i) {

        mPredictionHolder.mAddress.setText(mResultList.get(i).primaryAddress);
        mPredictionHolder.mSecAddress.setText(mResultList.get(i).secondaryAddress);
        mPredictionHolder.mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPlaceClick(mResultList, i);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mResultList != null)
            return mResultList.size();
        else
            return 0;
    }

    /*
    View Holder for each search result
     */
    class PlaceViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout mParentLayout;
        TextView mAddress;
        TextView mSecAddress;

        PlaceViewHolder(View itemView) {
            super(itemView);
            mParentLayout = itemView.findViewById(R.id.predictedRow);
            mAddress = itemView.findViewById(R.id.address);
            mSecAddress = itemView.findViewById(R.id.tv_sec_address);
        }

    }

    /**
     * Holder for Places Geo Data Autocomplete API results
     */
    class PlaceAutocomplete {

        CharSequence placeId;
        CharSequence primaryAddress;
        CharSequence secondaryAddress;

        PlaceAutocomplete(CharSequence placeId, CharSequence primaryAddress, CharSequence secondaryAddress) {
            this.placeId = placeId;
            this.primaryAddress = primaryAddress;
            this.secondaryAddress = secondaryAddress;
        }
    }
}