package com.example.tarea4_grupo2.dto;

import java.time.LocalDateTime;
import java.util.Date;

public interface PedidosclienteaexcelDTO {

    int getMontototal();
    String getNombre();
    Date getFechahorapedido();
    String getDireccion();
    String getMetodo();

}
