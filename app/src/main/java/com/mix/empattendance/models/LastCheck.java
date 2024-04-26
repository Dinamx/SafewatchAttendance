
package com.mix.empattendance.models;

import com.mix.empattendance.constants.Constants;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LastCheck {
   String lastCheck;
   String lastBreak;
   String lastLunch;

   public LastCheck(String lastCheck, String lastBreak, String lastLunch) {
      this.lastCheck = lastCheck;
      this.lastBreak = lastBreak;
      this.lastLunch = lastLunch;
   }

    public LastCheck() {

    }

   // Getters and setters
   public String getLastCheck() {
      return lastCheck;
   }

   public void setLastCheck(String lastCheck) {
      this.lastCheck = lastCheck;
   }

   public String getLastBreak() {
      return lastBreak;
   }

   public void setLastBreak(String lastBreak) {
      this.lastBreak = lastBreak;
   }

   public String getLastLunch() {
      return lastLunch;
   }

   public void setLastLunch(String lastLunch) {
      this.lastLunch = lastLunch;
   }



   public static void getLastMouvement(String idEmploye, final Callback callback) {
      ExecutorService executorService = Executors.newSingleThreadExecutor();
      Callable<LastCheck> callable = new Callable<LastCheck>() {
         @Override
         public LastCheck call() throws Exception {
            URL url = new URL(Constants.url + "/mouvement/" + idEmploye); // Remplacez par l'URL réelle de votre serveur
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            // Lecture de la réponse
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
               content.append(inputLine);
            }
            in.close();

            System.out.println("CALLING IT BABY");

            // Analyser la réponse JSON et créer un objet LastCheck
            // Pour cet exemple, nous supposons que la réponse est très simple
            String responseBody = content.toString();
            System.out.println(responseBody);
            String lastCheck = responseBody.substring(responseBody.indexOf("latestentrance") + 16, responseBody.indexOf(",", responseBody.indexOf("latestentrance")));
            String lastBreak = responseBody.substring(responseBody.indexOf("latestBreak") + 14, responseBody.indexOf(",", responseBody.indexOf("latestBreak")));
            String lastLunch = responseBody.substring(responseBody.indexOf("latestLunch") + 14, responseBody.indexOf(",", responseBody.indexOf("latestLunch")));

            return new LastCheck(lastCheck, lastBreak, lastLunch);
         }
      };

      executorService.submit(new Runnable() {
         @Override
         public void run() {
            try {
               LastCheck lastCheck = callable.call();
               callback.onSuccess(lastCheck);
            } catch (Exception e) {
               callback.onError(e);
            }
         }
      });
   }

   public interface Callback {
      void onSuccess(LastCheck lastCheck);
      void onError(Exception e);
   }






}