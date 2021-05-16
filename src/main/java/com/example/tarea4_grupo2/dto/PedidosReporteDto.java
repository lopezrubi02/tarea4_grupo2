package com.example.tarea4_grupo2.dto;

import java.util.Date;

public interface PedidosReporteDto {
    int getnumeropedido();
    Date getfechahorapedido();
    String getnombre();
    String getapellidos();
    float getmontototal();
    String getnombreplato();
    String getmetodo();
    String getdistrito();
}