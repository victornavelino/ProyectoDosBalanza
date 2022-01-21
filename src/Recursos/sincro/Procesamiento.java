package Recursos.sincro;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import giovynet.serial.Com;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase se encarga de realizar el procesamiento de todo los datos que
 * recibe el servidor
 * 
* @author franco
 */
public class Procesamiento implements Runnable, SerialPortEventListener {

    private final String nombrePuertoBalanza;

    private byte[] key = null;
    private final DateFormat hourdateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private String instancia;
//    private final DatagramSocket socket;
    private final Object mensaje;
    private final SocketAddress address;
    private final int puertoCliente;
    private final Socket cliente;
    private Com com1;
    // variables nuevas 
    private CommPortIdentifier IdPuerto;
    private Enumeration portList;
    private InputStream inputStream;
    private SerialPort serialPort;
    //variables nuevas
    static CommPortIdentifier portId;
    static CommPortIdentifier puertoEncontrado;
    boolean portFound = false;
    String defaultPort;
    public static String peso;

    public Procesamiento(Object data,
            SocketAddress address,
            int puertoCliente, Socket cliente, String nombrePuertoBalanza) throws UnknownHostException, SocketException {
        this.instancia = "[P" + this.hashCode() + "] ";
        this.cliente = cliente;
        this.mensaje = data;
        this.address = address;
        this.puertoCliente = puertoCliente;
        this.nombrePuertoBalanza = nombrePuertoBalanza;
        inicializar();
    }

    private void inicializar() {
        if (!portFound) {
            buscarPuerto();
            if (puertoEncontrado != null) {
                abrirPuerto();
            }
        } else {
            System.out.println("No se pudo abrir el puerto");
        }
        if (serialPort != null) {
            leerPuerto();
        }
    }

    @Override
    public void run() {
        try {
            long tiempoInicio = System.currentTimeMillis();//Cargo el tiempo de inicio del proceso
            // Llega un cliente.
            System.out.print("[" + hourdateFormat.format(new Date()) + "] " + instancia);
            System.out.println("Aceptado cliente");
            // Si el mensaje es de petición de fichero
            if (mensaje != null) {
                if (mensaje instanceof String) {
                    // Se muestra en pantalla el pedido y se envia
                    System.out.print("[" + hourdateFormat.format(new Date()) + "] " + instancia);

                    System.out.println("Recibido del servidor: "
                            + mensaje);

                    enviarBalanza();

                } else {
                    // Si no es el mensaje esperado, se avisa y se sale todo.
                    System.out.print("[" + hourdateFormat.format(new Date()) + "] " + instancia);

                    System.err.println(
                            "Mensaje no esperado " + mensaje.getClass().getName());
                }
            }
            // Cierre de sockets 
            cliente.close();
            long totalTiempo = System.currentTimeMillis() - tiempoInicio;//Cargo el tiempo de fin del proceso
            System.out.print("[" + hourdateFormat.format(new Date()) + "] " + instancia);
            System.out.println("Tiempo total de procesamiento: " + totalTiempo + " miliseg");//Muestro por pantalla
        } catch (Exception ex) {
            System.out.println("Error procesando: " + ex);
        }
    }

    private void enviarBalanza() throws UnknownHostException, IOException {


        String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + peso;
        cliente.getOutputStream()
                .write(httpResponse.getBytes("UTF-8"));

        cliente.close();

    }

    private void buscarNombrePuertoSegunSistema() {

        //Asignamos el nombre del puerto desde la tabla de configuraciones
        defaultPort = nombrePuertoBalanza;
        //fin

        //verificamos sistema operativo para comprobar
        //el nombre del puerto com
//        if (System.getProperty("os.name").contentEquals("Linux")) {
//            //nombre del puerto en linux
//            defaultPort = "/dev/ttyS0";
//        } else {
//            //nombre del puerto en windows
//            defaultPort = "COM3";
//        }
    }

    private void buscarPuerto() {
//        System.out.println("port lis" + portList);
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(defaultPort)) {
                    portFound = true;
                    puertoEncontrado = portId;

                }
            }
        }

    }

    private void abrirPuerto() {
        try {
            serialPort = (SerialPort) puertoEncontrado.open("SimpleReadApp", 2000);
        } catch (PortInUseException ex) {
            System.out.println("Error abriendo puerto: " + ex);
        }

    }

    private void cerrarPuertoBalanza() {
        try {
            serialPort.removeEventListener();
            serialPort.close();
        } catch (Exception e) {

        }

    }

    private void leerPuerto() {

        try {
            inputStream = serialPort.getInputStream();

        } catch (IOException e) {
        }

        try {
            serialPort.addEventListener(this);
        } catch (TooManyListenersException e) {
        }

        serialPort.notifyOnDataAvailable(true);
        try {
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_2,
                    SerialPort.PARITY_NONE);
            //agregados
            serialPort.setRTS(true);
            serialPort.setOutputBufferSize(200);
        } catch (UnsupportedCommOperationException e) {
        }
    }

    public void serialEvent(SerialPortEvent event) {

        switch (event.getEventType()) {

            case SerialPortEvent.BI:

            case SerialPortEvent.OE:

            case SerialPortEvent.FE:

            case SerialPortEvent.PE:

            case SerialPortEvent.CD:

            case SerialPortEvent.CTS:

            case SerialPortEvent.DSR:

            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                byte[] readBuffer = new byte[200];

                try {
                    while (inputStream.available() > 0) {

                        inputStream.read(readBuffer);
                        //System.out.println("inputStreeam: "+readBuffer);

                    }
                    peso = new String(readBuffer).trim().substring(0, 6);

                } catch (IOException e) {
                }

                break;
        }
    }

}
