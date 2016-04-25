# mediplus
Manual del desarrollador
Debido a que nuestra aplicacion trabaja con la API de Google para la gestion de los calendarios, antes de empezar a programar, es necesario realizar una serie de acciones que nos identifiquen ante Google como desarrollador, y nos permita hacer uso de dicha API.

Lo primero a realizar será crear un certificado SHA1 que Google utilizará para identificar el ordenador desde el cual el desarrollador esta programando la aplicacion. Para ello, bastará con introducir los siguientes comandos:

LINUX
keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore -list -v

WINDOWS
cd "C:\Program Files\Java\jdk1.8.0_45\bin" -> RUTA al jdk de JAVA
keytool -exportcert -alias androiddebugkey -keystore C:\Users\USUARIO\.android\debug.keystore -list -v

Una vez introducidos dichos comandos, se pedirá introducir una contraseña, que para ambos sistemas operativos sera android. Le damos a ENTER y nos tendra que aparecer una ventana similar a la mostrada a continuacion:



La clave SHA1 será necesaria en pasos posteriores, por lo que será conveniente apuntarla. Aun así, ahora que ya se ha creado un certificado para trabajar con la aplicación, procederemos a darnos de alta en la plataforma de desarrollo de aplicaciones integradas con Google. Antes de comenzar con la explicación de los pasos a seguir para darnos de alta, es necesario remarcar que cada ordenador podrá estar asociado a una única cuenta de desarrollador Google, que como es de esperar, tiene que pertenecer a Google, y ademas, dicha asociacion será bilateral. Es decir, el ordenador estará asociado a la cuenta Google, y la cuenta al ordenador, por lo que no se podrá reutilizar para programar la misma aplicacion en otro ordenador.

Dicho esto, entraremos en el siguiente enlace:
https://console.developers.google.com/flows/enableapi?apiid=calendar

Le damos a continuar, esperamos a que se cargue la siguiente pagina, y cuando acabe, le damos a ir a credenciales para que se nos habra la siguiente ventana:


En dicha ventana, se deberán las mismas opciones que se muestran en la imagen superior. Despues de introducir todos los datos, se le da al boton ¿Que credenciales necesito? Para que se habra la siguiente ventana:


En esta ventana, deberemos introducir un nombre con el que identificar la aplicacion dentro de la propia cuenta de Google, la clave SHA1 creada en pasos previos, y es.mediplus.mediplus como nombre del paquete. El primer campo puede contener cualquier dato, pero es necesario asegurarse de que los otros dos campos se introducen correctamente, de lo contrario Google no permitirá hacer uso de su API. Despues de introducir todos los datos, le datos a continuar, para pasar a la ultima ventana.

En esta ventana, seleccionamos el correo de Google a utilizar, y ponemos como nombre de producto Mediplus. Le damos a continuar, y posteriormente a listo. Ya tendremos la API de Google habilitada para poder programar la aplicacion. Ahora bastará con importar el proyecto en android studio, y comenzar a programar.
