package projekt_java;

import java.util.TimerTask;

    public class ZadanieZapis extends TimerTask {
        Projekt_JAVA pj;
        String doZapisu;

        ZadanieZapis(Projekt_JAVA pj){
            this.pj = pj;
        }
        
        // zapisywanie danych robota w postaci wierszy do zewn. pliku
        // 1 wiersz zawiera informacje na temat pozycji robota i krazka
        @Override
        public void run() {
            doZapisu = Integer.toString(pj.ustawienie1) + ' '
                    + Integer.toString(pj.ustawienie2) + ' '
                    + Float.toString(pj.ustawienie3) + ' '
                    + Float.toString(pj.ustawienieKrazekPion) + ' '
                    + Boolean.toString(pj.czyTrzyma) + ' '
                    + Boolean.toString(pj.czySpada) + ' '
                    + Integer.toString(pj.ustawienie1Spadanie) + ' '
                    + Integer.toString(pj.ustawienie2Spadanie);

            try {
                pj.zapis.println(doZapisu);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }