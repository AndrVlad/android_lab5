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
import java.io.FileWriter;
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

    InputStream is = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClickBtn1(View view) {
        new DownloadFileTask().execute("https://ntv.ifmo.ru/file/journal/1.pdf");
        writeExternalFile();
    }

    public void writeExternalFile() {


        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(MainActivity.this, "Доступ к внешнему хранилищу закрыт: " + Environment.getExternalStorageState(), Toast.LENGTH_SHORT).show();
            return;
        }

        File path = getExternalFilesDir("AppDir");
        if (path != null) {
            path.mkdirs(); // Создаем каталог
            File file = new File(path, "File1.txt");

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("Содержимое файла во внешнем хранилище");
                Toast.makeText(MainActivity.this, "Файл записан: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Ошибка записи файла: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Не удалось создать каталог", Toast.LENGTH_SHORT).show();
        }
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
                        "Загружаю", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(MainActivity.this,
                        "Нет такого журнала", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}