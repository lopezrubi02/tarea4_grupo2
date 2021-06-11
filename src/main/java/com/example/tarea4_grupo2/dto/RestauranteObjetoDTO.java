package com.example.tarea4_grupo2.dto;

import java.sql.Blob;

public interface RestauranteObjetoDTO {

    int getIdrestaurante();
    String getDireccion();
    String getRuc();
    String getNombre();
    float getCalificacionpromedio();
    int getIdadminrest();
    byte[] getFoto();
    String getFotonombre();
    String getFotocontenttype();
    int getIddistrito();

}
