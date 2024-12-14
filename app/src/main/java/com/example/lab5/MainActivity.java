package com.example.lab5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {


    static String url = "https://ntv.ifmo.ru/file/journal/2.pdf";
    InputStream is = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClickBtn1(View view) {
        new DownloadFileTask().execute("https://ntv.ifmo.ru/file/journal/1.pdf");
        Toast toast = Toast.makeText(MainActivity.this,
                "Btn1", Toast.LENGTH_SHORT);
        toast.show();
    }

    private class DownloadFileTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder buf = new StringBuilder();
            String urlString = urls[0];
            BufferedReader reader = null;
            InputStream stream = null;
            HttpsURLConnection connection = null;

            try {
                URL url = new URL(urlString);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.connect();
                stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buf.append(line).append("\n");
                }
            } catch (Exception e) {
                Log.e("DownloadFileTask", "Error downloading file", e);
                return null;
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (stream != null) stream.close();
                    if (connection != null) connection.disconnect();
                } catch (IOException e) {
                    Log.e("DownloadFileTask", "Error closing streams", e);
                }
            }
            return buf.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            // Здесь вы можете обновить пользовательский интерфейс или обработать загруженные данные
            if (result != null) {
                Toast toast = Toast.makeText(MainActivity.this,
                        "Получил что то", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}