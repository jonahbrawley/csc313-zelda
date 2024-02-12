package src.src;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.ImageObserver;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;

import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Graphics2D;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.*;

import javax.swing.border.Border;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import javax.swing.*;
import java.awt.*;

public class Zelda {
    private static int currentSegment = 5;


    public Zelda() {
        src.src.Zelda.setup();
    }
    public static void setup() {
        appFrame = new JFrame("Zelda");
        XOFFSET = 0;
        YOFFSET = 0;
        WINWIDTH = 320; //each tile 160 but its x2
        WINHEIGHT = 256; //each tile 128 but its x2

        endgame = false;

        p1width = 50; //30
        p1height = 50; //30
        p1originalX = RESPAWN_X;
        p1originalY = RESPAWN_Y;


        try { // IO
            player1 = ImageIO.read( new File("res/Zelda/player/Link.png") );


            OnTrack = ImageIO.read( new File("res/Zelda/tiles/M3.png") );
            OffTrack = ImageIO.read( new File("res/Zelda/tiles/M3.png") );


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static class BackgroundMusic implements Runnable {
        private String file = "res/Zelda/Rainbow-Road-Mario-Kart-Wii.wav";
        public BackgroundMusic(String file) {
            this.file = file;
        }
        public void play() {
            Thread t = new Thread(this);
            t.start();
        }
        public void run() {
            playSound(file);
        }
        private void playSound(String file) {
            File soundFile = new File(file);
            AudioInputStream inputStream = null;
            try { // get input stream
                Clip clip = AudioSystem.getClip();
                inputStream = AudioSystem.getAudioInputStream(soundFile);
                clip.open(inputStream);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static void setButtonAppearance(JButton button) {
        button.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(15, URANIAN),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        button.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(HIGHLIGHT);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(URANIAN);
            }

        });


        button.setBackground(URANIAN);
        button.setForeground(Color.BLACK);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFocusPainted(false);
    }



    private static class RoundBorder implements Border { // Used for rounded buttons
        private int radius;
        private Color color;

        public RoundBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(color);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius, radius, radius, radius);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }

    private static class MyButton extends JButton {
        public MyButton(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));  // Adjust the radius here
            super.paintComponent(g);
            g2.dispose();
        }
    }

    private static class Animate implements Runnable, ImageObserver {
        int i = 0;
        public void run() {
            bs = appFrame.getBufferStrategy();
            if (bs == null) {
                return;
            }
            while (!endgame) {
                Graphics g = bs.getDrawGraphics();
                Graphics2D g2D = (Graphics2D) g;

                // Draw the track
                g2D.drawImage(OffTrack, XOFFSET, YOFFSET, null);
                g2D.drawImage(OnTrack, XOFFSET, YOFFSET, null);

                double speedShown = Math.round(p1velocity * 1000.0) / 1000.0;
//                int LapCounter

                g2D.setColor(Color.LIGHT_GRAY);

                g2D.setFont(new Font("Arial", Font.PLAIN, 10));

                g2D.drawString("Velocity: " + Math.round(speedShown * 10), 50, 50);
                g2D.drawString("Lap Counter " + lapCount, 150, 50);


                // Draw the player
                g2D.drawImage(rotateImageObject(p1).filter(player1, null), (int) (p1.getX() + 0.5),
                        (int) (p1.getY() + 0.5), null);



                if (spacePressed && !isCollidingWithGrass(p1.getX(), p1.getY(), OffTrack)) {
                    try {
                        player1 = ImageIO.read( new File("res/Zelda/tiles/M3.png") );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("Coords:" + p1.x + "  " + p1.y);


// RESPAWN PROGRAMMING
                i += 1;
                if (i==5 && !isCollidingWithGrass(p1.getX(), p1.getY(), OffTrack)) {
                    RESPAWN_X = p1.x;
                    RESPAWN_Y = p1.y;
                    i = 0;
                    System.out.println("   \nSpawn Reset\n   ");
                }
                if (i==5) {
                    i=0;
                }

                if (!spacePressed) {
                    try {
                        player1 = ImageIO.read(new File("res/Zelda/tiles/M3.png"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }

                g.dispose();
                g2D.dispose();
                bs.show();

                try {
                    Thread.sleep(32);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            return true;
        }


    }









    // thread responsible for updating player movement
    private static class PlayerMoverplayer1 implements Runnable {
        public PlayerMoverplayer1() {
            p1velocitystep = 0.02; // aka accel
            p1rotatestep = 0.03; //0.03
            p1maxvelocity = 5;
            p1brakingforce = 0.04;
            p1nitroBoost = 4;

        }

        public void run() {
            while (!endgame) {
                try {
                    Thread.sleep(9);
                } catch (InterruptedException e) { }


                if (isCollidingWithGrass(p1.getX(), p1.getY(), OffTrack)) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }


                    p1.moveto(RESPAWN_X,RESPAWN_Y);


                    p1velocity = 0.0;


                } else {
                    p1maxvelocity = 3;
                    p1velocitystep = 0.02; // aka accel

                }


                if (spacePressed == true && isCollidingWithGrass(p1.getX(), p1.getY(), OffTrack) == false) {
                    //CHEAT
                    // NITRO
                    if (upPressed == false) {
                        if (p1velocity < p1maxvelocity) {
                            p1velocity = (p1velocity) + p1velocitystep;
                        } else if (p1velocity >= p1maxvelocity) { // ensure max vel not exceeded
                            p1velocity = p1maxvelocity;
                        }
                    }



                    p1maxvelocity += p1nitroBoost;
                    p1velocitystep = 0.04;

                    double flameX = p1.getX() + (p1.getWidth() / 2.0) - nitroFlamePNGWidth / 2.0;
                    double flameY = p1.getY() + (p1.getHeight() / 2.0) - nitroFlamePNGHeight / 2.0;
                    nitroFlameX = flameX;
                    nitroFlameY = flameY;


                    System.out.println("BOOOOOOOST");
                }


                if (upPressed == true) {
                    if (p1velocity < p1maxvelocity) {
                        p1velocity = (p1velocity) + p1velocitystep;
                    } else if (p1velocity >= p1maxvelocity) { // ensure max vel not exceeded
                        p1velocity = p1maxvelocity;
                    }
                }
                if (downPressed == true) {
                    System.out.println("down IS BEING PRESSED");

                    if (p1velocity < -1) { // ensure max rev speed
                        p1velocity = -1;
                    } else {
                        p1velocity = p1velocity - p1brakingforce;
                    }
                }
                if (leftPressed == true) {
                    if (p1velocity < 0) {
                        p1.rotate(-p1rotatestep);
                    } else {
                        p1.rotate(p1rotatestep);
                    }
                }
                if (rightPressed == true) {
                    if (p1velocity < 0) {
                        p1.rotate(p1rotatestep);
                    } else {
                        p1.rotate(-p1rotatestep);
                    }
                }

                // apply drag force
                if (!upPressed && !downPressed && !leftPressed && !rightPressed && !spacePressed
                        && p1velocity != 0) {
                    if ((p1velocity - 0.1) < 0) {
                        p1velocity = 0;
                    } else {
                        p1velocity = p1velocity - 0.04;
                    }
                }

                p1.move(-p1velocity * Math.cos(p1.getAngle() - Math.PI / 2.0),
                        p1velocity * Math.sin(p1.getAngle() - Math.PI / 2.0));
                try {
                    p1.screenBounds(XOFFSET, WINWIDTH, YOFFSET, WINHEIGHT);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        private double p1velocitystep, p1rotatestep, p1maxvelocity, p1brakingforce, p1nitroBoost;
    }



    private static synchronized boolean isCollidingWithGrass(double bluecarX, double bluecarY, BufferedImage grass) {
        int x = (int) bluecarX;
        int y = (int) bluecarY;
        int x2 = (int) bluecarX;
        int y2 = (int) bluecarY;

        // Check if the coordinates are within bounds
        if (x >= 0 && x < grass.getWidth() && y >= 0 && y < grass.getHeight()) {
            int pixelColor = grass.getRGB(x, y);
            return (pixelColor & 0xFF000000) != 0;
        }

        // Check if the coordinates are within bounds
        if (x2 >= 0 && x2 < grass.getWidth() && y2 >= 0 && y2 < grass.getHeight()) {
            int pixelColor = grass.getRGB(x, y);
            return (pixelColor & 0xFF000000) != 0;
        }



        // If coordinates are out of bounds, consider it as colliding with grass
        return true;
    }

    // moveable image objects
    private static class ImageObject {
        private double x, y, xwidth, yheight, angle, internalangle, comX, comY;
        private Vector<Double> coords, triangles;

        public ImageObject() {
        }

        public ImageObject(double xinput, double yinput, double xwidthinput,
                           double yheightinput, double angleinput) {
            x = xinput;
            y = yinput;
            xwidth = xwidthinput;
            yheight = yheightinput;
            angle = angleinput;
            internalangle = 0.0;
            coords = new Vector<Double>();
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getWidth() {
            return xwidth;
        }

        public double getHeight() {
            return yheight;
        }

        public double getAngle() {
            return angle;
        }

        public double getInternalAngle() {
            return internalangle;
        }

        public void setAngle(double angleinput) {
            angle = angleinput;
        }

        public void setInternalAngle(double input) {
            internalangle = input;
        }

        public Vector<Double> getCoords() {
            return coords;
        }

        public void setCoords(Vector<Double> input) {
            coords = input;
            generateTriangles();
        }

        public void generateTriangles() {
            triangles = new Vector<Double>();
            // format: (0, 1), (2, 3), (4, 5) is x,y coords of triangle

            // get center point of all coords
            comX = getComX();
            comY = getComY();

            for (int i = 0; i < coords.size(); i = i + 2) {
                triangles.addElement(coords.elementAt(i));
                triangles.addElement(coords.elementAt(i + 1));

                triangles.addElement(coords.elementAt((i + 2) % coords.size()));
                triangles.addElement(coords.elementAt((i + 3) % coords.size()));

                triangles.addElement(comX);
                triangles.addElement(comY);
            }
        }


        public double getComX() {
            double ret = 0;
            if (coords.size() > 0) {
                for (int i = 0; i < coords.size(); i = i + 2) {
                    ret = ret + coords.elementAt(i);
                }
                ret = ret / (coords.size() / 2.0);
            }
            return ret;
        }

        public double getComY() {
            double ret = 0;
            if (coords.size() > 0) {
                for (int i = 1; i < coords.size(); i = i + 2) {
                    ret = ret + coords.elementAt(i);
                }
                ret = ret / (coords.size() / 2.0);
            }
            return ret;
        }

        public void move(double xinput, double yinput) {
            x = x + xinput;
            y = y + yinput;
        }

        public void moveto(double xinput, double yinput) {
            x = xinput;
            y = yinput;
        }


        int currentSegment = 5;
        public void screenBounds(double leftEdge, double rightEdge, double topEdge, double bottomEdge) throws IOException {

//            if (isCollidingWithGrass(p1.getX(), p1.getY(), OffTrack)) {
//                currentSegment = 5;
//            }

            if (currentSegment == 5 && x + getWidth() > rightEdge) {
                moveto((leftEdge+50) - getWidth(), getY());
                p1velocity = p1velocity * 0.9;
                System.out.println("Mario is touching right");
                currentSegment = 10;

                OnTrack = ImageIO.read(new File("res/Zelda/tiles/M4.png"));
                OffTrack = ImageIO.read(new File("res/Zelda/tiles/M4.png"));


            }


            if (currentSegment == 10 && y + getHeight() > bottomEdge) {
                moveto(getX(), topEdge+50);
                p1velocity = p1velocity * 0.9;
                System.out.println("Mario is touching bottom");
                currentSegment = 15;  // Reset to segment 1
                OnTrack = ImageIO.read(new File("res/Zelda/tiles/M5.png"));
                OffTrack = ImageIO.read(new File("res/Zelda/tiles/M5.png"));
                System.out.println(currentSegment);
            }


            if (currentSegment == 15 && x < leftEdge+20) {
                moveto(rightEdge-50, getY());
                p1velocity = p1velocity * 0.9;
                System.out.println("Mario is touching left");
                currentSegment = 20;

                OnTrack = ImageIO.read(new File("res/Zelda/tiles/M3.png"));
                OffTrack = ImageIO.read(new File("res/Zelda/tiles/M3.png"));
            }


            if (currentSegment == 20 && y < topEdge+20) {
                moveto(getX(), (bottomEdge-10) - getHeight());
                p1velocity = p1velocity * 0.9;
                System.out.println("Mario is touching top");
                currentSegment = 5;

                OnTrack = ImageIO.read(new File("res/Zelda/tiles/M4.png"));
                OffTrack = ImageIO.read(new File("res/Zelda/tiles/M4.png"));
            }

        }

        public void rotate ( double input){
            angle = angle + input;
            while (angle > (Math.PI * 2)) {
                angle = angle - (Math.PI * 2);
            }
            while (angle < 0) {
                angle = angle + (Math.PI * 2);
            }
        }

        public void spin ( double input){
            internalangle = internalangle + input;
            while (internalangle > (Math.PI * 2)) {
                internalangle = internalangle - (Math.PI * 2);
            }
            while (internalangle < 0) {
                internalangle = internalangle + (Math.PI * 2);
            }
        }
    }


    // rotates ImageObject
    private static AffineTransformOp rotateImageObject(ImageObject obj) {
        AffineTransform at = new AffineTransform();
        at.translate(obj.getWidth() / 2.0, obj.getHeight() / 2.0);
        at.rotate(-obj.getAngle());
        at.translate(-obj.getWidth() / 2.0, -obj.getHeight() / 2.0);
        return new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
    }

    // rotates ImageObject


    // initiates key actions from panel key responses
    private static void bindKey(JPanel panel, String input) {
        panel.getInputMap(IFW).put(KeyStroke.getKeyStroke("pressed " + input), input + " pressed");
        panel.getActionMap().put(input + " pressed", new KeyPressed(input));

        panel.getInputMap(IFW).put(KeyStroke.getKeyStroke("released " + input), input + " released");
        panel.getActionMap().put(input + " released", new KeyReleased(input));
    }

    private static class KeyPressed extends AbstractAction {
        public KeyPressed() { action = ""; }

        public KeyPressed(String input) { action = input; }

        public void actionPerformed(ActionEvent e) {
            if (action.equals("UP")) { upPressed = true; }
            if (action.equals("DOWN")) { downPressed = true; }
            if (action.equals("LEFT")) { leftPressed = true; }
            if (action.equals("RIGHT")) { rightPressed = true; }
            if (action.equals("SPACE")) { spacePressed = true; }

        }

        private String action;
    }

    private static class KeyReleased extends AbstractAction {
        public KeyReleased() { action = ""; }

        public KeyReleased(String input) { action = input; }

        public void actionPerformed(ActionEvent e) {
            if (action.equals("UP")) { upPressed = false; }
            if (action.equals("DOWN")) { downPressed = false; }
            if (action.equals("LEFT")) { leftPressed = false; }
            if (action.equals("RIGHT")) { rightPressed = false; }
            if (action.equals("SPACE")) { spacePressed = false; }

        }

        private String action;
    }

    private static class StartGame implements ActionListener {
        private final JPanel panel;

        public StartGame(JPanel panel) {
            this.panel = panel;
        }

        public void actionPerformed(ActionEvent ae) {
            startButton.setVisible(false);
            quitButton.setVisible(false);
            endgame = true;

            upPressed = false;
            downPressed = false;
            leftPressed = false;
            rightPressed = false;
            spacePressed = false;

            wPressed = false;
            sPressed = false;
            aPressed = false;
            dPressed = false;
            tabPressed = false;

            p1 = new ImageObject(p1originalX, p1originalY, p1width, p1height, 4.7);
            p1velocity = 0.0;


            try { Thread.sleep(32); } catch (InterruptedException ie) { }

            endgame = false;
            Thread t1 = new Thread( new Animate() );
            Thread t2 = new Thread( new PlayerMoverplayer1() );
            t1.start();
            t2.start();
        }
    }

    private static class QuitGame implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    public static void main(String[] args){
        setup();
        src.src.Zelda racer2D = new src.src.Zelda();
        Zelda.setup();
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setSize(WINWIDTH, WINHEIGHT);
        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 15;
        gbc.ipadx = 50;
        startButton = new MyButton("START RACE");
        startButton.addActionListener(new StartGame(myPanel));
        setButtonAppearance(startButton);
        myPanel.add(startButton, gbc);
        gbc.insets = new Insets(10, 0, 0, 0);
        quitButton = new MyButton("QUIT");
        quitButton.addActionListener(new QuitGame());
        setButtonAppearance(quitButton);
        myPanel.add(quitButton, gbc);
        bindKey(myPanel, "UP");
        bindKey(myPanel, "DOWN");
        bindKey(myPanel, "LEFT");
        bindKey(myPanel, "RIGHT");
        bindKey(myPanel, "SPACE");
        myPanel.setBackground(CELESTIAL);
        appFrame.getContentPane().add(myPanel, "Center");
        appFrame.setVisible(true);
        Random rand  = new Random();
        int rand_int1 = rand.nextInt(3);
        if (rand_int1 == 2) {
            BackgroundMusic menu_theme = new BackgroundMusic("res/Zelda/MarioKart64.wav");
            menu_theme.play();
        }
        else {
            BackgroundMusic menu_theme = new BackgroundMusic("res/Zelda/Rainbow-Road-Mario-Kart-Wii.wav");
            menu_theme.play();
        }
        appFrame.createBufferStrategy(2);//2
    }
    public static class Speedometer {
        private int x, y;
        private Font font;
        public Speedometer(int x, int y) {
            this.x = x;
            this.y = y;
            this.font = new Font("Arial", Font.PLAIN, 18);
        }
        public void draw(Graphics g, double speed) {
            g.setFont(font);
            g.setColor(Color.WHITE);
            g.drawString("Speed: " + String.format("%.2f", speed), x, y);
        }
    }
    private static long startTime;
    private static long lapStartTime;
    private static long bestLapTime = Long.MAX_VALUE;
    private static long currentLapTime;
    private static int lapCount = 0;
    private static boolean lapInProgress = false;
    private static Boolean endgame;
    private static Boolean upPressed, downPressed, leftPressed, rightPressed, spacePressed, wPressed, sPressed, aPressed, dPressed, tabPressed;
    private static JButton startButton, quitButton;
    private static Color CELESTIAL = new Color(64, 224, 208);
    private static Color HIGHLIGHT = new Color(199, 199, 199);
    private static Color URANIAN = new Color(164, 210, 232);
    private static int XOFFSET, YOFFSET, WINWIDTH, WINHEIGHT;
    private static ImageObject p1; // player1 and player2 racecar object
    private static double p1width, p1height, p1originalX, p1originalY, p1velocity, p2width, p2height, p2originalX, p2originalY, p2velocity, nitroFlamePNGWidth, nitroFlamePNGHeight, nitroFlameX, nitroFlameY;
    private static JFrame appFrame;
    private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;
    private static double RESPAWN_X = 1001.1883469051929 ; // Set the appropriate x-coordinate
    private static double RESPAWN_Y = 343.13283518037696; // Set the appropriate y-coordinate
    private static final double RESPAWN_X2 = 956.1883469051929 ; // Set the appropriate x-coordinate
    private static final double RESPAWN_Y2 = 343.13283518037696; // Set the appropriate y-coordinate
    private static BufferStrategy bs;
    private static BufferedImage OnTrack, OffTrack, player1;
}