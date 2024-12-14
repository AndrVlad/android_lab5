package com.example.lab5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    File path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClickBtn1(View view) {
        EditText editText = (EditText) findViewById(R.id.editTextText);
        String j_id = editText.getText().toString();
        createDir();
        String url = "https://ntv.ifmo.ru/file/journal/";
        String journal_id = j_id+".pdf";
        new DownloadFileTask().execute(url,journal_id);
    }

    public void createDir() {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(MainActivity.this, "Доступ к внешнему хранилищу закрыт: " + Environment.getExternalStorageState(), Toast.LENGTH_SHORT).show();
            return;
        }

        path = getExternalFilesDir("AppDir");
        if (path != null) {
            path.mkdirs(); // Создаем каталог
        } else {
            Toast.makeText(this, "Не удалось создать каталог", Toast.LENGTH_SHORT).show();
        }
    }

    private class DownloadFileTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuilder buf = new StringBuilder();
            String urlString = params[0]+params[1];
            BufferedReader reader = null;
            InputStream stream = null;
            HttpsURLConnection connection = null;

            try {
                URL url = new URL(urlString);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.connect();
                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    stream = connection.getInputStream();
                    String filename = "File" + params[1];
                    File file = new File(path, filename);
                    FileOutputStream fos = new FileOutputStream(file);

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    // Чтение данных
                    while ((bytesRead = stream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    fos.close();
                } else {
                    return "Ошибка скачивания: " + connection.getResponseCode();
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

            return "Файл успешно сохранен";
        }

        @Override
        protected void onPostExecute(String result) {
            // Здесь вы можете обновить пользовательский интерфейс или обработать загруженные данные
            if (result != null) {
                Toast toast = Toast.makeText(MainActivity.this,
                        "Файл загружен", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(MainActivity.this,
                        "Нет такого журнала", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}