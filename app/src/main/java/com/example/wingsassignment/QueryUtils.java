package com.example.wingsassignment;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public final class QueryUtils {

    private QueryUtils(){
        //private constructor because no one should ever create QueryUtils object
    }

    public static ArrayList<Venue> fetchVenueData(String requestUrl){
        //Create URL object
        URL url = createUrl(requestUrl);

        //Perform HTTP request to the URL to receive JSON response
        String jsonResponse = null;
        try{
            jsonResponse = makeHttpRequest(url);
        }catch(IOException e){
            e.printStackTrace();
            Log.e("ERROR", "Problem making HTTP request");
        }

        //Extract fields of interest from JSON response
        ArrayList<Venue> venues = extractFeatureFromJson(jsonResponse);

        return venues;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try{
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("ERROR", "Problem building URL ", e);
        }

        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException{
        String jsonResponse = "";

        //if passed url is null return early
        if(url == null){
            return jsonResponse;
        }

        //TODO: maybe make this HTTPS?
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try{//each one of these could throw exception
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /*milliseconds*/);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //response code =200 ==> request successful
            if(urlConnection.getResponseCode()==200){
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }else{
                Log.e("ERROR", "Error response code: " + urlConnection.getResponseCode());

            }
        }catch (IOException e){
            Log.e("ERROR", "Problem retrieving the earthquake JSON results.", e);
        }finally{
            if(urlConnection != null) urlConnection.disconnect();

            if(inputStream !=null) inputStream.close();
        }

        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException{
        StringBuilder output = new StringBuilder();

        if(inputStream != null){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while(line != null){
                output.append(line);
                line = reader.readLine();
            }
        }

        return output.toString(); //jsonResponse
    }

    public static ArrayList<Venue> extractFeatureFromJson (String venueJSON){
        if(TextUtils.isEmpty(venueJSON)) return null;

        //empty ArrayList I can start adding venues to
        ArrayList<Venue> venues = new ArrayList<Venue>();

        try{
            //TODO: ALSO GET BITMAP FROM JSON FOR IMAGE - if there is one
            JSONObject root = new JSONObject(venueJSON); //Converting String to JSONObject

            String response = root.getString("response");
            JSONObject res = new JSONObject(response);

            JSONArray groups = res.getJSONArray("groups");
            JSONObject insideGroups = groups.getJSONObject(0);
            JSONArray venuesArray = insideGroups.getJSONArray("items");
            for(int i=0; i<venuesArray.length(); i++){
                JSONObject insideItems = venuesArray.getJSONObject(i);
                JSONObject venue = insideItems.getJSONObject("venue");
                String name = venue.getString("name");
                JSONObject location = venue.getJSONObject("location");
                String address = " ";
                if(location.has("address")) address = location.getString("address");
                String latitude = location.getString("lat");
                String longitude = location.getString("lng");
                String postalCode = " ";
                if(location.has("postalCode")) postalCode = location.getString("postalCode");
                String city = " ";
                if(location.has("city")) city = location.getString("city");

                Venue v = new Venue(latitude, longitude, name, address, city, postalCode);
                venues.add(v); //THIS SHOULD BE JUST V WHEN I MAKE A VENUE ARRAYLIST

            }

        }catch (JSONException e){
            Log.e("QueryUtils ERROR", "Problem parsing the venue JSON results", e);
        }

        return venues;
    }
}


