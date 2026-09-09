package com.example.actapriceproyect.model;

public class HechoVerificado {
    public String incumplimientoNombre;
    public String hechoRedactado = "";
    public String fecha;
    public String usuario;

    public HechoVerificado(String incumplimientoNombre, String fecha, String usuario) {
        this.incumplimientoNombre = incumplimientoNombre;
        this.fecha = fecha;
        this.usuario = usuario;
    }
}
