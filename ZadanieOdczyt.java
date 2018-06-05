package projekt_java;

import java.util.TimerTask;

public class ZadanieOdczyt extends TimerTask {
        Projekt_JAVA pj;
        String[] odczytane;

        ZadanieOdczyt(Projekt_JAVA pj){
            this.pj = pj;
        }

        // odczytywanie wierszy zawierajace dane z pliku
        // 1 wiersz zawiera informacje na temat pozycji robota i krazka
        @Override
        public void run() {

            try {
                odczytane = pj.odczyt.nextLine().split(" ");

                pj.ustawienie1 = Integer.parseInt(odczytane[0]);
                pj.ustawienie2 = Integer.parseInt(odczytane[1]);
                pj.ustawienie3 = Float.parseFloat(odczytane[2]);
                pj.ustawienieKrazekPion = Float.parseFloat(odczytane[3]);
                pj.czyTrzyma = Boolean.parseBoolean(odczytane[4]);
                pj.czySpada = Boolean.parseBoolean(odczytane[5]);
                pj.ustawienie1Spadanie = Integer.parseInt(odczytane[6]);
                pj.ustawienie2Spadanie = Integer.parseInt(odczytane[7]);

                pj.przestawRamie1();
                pj.przestawRamie2();
                pj.przestawChwytak();

                if (!pj.odczyt.hasNext()) {
                    pj.odczyt.close();
                    pj.czas.cancel();
                    pj.trwaOdczyt = false;
                    pj.odtwarzanie.setText("Odtwórz nagranie");
                    pj.nagrywanie.setEnabled(true);
                    pj.sposobSterowania.setEnabled(true);
                    pj.przyciskiDostepnosc(true);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }