package com.example.lab5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
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
    private SharedPreferences mSettings;
    public static final String APP_PREFERENCES_POPUP = "popup_show";
    public static final String settingsFile = "mysettings";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        mSettings = getSharedPreferences(settingsFile, MainActivity.MODE_PRIVATE);
        if (!(mSettings.contains(APP_PREFERENCES_POPUP))) {
            showPopupWindow();
        }
        createDir();

    }

    private void showPopupWindow() {

        // Создание разметки для PopupWindow
        View popupView = getLayoutInflater().inflate(R.layout.popup_view, null);

        // Создание экземпляра PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);

        Button acceptBtn = popupView.findViewById(R.id.button_pop);
        final CheckBox cb = popupView.findViewById(R.id.checkBox1);

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(cb.isChecked()) {
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putInt(APP_PREFERENCES_POPUP, 0);
                    editor.apply();
                }
                popupWindow.dismiss();
            }
        });


        btn2.post(new Runnable() {
            public void run() {
                popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
            }
        });

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
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        // Определяем MIME-тип файла
        String mime = "application/pdf"; // или другой соответствующий тип, например "application/vnd.openxmlformats-officedocument.wordprocessingml.document" для .docx

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chosenIntent = Intent.createChooser(intent, "Открыть PDF");
        startActivity(chosenIntent);
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