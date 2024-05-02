package com.mix.empattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class PhoneInfoActivity extends AppCompatActivity {






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);


        // Trouver l'élément de menu par son ID
        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        // Changer le titre de l'élément de menu
        logoutItem.setTitle("Log in");

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
        setContentView(R.layout.activity_phone_info);

        // Obtenez les informations du téléphone
        String deviceModel = Build.MODEL;
        String deviceManufacturer = Build.MANUFACTURER;
        String deviceBrand = Build.BRAND;
        String deviceHardware = Build.HARDWARE;
        String deviceSerial = getDeviceSerialNumber();
        String deviceSoftware = Build.VERSION.RELEASE;
        String androidVersion = Build.VERSION.RELEASE; // Ajout de la version d'Android
        int sdkVersion = Build.VERSION.SDK_INT; // Numéro de version SDK

// Afficher la version du système d'exploitation et le numéro de version SDK
        System.out.println("Numéro de version SDK : " + sdkVersion);

        // Affichez les informations dans des TextViews
        TextView modelTextView = findViewById(R.id.modelTextView);
        TextView manufacturerTextView = findViewById(R.id.manufacturerTextView);
        TextView brandTextView = findViewById(R.id.brandTextView);
        TextView hardwareTextView = findViewById(R.id.hardwareTextView);
        TextView serialTextView = findViewById(R.id.serialTextView);
        TextView softwareTextView = findViewById(R.id.softwareTextView);
        TextView androidVersionTextView = findViewById(R.id.androidVersionTextView); // Assurez-vous d'avoir un TextView pour la version d'Android dans votre layout
        TextView sdkVersionTextView = findViewById(R.id.sdkVersionTextView); // Assurez-vous d'avoir un TextView pour la version d'Android dans votre layout

        modelTextView.setText("Modèle: " + deviceModel);
        manufacturerTextView.setText("Fabricant: " + deviceManufacturer);
        brandTextView.setText("Marque: " + deviceBrand);
        hardwareTextView.setText("Matériel: " + deviceHardware);
        serialTextView.setText("Numéro de série: " + deviceSerial);
        softwareTextView.setText("Logiciel: " + deviceSoftware);
        androidVersionTextView.setText("Version d'Android: " + androidVersion); // Affichez la version d'Android
        sdkVersionTextView.setText("Sdk: " + sdkVersion); // Affichez la version d'Android
    }

    private String getDeviceSerialNumber() {
        String serialNumber = null;
        try {
            serialNumber = Build.class.getField("SERIAL").get(null).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serialNumber;
    }

}