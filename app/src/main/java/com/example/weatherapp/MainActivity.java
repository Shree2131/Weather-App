package com.example.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    TextView feelslike,displaytemp,displaycity,displaydescrip,displaydate,Humidity,airpressure,Visibility;
    @SuppressLint("StaticFieldLeak")
    public class DownloadTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder(" ");
            URL url;
            HttpsURLConnection urlConnection;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char curr = (char) data;
                    result.append(curr);
                    data = reader.read();
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }
        }
        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String city = jsonObject.getString("name");
                String temp = jsonObject.getString("weather");
                JSONObject tempMain = jsonObject.getJSONObject("main");
                String tempr = tempMain.getString("temp"),description,feelsslike = tempMain.getString("feels_like"),tempmin = tempMain.getString("temp_min"),tempmax = tempMain.getString("temp_max");
                JSONArray arr = new JSONArray(temp);
                JSONObject weatherInfo = arr.getJSONObject(0);
                description = weatherInfo.getString("description");
                description = description.toUpperCase();
                String humid = tempMain.getString("humidity"),vis = jsonObject.getString("visibility"),ap = tempMain.getString("pressure");
                displaytemp.setText(tempr);
                displaycity.setText(city);
                displaydescrip.setText(description);
                feelslike.setText("Feels like\n" + feelsslike + "°C");
                Humidity.setText("Humidity\n" + humid + "%");
                airpressure.setText("Air Pressure\n" + ap + "hPa");
                Visibility.setText("Visibility\n" + vis + "m");
                displaydate.setText("WED 31 AUG" + " " + tempmax +"°C/" + tempmin + "°C");
            }catch (Exception e) {
                e.printStackTrace();
                displaycity.setText("NOT FOUND");
                displaydescrip.setText("!!");
                displaytemp.setText("!! ");
                feelslike.setText("????????");
                Humidity.setText("????????");
                airpressure.setText("NOT FOUND");
                Visibility.setText("CITY");
            }
        }
    }
    public void getWeather(String city,String lat,String lon) {
        Log.i("City",city);
        DownloadTask task = new DownloadTask();

        if (!city.equals("--")) {
            task.execute("https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid=a5c791c46ff8505c3a41a1d0d864d45c&units=metric");
        }
        else {
            task.execute("https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid=a5c791c46ff8505c3a41a1d0d864d45c&units=metric");
        }
    }
    public void searchCity(View view) {
        EditText cityText = findViewById(R.id.citySearch);
        String city = cityText.getText().toString();
        city = city.toLowerCase();
        String firstLetStr = city.substring(0, 1);
        String remLetStr = city.substring(1);
        firstLetStr = firstLetStr.toUpperCase();
        String cityy = firstLetStr + remLetStr;
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(feelslike.getWindowToken(),0);
        getWeather(cityy,"0","0");
    }
    //GPS/////
    LocationManager locationManager;
    LocationListener locationListener;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }
    public void currLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = location -> {
            String  latitude = Double.toString(location.getLatitude());
            String longitude = Double.toString(location.getLongitude());
            getWeather("--",latitude,longitude);
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100, locationListener);
        }
    }
    ///////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        feelslike = findViewById(R.id.feelsLike);
        displaytemp = findViewById(R.id.displayTemp);
        displaycity = findViewById(R.id.displayCity);
        displaydescrip = findViewById(R.id.displayDescrip);
        displaydate = findViewById(R.id.displayDate);
        Humidity = findViewById(R.id.humidity);
        airpressure = findViewById(R.id.airPressure);
        Visibility = findViewById(R.id.visibility);
        currLocation();
        //getWeather("Tokyo","0","0");
    }
}