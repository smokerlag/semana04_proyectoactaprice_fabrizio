package com.example.actapriceproyect.model;

public class ProductoPrecio {
    public String nombre;
    public String precioPrice = "";
    public String precioPublicado = "";
    public String precioSurtidor = "";
    public String precioDescuento = "";
    /** Solo para columna "Otros": nombre comercial / marca que va en la columna Marca del acta. */
    public String marca = "";
    public boolean requiereMarca;

    public ProductoPrecio(String nombre) {
        this(nombre, false);
    }

    public ProductoPrecio(String nombre, boolean requiereMarca) {
        this.nombre = nombre;
        this.requiereMarca = requiereMarca;
    }
}
