package projekt_java;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Klawisze implements KeyListener {
        Projekt_JAVA pj;
        
        Klawisze(Projekt_JAVA pj){
            this.pj = pj;
        }
        
        //program reaguje tylko na wcisniecie klawiszy
        //A, D - przesuwanie 1 ramieniem
        //strzalka w lewo i prawo - przesuwanie 2 ramieniem
        //strzalka w gore i dol - przesuwanie chwytakiem
        //spacja - opuszczenie obiektu
        @Override
        public void keyPressed(KeyEvent arg) {

            if (arg.getKeyCode() == KeyEvent.VK_A)
                pj.przestawRamie1Lewo();
            else if (arg.getKeyCode() == KeyEvent.VK_D)
                pj.przestawRamie1Prawo();
            else if (arg.getKeyCode() == KeyEvent.VK_LEFT)
                pj.przestawRamie2Lewo();
            else if (arg.getKeyCode() == KeyEvent.VK_RIGHT)
                pj.przestawRamie2Prawo();
            else if (arg.getKeyCode() == KeyEvent.VK_DOWN)
                pj.przestawChwytakDol();
            else if (arg.getKeyCode() == KeyEvent.VK_UP)
                pj.przestawChwytakGora();
            else if (arg.getKeyCode() == KeyEvent.VK_SPACE)
                pj.wypuszczanie();
        }

        @Override
        public void keyReleased(KeyEvent arg) {
        }

        @Override
        public void keyTyped(KeyEvent arg) {
        }
    }
