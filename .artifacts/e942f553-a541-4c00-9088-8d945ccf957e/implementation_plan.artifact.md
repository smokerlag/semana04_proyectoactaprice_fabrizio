# Plan de Implementación: Ajuste de Diseño para Notch y Barra de Estado

El objetivo es evitar que el contenido de las pantallas se superponga con el notch (muesca) o la barra de estado del dispositivo. Para lograrlo de manera limpia y automática, se aplicará el atributo `android:fitsSystemWindows="true"` en el elemento raíz de todos los archivos de diseño (layouts) de las actividades.

## Cambios Propuestos

Se modificará el elemento raíz (ScrollView, RelativeLayout, LinearLayout, etc.) de cada uno de los siguientes archivos para incluir `android:fitsSystemWindows="true"`.

### UI Layouts

#### [MODIFY] [activity_add_establecimiento.xml](file:///C:/Users/jfabr/Documents/semana04_proyectoactaprice_fabrizio/app/src/main/res/layout/activity_add_establecimiento.xml)
#### [MODIFY] [activity_establecimiento_list.xml](file:///C:/Users/jfabr/Documents/semana04_proyectoactaprice_fabrizio/app/src/main/res/layout/activity_establecimiento_list.xml)
#### [MODIFY] [activity_fiscalizacion_firmas.xml](file:///C:/Users/jfabr/Documents/semana04_proyectoactaprice_fabrizio/app/src/main/res/layout/activity_fiscalizacion_firmas.xml)
#### [MODIFY] [activity_fiscalizacion_general.xml](file:///C:/Users/jfabr/Documents/semana04_proyectoactaprice_fabrizio/app/src/main/res/layout/activity_fiscalizacion_general.xml)
#### [MODIFY] [activity_fiscalizacion_hechos.xml](file:///C:/Users/jfabr/Documents/semana04_proyectoactaprice_fabrizio/app/src/main/res/layout/activity_fiscalizacion_hechos.xml)
#### [MODIFY] [activity_fiscalizacion_incumplimientos.xml](file:///C:/Users/jfabr/Documents/semana04_proyectoactaprice_fabrizio/app/src/main/res/layout/activity_fiscalizacion_incumplimientos.xml)
#### [MODIFY] [activity_fiscalizacion_precios.xml](file:///C:/Users/jfabr/Documents/semana04_proyectoactaprice_fabrizio/app/src/main/res/layout/activity_fiscalizacion_precios.xml)
#### [MODIFY] [activity_fiscalizacion_preview.xml](file:///C:/Users/jfabr/Documents/semana04_proyectoactaprice_fabrizio/app/src/main/res/layout/activity_fiscalizacion_preview.xml)
#### [MODIFY] [activity_fiscalizacion_verificacion.xml](file:///C:/Users/jfabr/Documents/semana04_proyectoactaprice_fabrizio/app/src/main/res/layout/activity_fiscalizacion_verificacion.xml)
#### [MODIFY] [activity_login.xml](file:///C:/Users/jfabr/Documents/semana04_proyectoactaprice_fabrizio/app/src/main/res/layout/activity_login.xml)
#### [MODIFY] [activity_main.xml](file:///C:/Users/jfabr/Documents/semana04_proyectoactaprice_fabrizio/app/src/main/res/layout/activity_main.xml)

## Plan de Verificación

### Verificación Manual
1. Abrir la aplicación en un emulador o dispositivo físico con notch.
2. Verificar que la pantalla de Login tenga un margen superior adecuado respecto al notch.
3. Navegar por las pantallas de Establecimientos y Fiscalización para confirmar que el buscador, los botones superiores y los títulos no estén tapados por la barra de estado.
