# Guía de evidencias (capturas, video y PDF)

Coloca aquí los archivos que **tú** generas al probar la app. Esta carpeta ya tiene la estructura lista.

## 1. Capturas de pantalla

Guarda en `entrega/evidencias/capturas/` con estos nombres sugeridos:

| Archivo sugerido | Qué mostrar |
|------------------|-------------|
| `01_login.png` | Pantalla de login |
| `02_lista_establecimientos.png` | Lista / búsqueda de establecimientos |
| `03_datos_generales.png` | Fiscalización – datos generales |
| `04_precios.png` | Precios (Otros + Marca visibles) |
| `05_verificacion.png` | Checklist Sí/No |
| `06_incumplimientos.png` | Incumplimientos detectados |
| `07_hechos.png` | Hechos verificados |
| `08_firmas.png` | Firmas y receptor |
| `09_preview.png` | Resumen / generar PDF |
| `10_pdf_pagina1.png` | PDF abierto – página 1 |
| `11_pdf_pagina2.png` | PDF abierto – página 2 (firmas) |
| `12_postman.png` | Postman con colección ejecutada en verde |
| `13_api_running.png` | Terminal con `npm start` de la API |

**Cómo capturar (Android Studio):**  
emulador → ícono cámara / `Ctrl+S` en Extended Controls, o `adb exec-out screencap -p > captura.png`.

## 2. Video corto (flujo completo)

Guarda en `entrega/evidencias/video/`:

- Nombre sugerido: `demo_flujo_acta_price.mp4`
- Duración sugerida: **1–3 minutos**
- Contenido del video:
  1. Login  
  2. Elegir establecimiento  
  3. Llenar precios (mostrar valores distintos en PRICE/Publicado/Surtidor)  
  4. Verificación → hechos → firmas  
  5. Generar PDF  
  6. Abrir PDF y mostrar **página 1 y página 2**  
  7. (Opcional) Postman: login + crear fiscalización  

**Cómo grabar:**  
- Emulador Android Studio → Record  
- o Xbox Game Bar (`Win+G`) / OBS

## 3. PDF final del Acta PRICE

1. Ejecuta el flujo completo en la app hasta **Generar PDF**
2. Copia el archivo generado desde el almacenamiento de la app, por ejemplo:
   - Emulador Device File Explorer →  
     `data/data/com.example.actapriceproyect/files/Documents/`  
     o almacenamiento externo de la app `Android/data/com.example.actapriceproyect/files/Documents/`
3. Pégalo en `entrega/evidencias/pdf/` como:
   - `Acta_PRICE_FINAL.pdf`
   - (opcional) también el `.docx`

También puedes usar el botón **Compartir / Descargar PDF** de la pantalla preview y guardarlo en Descargas, luego copiarlo a esta carpeta.

## 4. Checklist rápido

- [ ] 10+ capturas en `capturas/`
- [ ] Video en `video/`
- [ ] PDF del acta en `pdf/`
- [ ] Captura de Postman OK
- [ ] Captura de API corriendo
