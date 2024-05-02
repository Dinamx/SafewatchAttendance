package com.mix.empattendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mix.empattendance.constants.Constants;
import com.mix.empattendance.models.Presence;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Attendance extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private ImageView imageView;


    private ProgressBar progressBar;
    private RelativeLayout contentLayout;
    Button attendButton;
    Button checkOutButton;

    Button lunchStart;
    Button lunchEnd;


    private FusedLocationProviderClient fusedLocationClient;


    Button breakStart;
    Button breakEnd;

    //        Getting shit Likie the feedbakc text
    EditText feedbackEditText;


    private String encodedImage;

    private String typePresence;


    private Presence presence;
    private String username;
    private int idEmploye;
    private String lastCheck;

    private String btnLabel = "Entree";

    private void check(String lastCheck) {
        if (lastCheck.equalsIgnoreCase("checkin")) {
            this.btnLabel = "Entrer";
        } else {
            this.btnLabel = "Sortir";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            System.out.println("Log out selected");
            // Implement your logout logic here
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        contentLayout = findViewById(R.id.contentLayout);
        progressBar = findViewById(R.id.progressBar);
        showProgress();

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(0xFF6200EE);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestLocationPermissions();

        Intent intent = getIntent();
        this.username = intent.getStringExtra("username");
        this.lastCheck = intent.getStringExtra("lastCheck");

        check(this.lastCheck);

//        Na checkin na checkout

//        Tokony hoe alaina
//        this.idEmploye = 1;
        this.idEmploye = Integer.parseInt(intent.getStringExtra("idEmploye"));


        System.out.println("BONJOUR");

        getLastMouvement(String.valueOf(idEmploye));


        attendButton = findViewById(R.id.attendButton);
        checkOutButton = findViewById(R.id.checkOutButton);
        attendButton.setBackgroundColor(Color.parseColor("#27ae60"));
        checkOutButton.setBackgroundColor(Color.parseColor("#e74c3c"));

        feedbackEditText = findViewById(R.id.feedback);

//        Get the 4 other buttons
        lunchStart = findViewById(R.id.lunchin);
        lunchEnd = findViewById(R.id.lunchout);
        breakStart = findViewById(R.id.breakin);
        breakEnd = findViewById(R.id.breakout);


        // Vérifiez si la permission de la caméra est déjà accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Demandez la permission de la caméra
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        contentLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);


        imageView = findViewById(R.id.imageView);


        attendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typePresence = "entree";
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                resetImage();
            }
        });


        checkOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typePresence = "sortie";
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                System.out.println("Fin");
                resetImage();

            }
        });


        breakStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typePresence = "debut pause";
                System.out.println("Break Start");
                System.out.println(getFeedback());
                definePresence(typePresence);


                resetFeedback();
                resetImage();


            }
        });


        breakEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typePresence = "fin pause";
                System.out.println("Break End");
                System.out.println(getFeedback());
                definePresence(typePresence);
                resetFeedback();
                resetImage();

            }
        });


        lunchStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typePresence = "debut dejeuner";
                System.out.println("Lunch Start");
                System.out.println(getFeedback());
                definePresence(typePresence);
                resetFeedback();
                resetImage();

            }
        });
        lunchEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typePresence = "fin dejeuner";

                System.out.println("Lunch End");
                System.out.println(getFeedback());

                definePresence(typePresence);
                resetFeedback();
                resetImage();

            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            this.encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
            System.out.println("Image compressée en base64 : " + encodedImage);
            imageView.setImageBitmap(imageBitmap);
