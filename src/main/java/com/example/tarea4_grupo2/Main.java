package com.example.tarea4_grupo2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Main {

    public static boolean validarDNI(String dni){
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();
        try{

            // reemplazar DNI
            String urlString = "https://api.ateneaperu.com/api/reniec/dni?sNroDocumento=88";
            String urlString2 = "https://jsonplaceholder.typicode.com/todos/1";

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            System.out.println( connection.getHeaderField("Location"));

            int status = connection.getResponseCode();

            if(status > 299){
                System.out.println("EROR PAPU");
//                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
//                while ((line = reader.readLine()) != null){
//                    responseContent.append(line);
//                }
//                System.out.println(connection.getResponseMessage());
//                System.out.println(connection.getResponseCode());
//                System.out.println(connection.getErrorStream());
//                reader.close();
            } else {
                System.out.println("/GET");
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
            }
            System.out.println(responseContent.toString());

        }catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static void main(String[] args){
        validarDNI("71327393");
        System.out.println("de");
    }
}
