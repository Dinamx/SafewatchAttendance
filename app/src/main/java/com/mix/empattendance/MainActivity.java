package com.mix.empattendance;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.graphics.Color;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mix.empattendance.R;
import com.mix.empattendance.constants.Constants;
import com.mix.empattendance.models.LastCheck;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {



    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;






    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Safewatch Security Attendance Tracker");




        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Demandez la permission de la caméra
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }



        // Initialisation des vues
        contentLayout = findViewById(R.id.contentLayout);
        progressBar = findViewById(R.id.progressBar);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);



//        loginButton.setBackgroundColor(Color.parseColor("#27ae60"));
        // Couleur verte



        finishProgress();


        // Exemple de bouton pour déclencher l'affichage du ProgressBar
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();


//                Intent intent = new Intent(MainActivity.this, Attendance.class);
//                intent.putExtra("username", "DinaKely");
//                intent.putExtra("lastCheck","2" );
//                intent.putExtra("idEmploye", "2"); // Utilisez respID ici
//                startActivity(intent);


                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (isConnectedToInternet()) {
                        // Afficher le ProgressBar et masquer le contenu

                        showProgress();

                        System.out.println(username + password);
                        login(username,password);

                    } else {
                        Toast.makeText(MainActivity.this, "Please connect to internet", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }



    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

     private void login(String username, String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Constants.url + "/loginuser");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                    String jsonInputString = "{\"email\":\"" + username + "\",\"password\":\"" + password + "\"}";

                    System.out.println("Stuff i s= ");
                    System.out.println(jsonInputString);


// Envoi du corps de la requête
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes(jsonInputString);
                    outputStream.flush();
                    outputStream.close();
                    // Lecture de la réponse
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                        // Fermeture du flux
                        in.close();

                        // Traitement de la réponse
                        final String response = content.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("response is ");
                                System.out.println(response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    // Accéder à l'objet "user"
                                    JSONObject userObject = jsonObject.getJSONObject("user");
                                    // Extraire "id" de l'objet "user"
                                    String userId = userObject.getString("id");
                                    System.out.println("ID Utilisateur: " + userId);



                                    // Extraire "presence" de l'objet JSON principal
                                    String presence = jsonObject.getString("presence");
                                    System.out.println("Présence: " + presence);




                                    Toast.makeText(MainActivity.this, "Log in", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, Attendance.class);
                                    intent.putExtra("username", "DinaKely");
                                    intent.putExtra("lastCheck",presence );
                                    intent.putExtra("idEmploye", userId); // Utilisez respID ici
                                    startActivity(intent);
                                    finish();
                                } catch (JSONException e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Please Try Again", Toast.LENGTH_SHORT).show();
                                finishProgress();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finishProgress();
                            Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }



    private void checkConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Constants.url + "/testco");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET"); // Utilisez GET pour une requête de test
                    connection.setDoInput(true);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                        in.close();

                        final String response = content.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String success = jsonObject.getString("success");
                                    String message = jsonObject.getString("message");
                                    System.out.println("Success: " + success);
                                    System.out.println("Message: " + message);

                                    // Ici, vous pouvez gérer la réponse comme vous le souhaitez
                                    // Par exemple, afficher un Toast pour confirmer la connexion
                                    Toast.makeText(MainActivity.this, "Connection test successful", Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    Toast.makeText(MainActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Connection test failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }//cocu


    private void showProgress(){
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
    }
    private void finishProgress(){
        progressBar.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
    }



}