package projekt_java;

//Opis instalacji biblioteki:
//https://playground.arduino.cc/Interfacing/Java

import java.io.BufferedReader;
import java.io.InputStreamReader;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.Enumeration;

public class KomunikacjaArduino implements SerialPortEventListener {

    private SerialPort portKomunikacji;
    private BufferedReader odczyt;

    private final int TIME_OUT = 2000;          //czas oczekiwania na port
    private int baudrate;                       //szybkosc komunikacji miedzy Arduino a komputerem
    private String portName;                    //nazwa portu do komunikacji
    
    private int odczytane = 0;            //przechowuje dane odczytane z Arduino

    KomunikacjaArduino(String portName, int baudrate) {

        this.portName = portName;
        this.baudrate = baudrate;
    }

    public void inicjalizacja() {

        CommPortIdentifier port = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();     //pobranie istniejacych portow

        while (portEnum.hasMoreElements()) {    //dopoki istnieja niesprawdzone porty

            CommPortIdentifier aktualnyPort = (CommPortIdentifier) portEnum.nextElement();

            //jezeli aktualnie przegladany port to ten oczekiwany przez nas
            if (aktualnyPort.getName().equals(portName)) {
                port = aktualnyPort;
                break;
            }
        }

        if (port == null) {
            System.err.println("Nie można połączyć się z " + portName);
            return;
        }

        try {
            //ustawienie szczegolow komunikacji z portem
            portKomunikacji = (SerialPort) port.open(this.getClass().getName(), TIME_OUT);

            portKomunikacji.setSerialPortParams(baudrate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            odczyt = new BufferedReader(new InputStreamReader(portKomunikacji.getInputStream()));

            portKomunikacji.addEventListener(this);
            portKomunikacji.notifyOnDataAvailable(true);

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    @Override
    public void serialEvent(SerialPortEvent zdarzenie) {

        if (zdarzenie.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

            try {

                odczytane = odczyt.read();
                System.out.println((char)odczytane);

            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
    }
    
    public int odczytaj() {
        return odczytane;
    }
    
    public void resetujOdczytane() {
        odczytane = 0;
    }
}
