package projekt_java;

import java.util.TimerTask;

public class ZadanieArduino extends TimerTask {
        Projekt_JAVA pj;
        int odczytane;

        ZadanieArduino(Projekt_JAVA pj){
            this.pj = pj;
        }

    ZadanieArduino() {
    }
        
        @Override
        public void run() {

            try {

                odczytane = pj.arduino.odczytaj();

                if (odczytane != 0) {
                    if (odczytane == '1')
                        pj.przestawRamie1Lewo();
                    else if (odczytane == '2')
                        pj.przestawRamie1Prawo();
                    else if (odczytane == '3')
                        pj.przestawRamie2Lewo();
                    else if (odczytane == '4')
                        pj.przestawRamie2Prawo();
                    else if (odczytane == '5')
                        pj.przestawChwytakGora();
                    else if (odczytane == '6')
                        pj.przestawChwytakDol();
                    else if (odczytane == '7')
                        pj.wypuszczanie();

                    pj.arduino.resetujOdczytane();
                }

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }