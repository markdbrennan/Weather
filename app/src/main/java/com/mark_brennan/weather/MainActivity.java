package com.mark_brennan.weather;

import android.content.Context;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    Button searchCityButton;
    EditText cityEditText;
    URL weatherUrl;
    String weatherData;
    JSONObject weatherInfoObject;
    Double degreesCelsius;
    TextView temperatureText;
    TextView conditionText;

    public void searchCity(View view) {
        try {
            String city = cityEditText.getText().toString();

            // Encode city name to allow for spaces in name
            String encodedCityName = URLEncoder.encode(city, "UTF-8");

            weatherUrl = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&apikey=246be546aa15f63245c1b909090169f2");

            // Hide soft keyboard
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(cityEditText.getWindowToken(), 0);

            // Call DownloadWeatherData task
            DownloadWeatherData weatherTask = new DownloadWeatherData();
            weatherData = weatherTask.execute(weatherUrl.toString()).get();

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Could not fetch weather", Toast.LENGTH_LONG).show();
        }
    }



    public class DownloadWeatherData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            HttpURLConnection urlConnection;

            try {
                weatherUrl = new URL(urls[0]);
                urlConnection = (HttpURLConnection)weatherUrl.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char)data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Could not fetch weather", Toast.LENGTH_LONG).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                // Convert weather string to JSON
                JSONObject weatherJson = new JSONObject(weatherData);
                String weatherInfo = weatherJson.getString("weather");
                String temperatureInfo = weatherJson.getString("main");

                // Temperature
                JSONObject temperatureJson = new JSONObject(temperatureInfo);
                degreesCelsius = Double.parseDouble(temperatureJson.getString("temp")) - 273.15;
                temperatureText.setText(Math.round(degreesCelsius) + "°C");

                // Condition
                JSONArray weatherInfoArray = new JSONArray(weatherInfo);
                for (int i = 0; i < weatherInfoArray.length(); i++) {
                    weatherInfoObject = weatherInfoArray.getJSONObject(i);
                    conditionText.setText(weatherInfoObject.getString("main"));
                }

            } catch (Exception e) {
                temperatureText.setText("");
                conditionText.setText("");
                Toast.makeText(getApplicationContext(), "Could not fetch weather", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchCityButton = (Button)findViewById(R.id.searchCityButton);
        cityEditText = (EditText)findViewById(R.id.cityEditText);

        temperatureText = (TextView)findViewById(R.id.temperatureText);
        conditionText = (TextView)findViewById(R.id.conditionText);
    }
}