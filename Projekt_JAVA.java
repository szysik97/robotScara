
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.Timer;
import javax.media.j3d.Appearance;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.SpotLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnCollisionEntry;
import javax.media.j3d.WakeupOnCollisionExit;
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
    private JButton nagrywanie, odtwarzanie;
    private JButton przycRam1L, przycRam1P, przycRam2L, przycRam2P, przycChwytD, przycChwytG;
    private JButton sposobSterowania;
    private JButton zakonczTrzymanie;

    //obiekt do komunikacji z Arduino za pomoca SerialPortu
    private KomunikacjaArduino arduino;
    private boolean sterowanieArduino = false;

    //zmienne odpowiedzialne za nagrywanie i odtwarzanie ruchów robota
    private PrintWriter zapis;
    private Scanner odczyt;
    private Timer czas;
    private boolean trwaZapis = false;
    private boolean trwaOdczyt = false;
    private final int CZAS_TIMERA = 50;
    private final String SCIEZKA_PLIKU = "nagranie.txt";

    //zmienne typow podst. zwiazane z zachowaniem i pozycja robota
    private int ustawienie1 = 0, ustawienie2 = 0;
    private float ustawienie3 = 0.0f, ustawienieKrazekPion = 0.0f;
    private boolean czyTrzyma = false;
    private boolean czySpada = false;
    private int ustawienie1Spadanie, ustawienie2Spadanie;
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

    private class ZadanieZapis extends TimerTask {

        String doZapisu;

        @Override
        public void run() {
            doZapisu = Integer.toString(ustawienie1) + ' '
                    + Integer.toString(ustawienie2) + ' '
                    + Float.toString(ustawienie3) + ' '
                    + Float.toString(ustawienieKrazekPion) + ' '
                    + Boolean.toString(czyTrzyma) + ' '
                    + Boolean.toString(czySpada) + ' '
                    + Integer.toString(ustawienie1Spadanie) + ' '
                    + Integer.toString(ustawienie2Spadanie);

            try {
                zapis.println(doZapisu);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private class ZadanieArduino extends TimerTask {

        int odczytane;

        @Override
        public void run() {

            try {

                odczytane = arduino.odczytaj();

                if (odczytane != 0) {
                    if (odczytane == '1')
                        przestawRamie1Lewo();
                    else if (odczytane == '2')
                        przestawRamie1Prawo();
                    else if (odczytane == '3')
                        przestawRamie2Lewo();
                    else if (odczytane == '4')
                        przestawRamie2Prawo();
                    else if (odczytane == '5')
                        przestawChwytakGora();
                    else if (odczytane == '6')
                        przestawChwytakDol();
                    else if (odczytane == '7')
                        wypuszczanie();

                    arduino.resetujOdczytane();
                }

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private class Grawitacja extends TimerTask {

        @Override
        public void run() {
            if (ustawienieKrazekPion > 0.09f) {
                ustawienieKrazekPion -= 0.1f;
                przestawKrazek(ustawienie1Spadanie, ustawienie2Spadanie);
            } else {
                czySpada = false;
                this.cancel();
            }
        }
    }

    private class ZadanieOdczyt extends TimerTask {

        String[] odczytane;

        @Override
        public void run() {

            try {
                odczytane = odczyt.nextLine().split(" ");

                ustawienie1 = Integer.parseInt(odczytane[0]);
                ustawienie2 = Integer.parseInt(odczytane[1]);
                ustawienie3 = Float.parseFloat(odczytane[2]);
                ustawienieKrazekPion = Float.parseFloat(odczytane[3]);
                czyTrzyma = Boolean.parseBoolean(odczytane[4]);
                czySpada = Boolean.parseBoolean(odczytane[5]);
                ustawienie1Spadanie = Integer.parseInt(odczytane[6]);
                ustawienie2Spadanie = Integer.parseInt(odczytane[7]);

                przestawRamie1();
                przestawRamie2();
                przestawChwytak();

                if (!odczyt.hasNext()) {
                    odczyt.close();
                    czas.cancel();
                    trwaOdczyt = false;
                    odtwarzanie.setText("Odtwórz nagranie");
                    nagrywanie.setEnabled(true);
                    sposobSterowania.setEnabled(true);
                    przyciskiDostepnosc(true);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private class Przyciski implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg) {
            JButton klik = (JButton) arg.getSource();

            if (klik == nagrywanie && !trwaOdczyt)
                if (!trwaZapis) {
                    trwaZapis = true;
                    nagrywanie.setText("Zakończ nagrywanie");
                    odtwarzanie.setEnabled(false);
                    nagrywanie();
                } else {
                    trwaZapis = false;
                    czas.cancel();
                    zapis.close();
                    nagrywanie.setText("Rozpocznij nagrywanie");
                    odtwarzanie.setEnabled(true);
                }
            else if (klik == odtwarzanie && !trwaZapis)
                if (!trwaOdczyt) {
                    trwaOdczyt = true;
                    odtwarzanie.setText("Zakończ odtwarzanie");
                    nagrywanie.setEnabled(false);
                    sposobSterowania.setEnabled(false);
                    przyciskiDostepnosc(false);
                    odtwarzanie();
                } else {
                    trwaOdczyt = false;
                    czas.cancel();
                    odczyt.close();
                    odtwarzanie.setText("Odtwórz nagranie");
                    nagrywanie.setEnabled(true);
                    sposobSterowania.setEnabled(true);
                    przyciskiDostepnosc(true);
                }
            else if (klik == przycRam1L)
                przestawRamie1Lewo();
            else if (klik == przycRam1P)
                przestawRamie1Prawo();
            else if (klik == przycRam2L)
                przestawRamie2Lewo();
            else if (klik == przycRam2P)
                przestawRamie2Prawo();
            else if (klik == przycChwytD)
                przestawChwytakDol();
            else if (klik == przycChwytG)
                przestawChwytakGora();
            else if (klik == sposobSterowania)
                if (!sterowanieArduino) {
                    sterowanieArduino = true;
                    przyciskiDostepnosc(false);

                    czas = new Timer();
                    czas.scheduleAtFixedRate(new ZadanieArduino(), 0, 50);
                    sposobSterowania.setText("Zmień sterowanie na: klawiatuta/GUI");
                } else {
                    sterowanieArduino = false;
                    czas.cancel();
                    przyciskiDostepnosc(true);
                    sposobSterowania.setText("Zmień sterowanie na: Arduino");
                }
            else if (klik == zakonczTrzymanie)
                wypuszczanie();
        }
    }

    private class Klawisze implements KeyListener {

        @Override
        public void keyPressed(KeyEvent arg) {

            if (arg.getKeyCode() == KeyEvent.VK_A)
                przestawRamie1Lewo();
            else if (arg.getKeyCode() == KeyEvent.VK_D)
                przestawRamie1Prawo();
            else if (arg.getKeyCode() == KeyEvent.VK_LEFT)
                przestawRamie2Lewo();
            else if (arg.getKeyCode() == KeyEvent.VK_RIGHT)
                przestawRamie2Prawo();
            else if (arg.getKeyCode() == KeyEvent.VK_DOWN)
                przestawChwytakDol();
            else if (arg.getKeyCode() == KeyEvent.VK_UP)
                przestawChwytakGora();
            else if (arg.getKeyCode() == KeyEvent.VK_SPACE)
                wypuszczanie();
        }

        @Override
        public void keyReleased(KeyEvent arg) {
        }

        @Override
        public void keyTyped(KeyEvent arg) {
        }
    }

    private class Kolizja extends Behavior {

        private Cylinder ksztalt;
        private WakeupOnCollisionEntry zdarzenieKolizja;
        private WakeupOnCollisionExit zdarzenieKoniecKolizji;

        public Kolizja(Cylinder ksztalt, Bounds wiezy) {
            this.ksztalt = ksztalt;
            this.ksztalt.setCollisionBounds(wiezy);
            setSchedulingBounds(wiezy);
        }

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
                if (!trwaOdczyt) {
                    czyTrzyma = true;
                    zakonczTrzymanie.setEnabled(true);
                }
            } else if (kryterium instanceof WakeupOnCollisionExit)
                wakeupOn(zdarzenieKolizja);

        }
    }

    private void przestawRamie1Lewo() {
        if (ustawienie1 > -180) {
            ustawienie1--;
            przestawRamie1();
        }
    }

    private void przestawRamie1Prawo() {
        if (ustawienie1 < 180) {
            ustawienie1++;
            przestawRamie1();
        }
    }

    private void przestawRamie2Lewo() {
        if (ustawienie2 > -140) {
            ustawienie2--;
            przestawRamie2();
        }
    }

    private void przestawRamie2Prawo() {
        if (ustawienie2 < 140) {
            ustawienie2++;
            przestawRamie2();
        }
    }

    private void przestawChwytakGora() {
        if (ustawienie3 < 1.6f) {
            ustawienie3 += 0.1f;
            if (czyTrzyma == true)
                ustawienieKrazekPion += 0.1f;
            przestawChwytak();
        }
    }

    private void przestawChwytakDol() {
        if (ustawienie3 > -1.7f) {
            ustawienie3 -= 0.1f;
            if (czyTrzyma == true)
                ustawienieKrazekPion -= 0.1f;
            przestawChwytak();
        }
    }

    private void przestawRamie1() {
        obrotPodstawy.rotY(Math.toRadians(ustawienie1));
        transGrPodst.setTransform(obrotPodstawy);
        przestawKrazek(ustawienie1, ustawienie2);
    }

    private void przestawRamie2() {
        obrot1.rotY(Math.toRadians(ustawienie2));
        obrot1.setTranslation(new Vector3f(0.0f, 0.0f, 4.0f));
        transGrObrot1.setTransform(obrot1);
        przesObrot1.setTranslation(new Vector3f(0.0f, 0.0f, -4.0f));
        obrot1.mul(przesObrot1);
        transGrObrot1.setTransform(obrot1);
        przestawKrazek(ustawienie1, ustawienie2);
    }

    private void przestawChwytak() {
        ruchChwytak.setTranslation(new Vector3f(0.0f, ustawienie3, 0.0f));
        transGrChwyt.setTransform(ruchChwytak);
        przestawKrazek(ustawienie1, ustawienie2);
    }

    private void przestawKrazek(int ustaw1, int ustaw2) {

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

    private void nagrywanie() {

        ustawienie1Odtwarzanie = ustawienie1;
        ustawienie2Odtwarzanie = ustawienie2;

        try {
            zapis = new PrintWriter(SCIEZKA_PLIKU);

            czas = new Timer();
            czas.scheduleAtFixedRate(new ZadanieZapis(), 0, CZAS_TIMERA);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void wypuszczanie() {
        czySpada = true;
        czyTrzyma = false;
        ustawienie1Spadanie = ustawienie1;
        ustawienie2Spadanie = ustawienie2;

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new Grawitacja(), 0, 80);
        zakonczTrzymanie.setEnabled(false);
    }

    private void przyciskiDostepnosc(boolean bln) {

        przycRam1L.setEnabled(bln);
        przycRam1P.setEnabled(bln);
        przycRam2L.setEnabled(bln);
        przycRam2P.setEnabled(bln);
        przycChwytG.setEnabled(bln);
        przycChwytD.setEnabled(bln);
        zakonczTrzymanie.setEnabled(bln);
    }

    private void odtwarzanie() {

        ustawienie1 = ustawienie1Odtwarzanie;
        ustawienie2 = ustawienie2Odtwarzanie;
        przestawKrazek(ustawienie1Odtwarzanie, ustawienie2Odtwarzanie);

        try {
            odczyt = new Scanner(new File(SCIEZKA_PLIKU));

            czas = new Timer();
            czas.scheduleAtFixedRate(new ZadanieOdczyt(), 0, CZAS_TIMERA);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

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

        //upchanie wszystkiego w okienku
        Przyciski przyciskiListener = new Przyciski();
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

        JPanel panel1 = new JPanel(new BorderLayout());
        JPanel panel2 = new JPanel(new FlowLayout());
        JPanel panel3 = new JPanel(new FlowLayout());
        canvas.addKeyListener(new Klawisze());
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

        //utworzenie podstawy robota
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

        //podloga
        Cylinder podloga = new Cylinder(10.0f, 0.2f, wygladPodloga);
        Transform3D poz_podloga = new Transform3D();
        poz_podloga.set(new Vector3f(0.0f, -0.35f, 0.0f));
        TransformGroup trans_podloga = new TransformGroup(poz_podloga);
        trans_podloga.addChild(podloga);
        scena.addChild(trans_podloga);

        //Chwytak - sam cylinder
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
        Kolizja kolizja = new Kolizja(cy1Chwyt, obszarKolizja);
        transGrChwyt.addChild(kolizja);

        //krazek do przenoszenia
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
