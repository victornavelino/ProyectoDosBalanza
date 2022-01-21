package Recursos.sincro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Servidor de udp que se pone a la escucha de DatagramPacket que contengan
 * dentro DatoUdp y los escribe en pantalla.
 *
 * @author Fz
 */
public class Principal {
    private static String nombrePuertoBalanza;
    private static int puertoDelServidor;
    private static DateFormat hourdateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Clase principal del programa
     *
     * @param args
     */
    public static void main(String[] args) {
        try {

            //Obtengo el dato
            String puerto = null, nombre = null;
            if (args.length > 0) { //si hay parámetro
                puerto = args[0];
            }
            if (args.length == 2) {
                nombre = args[1];
            }
            obtenerConfiguracionPuerto(puerto);
            obtenerConfiguracionNombrePuertoBalanza(nombre);
            Principal sf = new Principal();
            sf.escucha(puertoDelServidor);

            //Cambio la salida a un log
            //redirigirSalida();
            // Informo que arrancó
            System.out.print("[" + hourdateFormat.format(new Date()) + "] ");
            System.out.println("Arrancando servidor en: " + puertoDelServidor);
        } catch (Exception e) {
            System.out.print("[" + hourdateFormat.format(new Date()) + "] ");
            System.out.println("ERROR: " + e);
        }
    }

    /**
     * Se escucha el puerto indicado en espera de clientes a los que enviar el
     * fichero.
     *
     * @param puerto El puerto de escucha
     */
    public void escucha(int puerto) {
        try {
            // Se abre el socket servidor
            ServerSocket socketServidor = new ServerSocket(puerto);

            while (true) {
                Socket cliente = socketServidor.accept();
                cliente.setSoLinger(true, 10);

                // Se lee el mensaje de petición de fichero del cliente.
                try {
                    InputStream InputStream = cliente.getInputStream();
                    InputStreamReader isr = new InputStreamReader(InputStream);
                    BufferedReader in = new BufferedReader(isr);
                    String s = in.readLine();
                    //Object mensaje = InputStream.readObject();
                    SocketAddress remoteSocketAddress = cliente.getRemoteSocketAddress();
                    int port = cliente.getPort();
                    Runnable procesamiento = new Procesamiento(s, remoteSocketAddress, port, cliente, nombrePuertoBalanza);
                    Thread t = new Thread(procesamiento);
                    t.start();
                } catch (Exception e) {
                    System.out.print("[" + hourdateFormat.format(new Date()) + "] ");
                    System.out.println("ERROR: " + e);
                }

            }    // Se espera un cliente

        } catch (Exception e) {
            System.out.print("[" + hourdateFormat.format(new Date()) + "] ");
            System.out.println("ERROR: " + e);
        }
    }

    /**
     * Permite enviar la salida a un archivo log
     *
     * @author franco
     */
    private static void redirigirSalida() throws FileNotFoundException {
        File file = new File("soquet.log");
        PrintStream printStream = new PrintStream(new FileOutputStream(file));
        System.setOut(printStream);
    }

    /**
     * Obtiene por consola el valor del del puerto
     *
     * @author franco
     */
    private static void obtenerConfiguracionPuerto(String arg) {
        try {
            System.out.print("[" + hourdateFormat.format(new Date()) + "] ");
            System.out.println("Bienvenido al socket de LA TRADICION");
            System.out.println();
            if (isInteger(arg)) {
                puertoDelServidor = Integer.parseInt(arg);
            } else {
                puertoDelServidor = 4700;
            }
            System.out.print("[" + hourdateFormat.format(new Date()) + "] ");
            System.out.println("Arrancando el servidor en puerto " + puertoDelServidor);
            System.out.println();
        } catch (java.lang.NumberFormatException ex) {
            System.out.print("[" + hourdateFormat.format(new Date()) + "] ");
            System.out.println("Ingreso un puerto no valido. Bye");
            System.exit(1);
        }
    }

    /**
     * Permite leer una linea desde la consola el puerto de la balanza
     *
     * @author franco
     */
    private static void obtenerConfiguracionNombrePuertoBalanza(String arg) {
        try {
            if (arg != null && !arg.isEmpty()) {
                nombrePuertoBalanza = arg;
            } else {
                nombrePuertoBalanza = "COM1";
            }
            System.out.print("[" + hourdateFormat.format(new Date()) + "] ");
            System.out.println("El puerto de la balanza esta configurado en:  " + nombrePuertoBalanza);
            System.out.println();
        } catch (java.lang.NumberFormatException ex) {
            System.out.print("[" + hourdateFormat.format(new Date()) + "] ");
            System.out.println("Ingreso un puerto no valido");
            System.exit(1);
        }
    }

    /**
     * Permite leer una linea desde la consola
     *
     * @return devuelve el valor leido desde la consola
     * @author franco
     */
    public static String leerLinea() {
        String s = "";
        try {
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(converter);
            s = in.readLine();
        } catch (Exception e) {
            System.out.print("[" + hourdateFormat.format(new Date()) + "]: ");
            System.out.println("Error! Exception: " + e);
        }
        return s;
    }

    /**
     * Permite evaluar si un String es entero
     *
     * @return devuelve verdadero si se trata de un entero o falso en caso
     * contrario
     * @author franco
     */
    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
}
