package projekt_java;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Timer;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.SpotLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class Projekt_JAVA extends JFrame {

    //przyciski GUI
    public JButton nagrywanie;
    public JButton odtwarzanie;
    public JButton przycRam1L;
    public JButton przycRam1P;
    public JButton przycRam2L;
    public JButton przycRam2P;
    public JButton przycChwytD;
    public JButton przycChwytG;
    public JButton sposobSterowania;
    public JButton zakonczTrzymanie;

    //obiekt do komunikacji z Arduino za pomoca SerialPortu
    public KomunikacjaArduino arduino;
    public boolean sterowanieArduino = false;

    //zmienne odpowiedzialne za nagrywanie i odtwarzanie ruchów robota
    public PrintWriter zapis;
    public Scanner odczyt;
    public Timer czas;
    public boolean trwaZapis = false;
    public boolean trwaOdczyt = false;
    private final int CZAS_TIMERA = 50;
    private final String SCIEZKA_PLIKU = "nagranie.txt";

    //zmienne typow podst. zwiazane z zachowaniem i pozycja robota
    public int ustawienie1 = 0;
    public int ustawienie2 = 0;
    public float ustawienie3 = 0.0f;
    public float ustawienieKrazekPion = 0.0f;
    public boolean czyTrzyma = false;
    public boolean czySpada = false;
    public int ustawienie1Spadanie;
    public int ustawienie2Spadanie;
    private int ustawienie1Odtwarzanie, ustawienie2Odtwarzanie;

    //zmienne odpowiedzialne na grafike 3D
    private BranchGroup scena;
    private TransformGroup transGrPodst, transGrObrot1, transGrChwyt, transGrKrazek;
    private Transform3D obrotPodstawy = new Transform3D();
    private Transform3D obrot1 = new Transform3D();
    private Transform3D przesObrot1 = new Transform3D();
    private Transform3D ruchChwytak = new Transform3D();
    private Transform3D ruchKrazek = new Transform3D();
    private Transform3D obrotKrazek = new Transform3D();
    private Transform3D obrotKrazek2a = new Transform3D();
    private Transform3D obrotKrazek2b = new Transform3D();

    //metody zwiazane z poruszaniem ramionami i chwytakiem
    public void przestawRamie1Lewo() {
        if (ustawienie1 > -180) {
            ustawienie1--;
            przestawRamie1();
        }
    }

    public void przestawRamie1Prawo() {
        if (ustawienie1 < 180) {
            ustawienie1++;
            przestawRamie1();
        }
    }

    public void przestawRamie2Lewo() {
        if (ustawienie2 > -140) {
            ustawienie2--;
            przestawRamie2();
        }
    }

    public void przestawRamie2Prawo() {
        if (ustawienie2 < 140) {
            ustawienie2++;
            przestawRamie2();
        }
    }

    public void przestawChwytakGora() {
        if (ustawienie3 < 1.6f) {
            ustawienie3 += 0.1f;
            if (czyTrzyma == true)
                ustawienieKrazekPion += 0.1f;
            przestawChwytak();
        }
    }

    public void przestawChwytakDol() {
        if (ustawienie3 > -1.7f) {
            ustawienie3 -= 0.1f;
            if (czyTrzyma == true)
                ustawienieKrazekPion -= 0.1f;
            przestawChwytak();
        }
    }

    public void przestawRamie1() {
        obrotPodstawy.rotY(Math.toRadians(ustawienie1));
        transGrPodst.setTransform(obrotPodstawy);
        przestawKrazek(ustawienie1, ustawienie2);
    }

    public void przestawRamie2() {
        obrot1.rotY(Math.toRadians(ustawienie2));
        obrot1.setTranslation(new Vector3f(0.0f, 0.0f, 4.0f));
        transGrObrot1.setTransform(obrot1);
        przesObrot1.setTranslation(new Vector3f(0.0f, 0.0f, -4.0f));
        obrot1.mul(przesObrot1);
        transGrObrot1.setTransform(obrot1);
        przestawKrazek(ustawienie1, ustawienie2);
    }

    public void przestawChwytak() {
        ruchChwytak.setTranslation(new Vector3f(0.0f, ustawienie3, 0.0f));
        transGrChwyt.setTransform(ruchChwytak);
        przestawKrazek(ustawienie1, ustawienie2);
    }

    // poruszanie krazkiem - zlozenie ruchow obu ramion i chwytaka
    public void przestawKrazek(int ustaw1, int ustaw2) {

        if (czyTrzyma || czySpada || trwaOdczyt) {

            obrotKrazek.rotY(Math.toRadians(ustaw1));
            obrotKrazek2a.rotY(Math.toRadians(ustaw2));
            ruchKrazek.setTranslation(new Vector3f(0.0f, ustawienieKrazekPion, 0.0f));
            obrotKrazek.mul(ruchKrazek);
            obrotKrazek2a.setTranslation(new Vector3f(0.0f, 0.0f, 4.0f));
            obrotKrazek2b.setTranslation(new Vector3f(0.0f, 0.0f, -4.0f));
            obrotKrazek2a.mul(obrotKrazek2b);

            obrotKrazek.mul(obrotKrazek2a);
            transGrKrazek.setTransform(obrotKrazek);
        }
    }
    
    // zapisywanie wartosci pozycji oraz stanu robota jak i krazka w zewnetrznym pliku
    public void nagrywanie() {

        ustawienie1Odtwarzanie = ustawienie1;
        ustawienie2Odtwarzanie = ustawienie2;

        try {
            zapis = new PrintWriter(SCIEZKA_PLIKU);

            czas = new Timer();
            czas.scheduleAtFixedRate(new ZadanieZapis(this), 0, CZAS_TIMERA);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    // zrzucenie krazka i poddanie go dzialaniu pseudo-grawitacji
    public void wypuszczanie() {
        czySpada = true;
        czyTrzyma = false;
        ustawienie1Spadanie = ustawienie1;
        ustawienie2Spadanie = ustawienie2;

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new Grawitacja(this), 0, 80);
        zakonczTrzymanie.setEnabled(false);
    }

    // sprawdzenie jakie przyciski sa dostepne do klikniecia
    public void przyciskiDostepnosc(boolean bln) {

        przycRam1L.setEnabled(bln);
        przycRam1P.setEnabled(bln);
        przycRam2L.setEnabled(bln);
        przycRam2P.setEnabled(bln);
        przycChwytG.setEnabled(bln);
        przycChwytD.setEnabled(bln);
        zakonczTrzymanie.setEnabled(bln);
    }

    // odtworzenie wartosci pozycji oraz stanu robota jak i krazka z wartosci z zewnetrznego pliku
    public void odtwarzanie() {

        ustawienie1 = ustawienie1Odtwarzanie;
        ustawienie2 = ustawienie2Odtwarzanie;
        przestawKrazek(ustawienie1Odtwarzanie, ustawienie2Odtwarzanie);

        try {
            odczyt = new Scanner(new File(SCIEZKA_PLIKU));

            czas = new Timer();
            czas.scheduleAtFixedRate(new ZadanieOdczyt(this), 0, CZAS_TIMERA);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    // 'kregoslup' programu, zawierajacy m.in. stworzenie okienka, komunikacje z arduino, czy dodanie przyciskow
    Projekt_JAVA() {

        //podstawowe utworzenie okna
        super("Robot typu SCARA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        //konfiguracja grafiki
        GraphicsConfiguration conf = SimpleUniverse.getPreferredConfiguration();

        //obiekt odpowiedzialny za renderowanie
        Canvas3D canvas = new Canvas3D(conf);
        canvas.setPreferredSize(new Dimension(800, 600));

        //stworzenie przyciskow
        Przyciski przyciskiListener = new Przyciski(this);
        nagrywanie = new JButton("Rozpocznij nagrywanie");
        nagrywanie.addActionListener(przyciskiListener);
        odtwarzanie = new JButton("Odtwórz nagranie");
        odtwarzanie.addActionListener(przyciskiListener);
        przycRam1L = new JButton("<<==");
        przycRam1L.addActionListener(przyciskiListener);
        przycRam1P = new JButton("==>>");
        przycRam1P.addActionListener(przyciskiListener);
        przycRam2L = new JButton("<<==");
        przycRam2L.addActionListener(przyciskiListener);
        przycRam2P = new JButton("==>>");
        przycRam2P.addActionListener(przyciskiListener);
        przycChwytD = new JButton("v|v");
        przycChwytD.addActionListener(przyciskiListener);
        przycChwytG = new JButton("^|^");
        przycChwytG.addActionListener(przyciskiListener);
        zakonczTrzymanie = new JButton("Puść obiekt");
        zakonczTrzymanie.addActionListener(przyciskiListener);
        zakonczTrzymanie.setEnabled(false);
        sposobSterowania = new JButton("Zmień sterowanie na: Arduino");
        sposobSterowania.addActionListener(przyciskiListener);

        //stworzenie paneli z odpowiednimi przyciskami
        JPanel panel1 = new JPanel(new BorderLayout());
        JPanel panel2 = new JPanel(new FlowLayout());
        JPanel panel3 = new JPanel(new FlowLayout());
        canvas.addKeyListener(new Klawisze(this));
        panel2.add(nagrywanie);
        panel2.add(odtwarzanie);
        panel2.add(sposobSterowania);
        panel3.add(new JLabel("RAMIE 1"));
        panel3.add(przycRam1L);
        panel3.add(przycRam1P);
        panel3.add(new JLabel("RAMIE 2"));
        panel3.add(przycRam2L);
        panel3.add(przycRam2P);
        panel3.add(new JLabel("CHWYTAK"));
        panel3.add(przycChwytD);
        panel3.add(przycChwytG);
        panel3.add(zakonczTrzymanie);
        panel1.add(panel2, BorderLayout.NORTH);
        panel1.add(canvas, BorderLayout.CENTER);
        panel1.add(panel3, BorderLayout.SOUTH);

        // spakowanie paneli i nadanie wlasciwosci okienku(widocznosc)
        add(panel1);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);

        //utworzenie sceny
        scena = nowaScena();
        scena.compile();

        //dodanie obserwatora
        Transform3D przesuniecie_obserwatora = new Transform3D();
        Transform3D obrot_obserwatora = new Transform3D();
        obrot_obserwatora.rotY(Math.PI / 4);
        przesuniecie_obserwatora.set(new Vector3f(0.0f, 3.0f, 25.0f));
        obrot_obserwatora.mul(przesuniecie_obserwatora);

        SimpleUniverse simpleU = new SimpleUniverse(canvas);
        simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(obrot_obserwatora);
        simpleU.addBranchGraph(scena);

        //obracanie i zoom za pomoca myszki
        OrbitBehavior orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ROTATE);
        orbit.setSchedulingBounds(new BoundingSphere());
        simpleU.getViewingPlatform().setViewPlatformBehavior(orbit);

        //rozpoczecie komunikacji z Arduino
        try {
            arduino = new KomunikacjaArduino("COM3", 9600);
            arduino.inicjalizacja();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    // utworzenie glownego wezla, czyli sceny
    public BranchGroup nowaScena() {
        BranchGroup scena = new BranchGroup();

        BoundingSphere granica = new BoundingSphere();

        //dodanie swiatla
        SpotLight swiatlo = new SpotLight(
                new Color3f(1.0f, 1.0f, 1.0f), //kolor swiatla(biale)
                new Point3f(1.0f, 1.0f, 1.0f), //pozycja reflektora/lampy
                new Point3f(0.5f, 0.5f, 0.5f), //szybkosc rozpraszania swiatla
                new Vector3f(0.0f, 0.0f, 0.0f), //wektor kierunku swiatla
                (float) Math.PI, //kat stozka swiatla
                50);                               //wsp.skupienia swiatla
        swiatlo.setInfluencingBounds(granica);
        scena.addChild(swiatlo);

        //nowy material - robot
        Appearance wyglad = new Appearance();
        wyglad.setColoringAttributes(new ColoringAttributes(0.0f, 0.2f, 0.5f,
                ColoringAttributes.NICEST));

        //nowy material - podloga
        Appearance wygladPodloga = new Appearance();
        wygladPodloga.setColoringAttributes(new ColoringAttributes(0.0f, 0.2f, 0.0f,
                ColoringAttributes.NICEST));

        //nowy material - krazek
        Appearance wygladKrazek = new Appearance();
        wygladKrazek.setColoringAttributes(new ColoringAttributes(0.8f, 0.0f, 0.1f,
                ColoringAttributes.NICEST));

        //utworzenie podstawy robota skladajacej sie z 2 cylindrow
        Cylinder podstawa1 = new Cylinder(1.2f, 0.7f, wyglad);
        Transform3D poz_podstawy1 = new Transform3D();
        poz_podstawy1.set(new Vector3f(0.0f, 0.0f, 0.0f));
        TransformGroup transPods1 = new TransformGroup(poz_podstawy1);
        transPods1.addChild(podstawa1);
        scena.addChild(transPods1);

        Cylinder podstawa2 = new Cylinder(0.45f, 5f, wyglad);
        Transform3D poz_podstawy2 = new Transform3D();
        poz_podstawy2.set(new Vector3f(0.0f, 2.8f, 0.0f));
        TransformGroup transPods2 = new TransformGroup(poz_podstawy2);
        transPods2.addChild(podstawa2);
        scena.addChild(transPods2);

        //utworzenie ramiena numer 1 skladajacego sie z
        //dwoch walcow oraz jednego prostopadloscianu
        Cylinder cy1Ram1 = new Cylinder(0.6f, 0.7f, wyglad);
        Transform3D poz_cy1Ram1 = new Transform3D();
        poz_cy1Ram1.set(new Vector3f(0.0f, 5.0f, 0.0f));
        TransformGroup trans_cy1Ram1 = new TransformGroup(poz_cy1Ram1);
        trans_cy1Ram1.addChild(cy1Ram1);

        Cylinder cy2Ram1 = new Cylinder(0.6f, 0.7f, wyglad);
        Transform3D poz_cy2Ram1 = new Transform3D();
        poz_cy2Ram1.set(new Vector3f(0.0f, 5.0f, 4.0f));
        TransformGroup trans_cy2Ram1 = new TransformGroup(poz_cy2Ram1);
        trans_cy2Ram1.addChild(cy2Ram1);

        com.sun.j3d.utils.geometry.Box box1Ram1 = new com.sun.j3d.utils.geometry.Box(0.3f, 0.3f, 1.5f, wyglad);
        Transform3D poz_box1Ram1 = new Transform3D();
        poz_box1Ram1.set(new Vector3f(0.0f, 5.0f, 2.0f));
        TransformGroup trans_box1Ram1 = new TransformGroup(poz_box1Ram1);
        trans_box1Ram1.addChild(box1Ram1);

        transGrPodst = new TransformGroup();
        transGrPodst.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transGrPodst.addChild(trans_cy1Ram1);
        transGrPodst.addChild(trans_cy2Ram1);
        transGrPodst.addChild(trans_box1Ram1);
        transGrPodst.setCollidable(false);
        scena.addChild(transGrPodst);

        //utworzenie ramiena numer 2 skladajacego sie z
        //dwoch walcow oraz jednego prostopadloscianu
        Cylinder cy1Ram2 = new Cylinder(0.6f, 0.7f, wyglad);
        Transform3D poz_cy1Ram2 = new Transform3D();
        poz_cy1Ram2.set(new Vector3f(0.0f, 4.3f, 4.0f));
        TransformGroup trans_cy1Ram2 = new TransformGroup(poz_cy1Ram2);
        trans_cy1Ram2.addChild(cy1Ram2);

        Cylinder cy2Ram2 = new Cylinder(0.6f, 0.7f, wyglad);
        Transform3D poz_cy2Ram2 = new Transform3D();
        poz_cy2Ram2.set(new Vector3f(0.0f, 4.3f, 8.0f));
        TransformGroup trans_cy2Ram2 = new TransformGroup(poz_cy2Ram2);
        trans_cy2Ram2.addChild(cy2Ram2);

        com.sun.j3d.utils.geometry.Box box1Ram2 = new com.sun.j3d.utils.geometry.Box(0.3f, 0.3f, 1.5f, wyglad);
        Transform3D poz_box1Ram2 = new Transform3D();
        poz_box1Ram2.set(new Vector3f(0.0f, 4.3f, 6.0f));
        TransformGroup trans_box1Ram2 = new TransformGroup(poz_box1Ram2);
        trans_box1Ram2.addChild(box1Ram2);

        transGrObrot1 = new TransformGroup();
        transGrObrot1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transGrObrot1.addChild(trans_cy1Ram2);
        transGrObrot1.addChild(trans_cy2Ram2);
        transGrObrot1.addChild(trans_box1Ram2);
        transGrObrot1.setCollidable(false);
        transGrPodst.addChild(transGrObrot1);

        // utworzenie podlogi
        Cylinder podloga = new Cylinder(10.0f, 0.2f, wygladPodloga);
        Transform3D poz_podloga = new Transform3D();
        poz_podloga.set(new Vector3f(0.0f, -0.35f, 0.0f));
        TransformGroup trans_podloga = new TransformGroup(poz_podloga);
        trans_podloga.addChild(podloga);
        scena.addChild(trans_podloga);

        // utworzenie chwytaka
        Cylinder cy1Chwyt = new Cylinder(0.3f, 4f, wyglad);
        Transform3D poz_cy1Chwyt = new Transform3D();
        poz_cy1Chwyt.set(new Vector3f(0.0f, 4.2f, 8.0f));
        TransformGroup trans_cy1Chwyt = new TransformGroup(poz_cy1Chwyt);
        trans_cy1Chwyt.addChild(cy1Chwyt);

        transGrChwyt = new TransformGroup();
        transGrChwyt.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transGrChwyt.setCollidable(true);
        transGrChwyt.addChild(trans_cy1Chwyt);
        transGrObrot1.addChild(transGrChwyt);
        BoundingSphere obszarKolizja = new BoundingSphere(new Point3d(), 1.9f);
        Kolizja kolizja = new Kolizja(cy1Chwyt, obszarKolizja, this);
        transGrChwyt.addChild(kolizja);

        // utworzenie krazka do przenoszenia dla robota
        Cylinder krazek = new Cylinder(1.2f, 0.9f, wygladKrazek);
        Transform3D poz_krazek = new Transform3D();
        poz_krazek.set(new Vector3f(0.0f, 0.2f, 8.0f));
        TransformGroup trans_krazek = new TransformGroup(poz_krazek);

        trans_krazek.addChild(krazek);

        transGrKrazek = new TransformGroup();
        transGrKrazek.setCollidable(true);
        transGrKrazek.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transGrKrazek.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        transGrKrazek.addChild(trans_krazek);

        scena.addChild(transGrKrazek);

        return scena;
    }

    public static void main(String args[]) throws InterruptedException {
        Projekt_JAVA projekt = new Projekt_JAVA();
    }
}
