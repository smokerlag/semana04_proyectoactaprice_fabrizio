package com.example.actapriceproyect.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fiscalizaciones")
public class Fiscalizacion {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int establecimientoId;
    
    // Módulo 04: Datos Generales
    public String expediente;
    public String agenteFiscalizado;
    public String codigoOsinergmin;
    public String registroHidrocarburos;
    public String fechaDiligencia;
    public String horaApertura;
    public String horaCierre;
    public String direccion;
    public String distrito;
    public String provincia;
    public String departamento;
    public String rucDni;
    public String telefonoFax;
    public String fiscalizadorResponsable;
    
    // Módulo 05: Información Recabada
    public String productosJson; 
    
    // Módulo 06: Verificación
    public String telefonoPublicado;
    public String telefonoActualizado;
    public String horarioPublicado;
    public String listaPreciosExhibida; // q4
    public String unidadGalonEmpleada;  // q5
    public String etiquetaVisible;      // q6
    
    // Módulos 07-08: Incumplimientos y Hechos
    public String incumplimientosJson;
    public String hechosVerificados;
    
    // Módulo 09: Otros y Firmas
    public String documentacion;
    public String ocurrencias;
    public String observaciones;
    public boolean negativaFirma;
    public String firmaInspectorPath;
    public String firmaResponsablePath;

    public boolean sincronizado;
    
    // Funciones Avanzadas
    public String estado; // BORRADOR, EN PROCESO, FINALIZADA, ACTA GENERADA
    public double latitud;
    public double longitud;
    public String fotosJson; // Lista de rutas de fotos en formato JSON
    public String historialCambios; // Texto descriptivo o JSON con log de cambios

    public Fiscalizacion() {
        this.estado = "BORRADOR";
        this.sincronizado = false;
    }
}
