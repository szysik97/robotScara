package projekt_java;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
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
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingPolytope;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SpotLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnCollisionEntry;
import javax.media.j3d.WakeupOnCollisionMovement;
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

    private JButton nagrywanie, odtwarzanie;
    private JButton przycRam1L, przycRam1P, przycRam2L, przycRam2P, przycChwytD, przycChwytG;

    //zmienne odpowiedzialne za nagrywanie i odtwarzanie ruchów robota
    private PrintWriter zapis;
    private Scanner odczyt;
    private Timer czas;
    private boolean trwaZapis = false;
    private boolean trwaOdczyt = false;
    private final int czasTimera = 50;
    private final String sciezkaPliku = "nagranie.txt";

    //zmienne typow podst. zwiazane z zachowaniem i pozycja robota
    private int ustawienie1 = 0, ustawienie2 = 0;
    private float ustawienie3 = 0.0f, ustawienieKrazekPion = 0.0f;
    private boolean czyTrzyma = false;

    private BranchGroup scena;
    private TransformGroup transGrPodst, transGrObrot1, transGrChwyt, transGrKrazek;
    private Transform3D obrotPodstawy = new Transform3D();
    private Transform3D obrot1 = new Transform3D();
    private Transform3D przesObrot1 = new Transform3D();
    private Transform3D ruchChwytak = new Transform3D();
    private Transform3D ruchKrazek = new Transform3D();
    private Transform3D obrotKrazek = new Transform3D();
    
    class ZadanieZapis extends TimerTask {

        String doZapisu;

        @Override
        public void run() {
            doZapisu = Integer.toString(ustawienie1) + ' '
                    + Integer.toString(ustawienie2) + ' '
                    + Float.toString(ustawienie3) + ' '
                    + Float.toString(ustawienieKrazekPion) + ' '
                    + Boolean.toString(czyTrzyma);

            try {
                zapis.println(doZapisu);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    class ZadanieOdczyt extends TimerTask {

        String[] odczytane;

        @Override
        public void run() {

            try {
                odczytane = odczyt.nextLine().split(" ");

                ustawienie1 = Integer.parseInt(odczytane[0]);
                ustawienie2 = Integer.parseInt(odczytane[1]);
                ustawienie3 = Float.parseFloat(odczytane[2]);
                ustawienieKrazekPion = Float.parseFloat(odczytane[3]);
                przestawRamie1();
                przestawRamie2();
                przestawChwytak();
                
                if (!odczyt.hasNext()) {
                    odczyt.close();
                    czas.cancel();
                    trwaOdczyt = false;
                    odtwarzanie.setText("Odtwórz nagranie");
                    nagrywanie.setEnabled(true);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    class Przyciski implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg) {
            JButton klik = (JButton) arg.getSource();

            if (klik == nagrywanie && !trwaOdczyt) {
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
            } else if (klik == odtwarzanie && !trwaZapis) {
                if (!trwaOdczyt) {
                    trwaOdczyt = true;
                    odtwarzanie.setText("Zakończ odtwarzanie");
                    nagrywanie.setEnabled(false);
                    odtwarzanie();
                } else {
                    trwaOdczyt = false;
                    czas.cancel();
                    odczyt.close();
                    odtwarzanie.setText("Odtwórz nagranie");
                    nagrywanie.setEnabled(true);
                }
            } else if (klik == przycRam1L && ustawienie1 > -180) {
                ustawienie1--;
                przestawRamie1();
            } else if (klik == przycRam1P && ustawienie1 < 180) {
                ustawienie1++;
                przestawRamie1();
            } else if (klik == przycRam2L && ustawienie2 > -180) {
                ustawienie2--;
                przestawRamie2();
            } else if (klik == przycRam2P && ustawienie2 < 180) {
                ustawienie2++;
                przestawRamie2();
            } else if (klik == przycChwytD && ustawienie3 > -1.6f) {
                ustawienie3 -= 0.1f;
                przestawChwytak();
            } else if (klik == przycChwytG && ustawienie3 < 1.6f) {
                ustawienie3 += 0.1f;
                przestawChwytak();
            }
        }
    }

    class Klawisze implements KeyListener {

        @Override
        public void keyPressed(KeyEvent arg) {

            if (arg.getKeyCode() == KeyEvent.VK_A && ustawienie1 > -180) {
                ustawienie1--;
                przestawRamie1();
            } else if (arg.getKeyCode() == KeyEvent.VK_D && ustawienie1 < 180) {
                ustawienie1++;
                przestawRamie1();
            } else if (arg.getKeyCode() == KeyEvent.VK_LEFT && ustawienie2 > -180) {
                ustawienie2--;
                przestawRamie2();
            } else if (arg.getKeyCode() == KeyEvent.VK_RIGHT && ustawienie2 < 180) {
                ustawienie2++;
                przestawRamie2();
            } else if(arg.getKeyCode() == KeyEvent.VK_DOWN && ustawienie3 > -1.6f) {
                ustawienie3 -= 0.1f;
                if(czyTrzyma == true){
                    ustawienieKrazekPion -= 0.1f;
                }
                przestawChwytak();
            } else if(arg.getKeyCode() == KeyEvent.VK_UP && ustawienie3 < 1.6f) {
                ustawienie3 += 0.1f;
                if(czyTrzyma == true){
                    ustawienieKrazekPion += 0.1f;
                }
                przestawChwytak();
            }
        }

        @Override
        public void keyReleased(KeyEvent arg) {
        }

        @Override
        public void keyTyped(KeyEvent arg) {
        }
    }

    class Kolizja extends Behavior{
        
        private Cylinder ksztalt;

        public Kolizja(Cylinder ksztalt, Bounds wiezy) {
            this.ksztalt = ksztalt;
            this.ksztalt.setCollisionBounds(wiezy);
            setSchedulingBounds(wiezy);
        }

        @Override
        public void initialize() {
            wakeupOn(new WakeupOnCollisionEntry(ksztalt));
        }

        @Override
        public void processStimulus(Enumeration enmrtn) {
            WakeupCriterion kryterium = (WakeupCriterion) enmrtn.nextElement();

                if(kryterium instanceof WakeupOnCollisionEntry) {
                    
                    System.out.print("Nastapila kolizja ");
                    czyTrzyma = true;
                }
            
        }
    }
    
    private void przestawRamie1() {
        obrotPodstawy.rotY(Math.toRadians(ustawienie1));
        transGrPodst.setTransform(obrotPodstawy);
        if(czyTrzyma == true){
            obrotKrazek.rotY(Math.toRadians(ustawienie1));
            ruchKrazek.setTranslation(new Vector3f(0.0f, ustawienieKrazekPion, 0.0f));
            obrotKrazek.mul(ruchKrazek);
            transGrKrazek.setTransform(obrotKrazek);
        }
        
    }

    private void przestawRamie2() {
        obrot1.rotY(Math.toRadians(ustawienie2));
        przesObrot1.setTranslation(new Vector3f(0.0f, 0.0f, 0.0f));
        obrot1.mul(przesObrot1);
        transGrObrot1.setTransform(obrot1);
        if(czyTrzyma == true){
            //transGrKrazek.setTransform(obrot1);
        }
        
    }

    private void przestawChwytak(){
        ruchChwytak.setTranslation(new Vector3f(0.0f, ustawienie3, 0.0f));
        transGrChwyt.setTransform(ruchChwytak);
        if(czyTrzyma == true){
            obrotKrazek.rotY(Math.toRadians(ustawienie1));
            ruchKrazek.setTranslation(new Vector3f(0.0f, ustawienieKrazekPion, 0.0f));
            obrotKrazek.mul(ruchKrazek);
            transGrKrazek.setTransform(obrotKrazek);
        }
        
    }
    
    private void nagrywanie() {
        try {
            zapis = new PrintWriter(sciezkaPliku);

            czas = new Timer();
            czas.scheduleAtFixedRate(new ZadanieZapis(), 0, czasTimera);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void odtwarzanie() {

        try {
            odczyt = new Scanner(new File(sciezkaPliku));

            czas = new Timer();
            czas.scheduleAtFixedRate(new ZadanieOdczyt(), 0, czasTimera);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
        
        
        JPanel panel1 = new JPanel(new BorderLayout());
        JPanel panel2 = new JPanel(new FlowLayout());
        JPanel panel3 = new JPanel(new FlowLayout());
        canvas.addKeyListener(new Klawisze());
        panel2.add(nagrywanie);
        panel2.add(odtwarzanie);
        panel3.add(new JLabel("RAMIE 1"));
        panel3.add(przycRam1L);
        panel3.add(przycRam1P);
        panel3.add(new JLabel("RAMIE 2"));
        panel3.add(przycRam2L);
        panel3.add(przycRam2P);
        panel3.add(new JLabel("CHWYTAK"));
        panel3.add(przycChwytD);
        panel3.add(przycChwytG);
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
        przesuniecie_obserwatora.set(new Vector3f(0.0f, 3.0f, 15.0f));//0 3 15

        SimpleUniverse simpleU = new SimpleUniverse(canvas);
        simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecie_obserwatora);
        simpleU.addBranchGraph(scena);

        //obracanie i zoom za pomoca myszki
        OrbitBehavior orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ROTATE);
        orbit.setSchedulingBounds(new BoundingSphere());
        simpleU.getViewingPlatform().setViewPlatformBehavior(orbit);
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

        //utworzenie nowego materialu
        Appearance wyglad = new Appearance();
        Material material = new Material(
                new Color3f(0.2f, 0.0f, 0.0f), //ambient
                new Color3f(0.0f, 0.0f, 0.6f), //emmisive
                new Color3f(0.6f, 0.0f, 0.0f), //diffuse
                new Color3f(1.0f, 1.0f, 1.0f), //specular
                50.0f);                                 //blyszczenie
        ColoringAttributes kolor = new ColoringAttributes();
        kolor.setShadeModel(ColoringAttributes.SHADE_GOURAUD);
        wyglad.setMaterial(material);
        wyglad.setColoringAttributes(kolor);

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

        //utworzenie ramiena numer 1 skladajacego sie z
        //dwoch walcow oraz jednego prostopadloscianu
        Cylinder cy1Ram2 = new Cylinder(0.6f, 0.7f, wyglad);
        Transform3D poz_cy1Ram2 = new Transform3D();
        poz_cy1Ram2.set(new Vector3f(0.0f, 4.2f, 4.0f));
        TransformGroup trans_cy1Ram2 = new TransformGroup(poz_cy1Ram2);
        trans_cy1Ram2.addChild(cy1Ram2);

        Cylinder cy2Ram2 = new Cylinder(0.6f, 0.7f, wyglad);
        Transform3D poz_cy2Ram2 = new Transform3D();
        poz_cy2Ram2.set(new Vector3f(0.0f, 4.2f, 8.0f));
        TransformGroup trans_cy2Ram2 = new TransformGroup(poz_cy2Ram2);
        trans_cy2Ram2.addChild(cy2Ram2);

        com.sun.j3d.utils.geometry.Box box1Ram2 = new com.sun.j3d.utils.geometry.Box(0.3f, 0.3f, 1.5f, wyglad);
        Transform3D poz_box1Ram2 = new Transform3D();
        poz_box1Ram2.set(new Vector3f(0.0f, 4.2f, 6.0f));
        TransformGroup trans_box1Ram2 = new TransformGroup(poz_box1Ram2);
        trans_box1Ram2.addChild(box1Ram2);

        transGrObrot1 = new TransformGroup();
        transGrObrot1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transGrObrot1.addChild(trans_cy1Ram2);
        transGrObrot1.addChild(trans_cy2Ram2);
        transGrObrot1.addChild(trans_box1Ram2);
        transGrObrot1.setCollidable(false);
        transGrPodst.addChild(transGrObrot1);

        //Chwytak - sam cylinder
        Cylinder cy1Chwyt = new Cylinder(0.4f, 4f, wyglad);
        Transform3D poz_cy1Chwyt = new Transform3D();
        poz_cy1Chwyt.set(new Vector3f(0.0f, 4.2f, 8.0f));
        TransformGroup trans_cy1Chwyt = new TransformGroup(poz_cy1Chwyt);
        trans_cy1Chwyt.addChild(cy1Chwyt);
        
        transGrChwyt = new TransformGroup();
        transGrChwyt.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transGrChwyt.setCollidable(true);
        transGrChwyt.addChild(trans_cy1Chwyt);
        transGrObrot1.addChild(transGrChwyt);
        BoundingSphere obszarKolizja = new BoundingSphere(new Point3d(), 2.0f);
        Kolizja kolizja = new Kolizja(cy1Chwyt, obszarKolizja);
        transGrChwyt.addChild(kolizja);
        
        //krazek do przenoszenia
        Cylinder krazek = new Cylinder(1.2f, 1.4f, wyglad);
        Transform3D poz_krazek = new Transform3D();
        poz_krazek.set(new Vector3f(0.0f, 0.0f, 8.0f));
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
