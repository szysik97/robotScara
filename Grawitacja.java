package projekt_java;

import java.util.TimerTask;

    public class Grawitacja extends TimerTask {
        Projekt_JAVA pj;
        
        Grawitacja(Projekt_JAVA pj){
            this.pj = pj;
        }
        
        // pseudo grawitacja, jesli krazek zostal puszczony jego pozycja zmniejsza
        // sie co 80 ms az do dotkniecia podlogi
        @Override
        public void run() {
            if (pj.ustawienieKrazekPion > 0.09f) {
                pj.ustawienieKrazekPion -= 0.1f;
                pj.przestawKrazek(pj.ustawienie1Spadanie, pj.ustawienie2Spadanie);
            } else {
                pj.czySpada = false;
                this.cancel();
            }
        }
    }