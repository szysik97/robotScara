package projekt_java;

import com.sun.j3d.utils.geometry.Cylinder;
import java.util.Enumeration;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnCollisionEntry;
import javax.media.j3d.WakeupOnCollisionExit;

//klasa odpowiadajaca za kolizje
public class Kolizja extends Behavior{
    //ksztalt to chwytak
    Projekt_JAVA pj;
    private Cylinder ksztalt;
    private WakeupOnCollisionEntry zdarzenieKolizja;
    private WakeupOnCollisionExit zdarzenieKoniecKolizji;

    //przypisanie wartosci dla obiektu rozpatrywanego pod wzgledem kolizji
    Kolizja(Cylinder ksztalt, BoundingSphere wiezy, Projekt_JAVA pj) {
        this.pj = pj;
        this.ksztalt = ksztalt;
        this.ksztalt.setCollisionBounds(wiezy);
        setSchedulingBounds(wiezy);
    }

    //obiekt 'pobudza sie' pod wplywem WEJSCIA w inny obiekt
    //obiekt 'wychodzi' ze stanu kolizji jesli opusci obszar innego obiektu
    @Override
    public void initialize() {
        zdarzenieKolizja = new WakeupOnCollisionEntry(ksztalt);
        zdarzenieKoniecKolizji = new WakeupOnCollisionExit(ksztalt);
        wakeupOn(zdarzenieKolizja);
    }

    @Override
    public void processStimulus(Enumeration enmrtn) {

        WakeupCriterion kryterium = (WakeupCriterion) enmrtn.nextElement();
        if (kryterium instanceof WakeupOnCollisionEntry) {
            wakeupOn(zdarzenieKoniecKolizji);
            if (!pj.trwaOdczyt) {
                pj.czyTrzyma = true;
                pj.zakonczTrzymanie.setEnabled(true);
            }
        } else if (kryterium instanceof WakeupOnCollisionExit)
            wakeupOn(zdarzenieKolizja);

    }
}