//            J'aimerai que si c'est sur attendButton que j'ai cliqué , ce soit "entree" et si c'est sur checkOutButton , ce soit "sortie"
            definePresence(typePresence);


        }
    }

    private void checkGpsStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS non activé. Veuillez l'activer pour une localisation précise.", Toast.LENGTH_SHORT).show();
            // Rediriger l'utilisateur vers les paramètres de localisation
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }


    private void defineLocation2() {
//        Check si la location par gps est disponible , si oui , retourne la location par gps
//        Sinon retourne la location par NETWORK
//        Sionn utilise fusedLocationClient
//        Sinon Affiche Localisation Indisponibl
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            System.out.println("NETWORK PROVIDER");
//            Toast.makeText(Attendance.this, "GPS non disponible. Utilisation de la localisation réseau", Toast.LENGTH_SHORT).show();
//            // Utilisez le fournisseur de localisation réseau comme solution de repli
//            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            System.out.println("Printing localisation");
//        }
//        if (location == null){
//            System.out.println("GPS PROVIDER");
//            // Utilisez le fournisseur GPS comme d'habitude
//            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        }
//        Modifie ce code, mais en gros c'est l'idée , et si ca marcjhe toujours pas , il faut un prendre grace a fusedLocationClient ,
//        Mais ca doit retourner un Location peu importe
    }

    private static final int REQUEST_LOCATION_PERMISSION_CODE = 1;
    private void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Demande les permissions si elles ne sont pas déjà accordées
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION_CODE);
        }
    }


    private void defineLocation(Presence presence) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Demande les permissions si elles ne sont pas déjà accordées
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION_CODE);
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Gère le cas où la localisation est obtenue avec succès
                        if (location != null) {
                            System.out.println("Last Location is not null");
                            System.out.println("Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                            presence.setLatitude(location.getLatitude());
                            presence.setLongitude(location.getLongitude());
                            presence.print();
                            sendPresence(presence);
                        } else {
                            // Si la localisation n'est pas disponible, essayez d'utiliser la localisation par réseau
                            try {
                                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                    System.out.println("NETWORK PROVIDER");
                                    System.out.println("Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                                    presence.setLatitude(location.getLatitude());
                                    presence.setLongitude(location.getLongitude());
                                    presence.print();
                                    sendPresence(presence);

                                }
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                        }
                        if (location == null) {
                            Toast.makeText(Attendance.this, "Localisation Indisponible", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Gère le cas où l'obtention de la localisation échoue
                        Toast.makeText(Attendance.this, "Erreur lors de l'obtention de la localisation", Toast.LENGTH_SHORT).show();
                    }
                });
    }










    private void definePresence(String typeAttendance) {
        Presence presence = new Presence();
        presence.setImage(this.encodedImage);
        presence.setIdemploye(this.idEmploye);
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        presence.setDateheure(currentTimestamp);
        presence.setType_attendance(typeAttendance);
        presence.setFeedback(getFeedback());

        defineLocation(presence);




    }



    public void checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Demande les permissions si elles ne sont pas accordées
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            Toast.makeText(this, "Localisation nécessaire", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    private void definePresence1(String typeAttendance){
        // Obtenez la localisation actuelle
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            Toast.makeText(Attendance.this, "Localisation needed", Toast.LENGTH_SHORT).show();
            return;
        }

                Location location = null;
        // Vérifiez si le fournisseur GPS est disponible
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            System.out.println("NETWORK PROVIDER");
            Toast.makeText(Attendance.this, "GPS non disponible. Utilisation de la localisation réseau", Toast.LENGTH_SHORT).show();
            // Utilisez le fournisseur de localisation réseau comme solution de repli
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            System.out.println("Printing localisation");
        }
        if (location == null){
            System.out.println("GPS PROVIDER");
            // Utilisez le fournisseur GPS comme d'habitude
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }






        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            System.out.println("Latitude: " + latitude + ", Longitude: " + longitude);


            Presence presence = new Presence();
            presence.setImage(this.encodedImage); // Assurez-vous que l'attribut image est de type String
            presence.setLatitude(latitude);
            presence.setLongitude(longitude);
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            presence.setDateheure(currentTimestamp);
            presence.setIdemploye(this.idEmploye);

            presence.setType_attendance(typeAttendance);

            presence.setFeedback(getFeedback());


            presence.print();
            sendPresence(presence);

            // Définissez les autres attributs de Presence selon vos besoins

        } else {
            Toast.makeText(Attendance.this, "Localisation indisponible", Toast.LENGTH_SHORT).show();
            System.out.println("La localisation n'est pas disponible");
        }
    }



    private void sendPresence(Presence presence)
    {
        showProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Convertir l'objet Presence en JSON
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("idemploye", presence.getIdemploye());
                        jsonObject.put("type_attendance", presence.getType_attendance());
//                        jsonObject.put("type_attendance", "entree");
                        jsonObject.put("latitude", presence.getLatitude());
                        jsonObject.put("longitude", presence.getLongitude());
                        jsonObject.put("date_presence", presence.getDateheureFormated()); // Convertir Timestamp en long
                        jsonObject.put("image", presence.getImage());
                        jsonObject.put("feedback", presence.getFeedback());
                    } catch (Exception e) {
                        Toast.makeText(Attendance.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    System.out.println(jsonObject);
                    // Préparation de la requête
                    URL url = new URL(Constants.url + "/store");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                    // Envoi du corps de la requête
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes(jsonObject.toString());
                    outputStream.flush();
                    outputStream.close();

                    // Lecture de la réponse
                    int responseCode = connection.getResponseCode();
                    System.out.println("response is "+  responseCode);

                    // Lecture de la réponse
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    System.out.println("Response content: " + content.toString());

                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

//                                Ato no mi update ireo bouton
                                getLastMouvement(String.valueOf(idEmploye));


                                Toast.makeText(Attendance.this, "Attendance done", Toast.LENGTH_SHORT).show();





//                                Intent intent = new Intent(Attendance.this, MainActivity.class);
//                                startActivity(intent);
//                                finish();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finishProgress();

                                Toast.makeText(Attendance.this, "Attendance Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finishProgress();
                            Toast.makeText(Attendance.this, "Erreur d'envoi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // La permission de la caméra a été accordée
                    Toast.makeText(this, "Camera Allowed", Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION);
                    // La permission de la caméra a été refusée
                    Toast.makeText(this, "Please Allow the camera", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // La permission de localisation a été accordée
                    Toast.makeText(this, "Localisation Allowed", Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
                    // La permission de localisation a été refusée
                    Toast.makeText(this, "Localisation is needed", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private String getFeedback() {
        String feedbackText = feedbackEditText.getText().toString();
        return feedbackText.isEmpty() ? " " : feedbackText;
    }

    private void resetFeedback() {
        feedbackEditText.setText("");
    }



    private void setValabilityButton(String latestBreak, String latestLunch, String latestEntrance) {
        // Rendre tous les boutons cliquables par défaut
        lunchStart.setEnabled(true);
        lunchEnd.setEnabled(true);
        breakStart.setEnabled(true);
        breakEnd.setEnabled(true);
        attendButton.setEnabled(true);
        checkOutButton.setEnabled(true);

        // Désactiver les boutons en fonction des dernières actions
        if (latestBreak.equalsIgnoreCase("Break Start")) {
            breakStart.setEnabled(false);
        } else if (latestBreak.equalsIgnoreCase("Break End")) {
            breakEnd.setEnabled(false);
        }

        if (latestLunch.equalsIgnoreCase("Lunch Start")) {
            lunchStart.setEnabled(false);
        } else if (latestLunch.equalsIgnoreCase("Lunch End")) {
            lunchEnd.setEnabled(false);
        }

        if (latestEntrance.equalsIgnoreCase("Check In")) {
            attendButton.setEnabled(false);
        } else if (latestEntrance.equalsIgnoreCase("Check Out")) {
            checkOutButton.setEnabled(false);
        }
    }



    private void getLastMouvement(String idEmp) {
        // URL de l'API pour récupérer les derniers mouvements
        String url = Constants.url+  "/mouvement/" + idEmp; // Assurez-vous que cette URL est correcte

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Gestion des erreurs de réseau ou d'autres échecs
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Attendance.this, "Erreur de réseau", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Traitement de la réponse
                    final String responseData = response.body().string();
                    System.out.println("Response data = " + responseData);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);
                                // Vérifiez si les clés existent dans la réponse JSON
                                if (jsonResponse.has("latestBreak") && jsonResponse.has("latestLunch") && jsonResponse.has("latestentrance")) {
                                    String latestBreak = jsonResponse.getString("latestBreak");
                                    String latestLunch = jsonResponse.getString("latestLunch");
                                    String latestEntrance = jsonResponse.getString("latestentrance");
                                    // Utilisez les données récupérées comme vous le souhaitez
                                    // Par exemple, mettez à jour l'interface utilisateur
                                    setValabilityButton(latestBreak, latestLunch, latestEntrance);
                                    finishProgress();

//                                    Toast.makeText(Attendance.this, "Dernière pause: " + latestBreak, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Attendance.this, "Erreur de traitement des données: clés manquantes", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(Attendance.this, "Erreur de traitement des données: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    // Gestion des erreurs de serveur ou d'autres échecs
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Attendance.this, "Erreur de serveur: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

//    {
//        "latestBreak": "Break End",
//            "latestLunch": "Lunch End",
//            "latestentrance": "Check Out"
//    }

    private void showProgress(){
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
    }
    private void finishProgress(){
        progressBar.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
    }

    private void resetImage() {
        imageView.setImageBitmap(null);
    }




}