package com.example.lab5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    String downloadedFilename = null;
    Button btn2, btn3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        createDir();
    }

    public void onClickBtn1(View view) {

        btn2.setEnabled(false);
        btn3.setEnabled(false);
        EditText editText = (EditText) findViewById(R.id.editTextText);
        String j_id = editText.getText().toString();
        String url = "https://ntv.ifmo.ru/file/journal/";
        String journal_id = j_id+".pdf";
        new DownloadFileTask().execute(url,journal_id);
    }

    public void onClickBtn2(View view) {

        String filepath = new File(path, downloadedFilename).getPath(); // Полный путь к файлу
        File file = new File(filepath);
        //Uri uri = Uri.fromFile(file); // Создаем Uri из файла
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        // Определяем MIME-тип файла
        String mime = "application/pdf"; // или другой соответствующий тип, например "application/vnd.openxmlformats-officedocument.wordprocessingml.document" для .docx

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chosenIntent = Intent.createChooser(intent, "Открыть PDF");
        startActivity(chosenIntent);
        /*
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent); // Запускаем активность
        } else {
            Toast.makeText(this, "Нет приложения для открытия файла", Toast.LENGTH_SHORT).show();
        } */
    }

    public void onClickBtn3(View view) {
        String filepath = new File(path, downloadedFilename).getPath();
        File file = new File(filepath);
        if(file.delete()) {
            Toast toast = Toast.makeText(MainActivity.this,
                    "Файл "+downloadedFilename+" удален", Toast.LENGTH_SHORT);
            toast.show();
            btn2.setEnabled(false);
            btn3.setEnabled(false);
        } else {
            Toast toast = Toast.makeText(MainActivity.this,
                    "Ошибка при удалении файла "+downloadedFilename, Toast.LENGTH_SHORT);
            toast.show();
        };

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
            String filename = "";
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
                    filename = "File" + params[1];
                    File file = new File(path, filename);
                    FileOutputStream fos = new FileOutputStream(file);

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    // Чтение данных
                    while ((bytesRead = stream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    fos.close();
                    return filename;
                } else if (connection.getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                    return null;
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
                    return null;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result != null) {
                Toast toast = Toast.makeText(MainActivity.this,
                        "Файл "+result+" сохранен", Toast.LENGTH_SHORT);
                toast.show();
                btn2.setEnabled(true);
                btn3.setEnabled(true);
                downloadedFilename = result;
            } else {
                Toast toast = Toast.makeText(MainActivity.this,
                        "Журнала с таким номером нет", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}