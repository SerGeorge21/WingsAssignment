package com.example.wingsassignment;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class VenueLoader extends AsyncTaskLoader {

    private String mUrl;

    public VenueLoader(Context context, String url){
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public ArrayList<Venue> loadInBackground() {
        ArrayList<Venue> venues = QueryUtils.fetchVenueData(mUrl);
        return venues;
    }
}
