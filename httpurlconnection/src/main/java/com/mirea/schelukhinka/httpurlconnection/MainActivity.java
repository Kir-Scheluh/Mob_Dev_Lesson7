package com.mirea.schelukhinka.httpurlconnection;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mirea.schelukhinka.httpurlconnection.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
        private ActivityMainBinding binding;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            binding.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ConnectivityManager connectivityManager =
                            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkinfo = null;
                    if (connectivityManager != null) {
                        networkinfo = connectivityManager.getActiveNetworkInfo();
                    }
                    if (networkinfo != null && networkinfo.isConnected()) {
                        new DownloadPageTask().execute("https://ipinfo.io/json"); // запуск нового по-
                    } else {
                        Toast.makeText(getApplicationContext(), "Нет интернета", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    private class DownloadPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Загружаем...", Toast.LENGTH_SHORT).show();
        }
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadIpInfo(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject responseJson = new JSONObject(result);
                binding.textViewIP.setText("ip: "+responseJson.getString("ip"));
                binding.textViewCity.setText("Город: "+responseJson.getString("city"));
                binding.textViewCountry.setText("Регион: "+responseJson.getString("country"));
                String cord=responseJson.getString("loc");
                binding.textViewLongitude.setText("Долгота: "+ cord.substring(0,5));
                binding.textViewLatitude.setText("Широта: "+ cord.substring(8,13));
                String weather="https://api.open-meteo.com/v1/forecast?latitude="+cord.substring(0,5)+"&longitude="+cord.substring(8,13)+"&current_weather=true";
                new DownloadPageTask2().execute(weather);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }
    }
    private String downloadIpInfo(String address) throws IOException {
        InputStream inputStream = null;
        String data = "";
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(100000);
            connection.setConnectTimeout(100000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                inputStream = connection.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int read = 0;
                while ((read = inputStream.read()) != -1) {
                    bos.write(read);
                }
                bos.close();
                data = bos.toString();
            } else {
                data = connection.getResponseMessage() + ". Error Code: " + responseCode;
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return data;
    }
    private class DownloadPageTask2 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadIpInfo(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject responseJson = new JSONObject(result);
                JSONObject current_weather = responseJson.getJSONObject("current_weather");
                binding.textViewWeather.setText("Температура сейчас по Цельсию: " + current_weather.getString("temperature"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }
    }
}
