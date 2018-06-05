package projekt_java;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import javax.swing.JButton;

public class Przyciski implements ActionListener {
        Projekt_JAVA pj;
        
        Przyciski(Projekt_JAVA pj){
            this.pj = pj;
        }
        
        //obsluga przyciskow odpowiedzialnych za poruszanie robotem
        //przyciski nagrywanie, odtwarzanie odpowiadaja za nauke robota
        //nagrywanie - zapisuje co 50ms pozycje robota do pliku zewn.
        //odtwarzanie - odczytuje co 50ms pozycje robota z pliku zewn.
        @Override
        public void actionPerformed(ActionEvent arg) {
            JButton klik = (JButton) arg.getSource();

            if (klik == pj.nagrywanie && !pj.trwaOdczyt)
                if (!pj.trwaZapis) {
                    pj.trwaZapis = true;
                    pj.nagrywanie.setText("Zakończ nagrywanie");
                    pj.odtwarzanie.setEnabled(false);
                    pj.nagrywanie();
                } else {
                    pj.trwaZapis = false;
                    pj.czas.cancel();
                    pj.zapis.close();
                    pj.nagrywanie.setText("Rozpocznij nagrywanie");
                    pj.odtwarzanie.setEnabled(true);
                }
            else if (klik == pj.odtwarzanie && !pj.trwaZapis)
                if (!pj.trwaOdczyt) {
                    pj.trwaOdczyt = true;
                    pj.odtwarzanie.setText("Zakończ odtwarzanie");
                    pj.nagrywanie.setEnabled(false);
                    pj.sposobSterowania.setEnabled(false);
                    pj.przyciskiDostepnosc(false);
                    pj.odtwarzanie();
                } else {
                    pj.trwaOdczyt = false;
                    pj.czas.cancel();
                    pj.odczyt.close();
                    pj.odtwarzanie.setText("Odtwórz nagranie");
                    pj.nagrywanie.setEnabled(true);
                    pj.sposobSterowania.setEnabled(true);
                    pj.przyciskiDostepnosc(true);
                }
            else if (klik == pj.przycRam1L)
                pj.przestawRamie1Lewo();
            else if (klik == pj.przycRam1P)
                pj.przestawRamie1Prawo();
            else if (klik == pj.przycRam2L)
                pj.przestawRamie2Lewo();
            else if (klik == pj.przycRam2P)
                pj.przestawRamie2Prawo();
            else if (klik == pj.przycChwytD)
                pj.przestawChwytakDol();
            else if (klik == pj.przycChwytG)
                pj.przestawChwytakGora();
            else if (klik == pj.sposobSterowania)
                if (!pj.sterowanieArduino) {
                    pj.sterowanieArduino = true;
                    pj.przyciskiDostepnosc(false);

                    pj.czas = new Timer();
                    pj.czas.scheduleAtFixedRate(new ZadanieArduino(), 0, 50);
                    pj.sposobSterowania.setText("Zmień sterowanie na: klawiatuta/GUI");
                } else {
                    pj.sterowanieArduino = false;
                    pj.czas.cancel();
                    pj.przyciskiDostepnosc(true);
                    pj.sposobSterowania.setText("Zmień sterowanie na: Arduino");
                }
            else if (klik == pj.zakonczTrzymanie)
                pj.wypuszczanie();
        }
    }