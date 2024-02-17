import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.border.Border;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList; // import ArrayList 

public class Zelda {
	public Zelda() {
		setup();
		appFrame = new JFrame("The Legend of Zelda - Demo");
		initUI();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new Zelda();
		});
	}

	private static void initUI() {
		appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		appFrame.setSize(WINWIDTH, WINHEIGHT);

		gamePanel = new GamePanel();
		gamePanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.ipady = 15;
		gbc.ipadx = 50;

		    // Add a rigid area to create space between the image and buttons
		    gamePanel.add(Box.createRigidArea(new Dimension(0, 10)), gbc);

			gbc.insets = new Insets(10, 0, 0, 0);

			startButton = new MyButton("Start");
			startButton.addActionListener(new StartGame((GamePanel) gamePanel));
			setButtonAppearance(startButton);
			gamePanel.add(startButton, gbc);

			gbc.insets = new Insets(10, 0, 0, 0);
			
			quitButton = new MyButton("Quit");
			quitButton.addActionListener(new QuitGame());
			setButtonAppearance(quitButton);
			gamePanel.add(quitButton, gbc);

			bindKey((JPanel) gamePanel, "UP");
			bindKey((JPanel) gamePanel, "DOWN");
			bindKey((JPanel) gamePanel, "LEFT");
			bindKey((JPanel) gamePanel, "RIGHT");

		gamePanel.setBackground(CELESTIAL);
		appFrame.getContentPane().add(gamePanel, "Center");
		appFrame.setVisible(true);

		BackgroundSound theme = new BackgroundSound("res/overworld.wav", true);
		
		if (SOUNDS_ENABLED) {
			theme.play();
		}
	}

	public static void setup() {
		XOFFSET = 0;
		YOFFSET = 0;
		WINWIDTH = 320; // tile width 160 but x2
        WINHEIGHT = 256; // tile height 128 but x2
		endgame = false;

		p1width = 30;
		p1height = 30;
		p1originalX = 40.0; // hard coded
		p1originalY = 60.0; // hard coded

		try { // Get link graphics
			walk_left1 = ImageIO.read( new File("res/Zelda/player/walk_left1.png") );
			walk_left2 = ImageIO.read( new File("res/Zelda/player/walk_left2.png") );
			walk_right1 = ImageIO.read( new File("res/Zelda/player/walk_right1.png") );
			walk_right2 = ImageIO.read( new File("res/Zelda/player/walk_right2.png") );
			walk_down1 = ImageIO.read( new File("res/Zelda/player/walk_down1.png") );
			walk_down2 = ImageIO.read( new File("res/Zelda/player/walk_down2.png") );
			walk_up1 = ImageIO.read( new File("res/Zelda/player/walk_up1.png") );
			walk_up2 = ImageIO.read( new File("res/Zelda/player/walk_up2.png") );

			Barriers = ImageIO.read( new File("res/Zelda/tiles/M3Doubledspace.png") );
            Map = ImageIO.read( new File("res/Zelda/tiles/M3Doubled.png") );

			heart1 = ImageIO.read(new File("res/Zelda/healthbar/healthheart.png"));
			heart2 = ImageIO.read(new File("res/Zelda/healthbar/healthheart.png"));
			heart3 = ImageIO.read(new File("res/Zelda/healthbar/healthheart.png"));

			// save player walkable pixels and dungeon door region into sep arraylists
			BufferedImage N4MapKey = ImageIO.read(new File("res/Zelda/tiles/N4MapKey.png"));
			regionDungeonDoor = loadRegion(N4MapKey, regionBLUE);
			System.out.println(regionDungeonDoor);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// -------- BUTTON ACTIONS --------
	private static class StartGame implements ActionListener {
		private final GamePanel gamePanel;

		public StartGame(GamePanel gamePanel) {
			this.gamePanel = gamePanel;
		}

		public void actionPerformed(ActionEvent ae) { // autechre
			startButton.setVisible(false);
			quitButton.setVisible(false);
			endgame = true;

			upPressed = false;
			downPressed = false;
			leftPressed = false;
			rightPressed = false;

			p1 = new ImageObject(p1originalX, p1originalY, p1width, p1height);

			try { Thread.sleep(50); } catch (InterruptedException ie) { }

			endgame = false; // this variable is super not useful
			gameActive = true;
			gamePanel.startAnimation();

			t1 = new Thread( new PlayerOneMover() );

			t1.start();
		}
	}

	private static class QuitGame implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	// -------- ANIMATION AND MOVEMENT --------
	// Proper way to display graphics is by overriding JPanel's paintComponent method
	public static class GamePanel extends JPanel {
		private Timer timer;
		private BufferedImage player = walk_left1;
		private double increment = 0.35;

		public GamePanel() {
			timer = new Timer(32, new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (gameActive || GameOver) {
						repaint();
					}
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (gameActive) {
				Graphics2D g2D = (Graphics2D) g;

				System.out.println(p1.getX()+ " " + p1.getY());


				g2D.drawImage(Barriers, XOFFSET, YOFFSET, null);
				g2D.drawImage(Map, XOFFSET, YOFFSET, null);

				g2D.drawImage(heart1, 20, 20, null);
				g2D.drawImage(heart2, 50, 20, null);
				g2D.drawImage(heart3, 80, 20, null);


				if (leftPressed && anim_counter < 2) {
					player = walk_left1;
					anim_counter += increment;
				} else if (leftPressed) {
					player = walk_left2;
					anim_counter += increment;
				}

				if (rightPressed && anim_counter < 2) {
					player = walk_right1;
					anim_counter += increment;
				} else if (rightPressed) {
					player = walk_right2;
					anim_counter += increment;
				}

				if (downPressed && anim_counter < 2) {
					player = walk_down1;
					anim_counter += increment;
				} else if (downPressed) {
					player = walk_down2;
					anim_counter += increment;
				}

				if (upPressed && anim_counter < 2) {
					player = walk_up1;
					anim_counter += increment;
				} else if (upPressed) {
					player = walk_up2;
					anim_counter += increment;
				}

				// dont draw player objects if they are "dead" (for 3 seconds)
				if (!p1dead) {
					g2D.drawImage(affineTranform(p1).filter(player, null), (int)(p1.getX() + 0.5),
					(int)(p1.getY() + 0.5), null);
				}

				if (anim_counter > 3) { anim_counter = 1; }

				g2D.dispose();
			}
		}

		public void startAnimation() { timer.start(); }

		public void stopAnimation() { timer.stop(); }
	}

	// updating player one movement
	private static class PlayerOneMover implements Runnable {
		public PlayerOneMover() {
			speed = 1;
		}

		public void run() {
			while (!endgame) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) { }

				if (upPressed == true) {
					p1.move(0, -speed);
				}

				if (downPressed == true) {
					p1.move(0, speed);
				}

				if (leftPressed == true) {
					p1.move(-speed, 0);
				}

				if (rightPressed == true) {
					p1.move(speed, 0);
				}

				try {
					p1.screenBounds(XOFFSET - 10, WINWIDTH - 10, YOFFSET - 5,
							WINHEIGHT + 5, p1.maxvelocity); // a little more accurate to sprite					p1.enemyHitBoxes();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		private double speed;
	}
	// initiates key actions from panel key responses
	private static void bindKey(JPanel panel, String input) {
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke("pressed " + input), input + " pressed");
		panel.getActionMap().put(input + " pressed", new KeyPressed(input));

		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke("released " + input), input + " released");
		panel.getActionMap().put(input + " released", new KeyReleased(input));
	}

	// monitors key presses
	private static class KeyPressed extends AbstractAction {
		public KeyPressed(String input) { action = input; }

		public void actionPerformed(ActionEvent e) {
			if (action.equals("UP")) { upPressed = true; }
			if (action.equals("DOWN")) { downPressed = true; }
			if (action.equals("LEFT")) { leftPressed = true; }
			if (action.equals("RIGHT")) { rightPressed = true; }
		}
	
		private String action;
	}

	// monitors key releases
	private static class KeyReleased extends AbstractAction {
		public KeyReleased(String input) { action = input; }

		public void actionPerformed(ActionEvent e) {
			if (action.equals("UP")) { upPressed = false; }
			if (action.equals("DOWN")) { downPressed = false; }
			if (action.equals("LEFT")) { leftPressed = false; }
			if (action.equals("RIGHT")) { rightPressed = false; }
		}

		private String action;
	}

	// -------- UI AND APPEARANCE --------
	public static class BackgroundSound implements Runnable {
		private String file;
		private boolean loopAudio;

		public BackgroundSound(String file, Boolean isLoop) {
			this.file = file;
			this.loopAudio = isLoop;
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
				if (loopAudio) { 
					clip.loop(Clip.LOOP_CONTINUOUSLY); 
				} else {
					clip.start();
				}
	        } 
	        catch (Exception e) {
	            e.printStackTrace();
	        }
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

    // -------- UTILITY FUNCTIONS --------
	// moveable image objects
	private static class ImageObject {
		private double x, y, xwidth, yheight;
		public double maxvelocity;

		public ImageObject(double xinput, double yinput, double xwidthinput,
			double yheightinput) {
			x = xinput;
			y = yinput;
			xwidth = xwidthinput;
			yheight = yheightinput;
		}

		public double getX() { return x; }

		public double getY() { return y; }

		public double getWidth() { return xwidth; }

		public double getHeight() { return yheight; }

		//public double getAngle() { return angle; }

		public void move(double xinput, double yinput) {
			x = x + xinput; 
			y = y + yinput;
		}

		public void moveto(double xinput, double yinput) {
			x = xinput; 
			y = yinput;
		}

		public void enemyHitBoxes() throws IOException {
			if (currentSegment == 1  && p1.getX() > 73 && p1.getX() < 117 && p1.getY() > 49 && p1.getY() < 6) {
				heart3 = ImageIO.read(new File("res/Zelda/healthbar/blankheart"));
			}
		}

		int currentSegment = 1;
		// currSegment == what map tile you are on

		public void screenBounds(double leftEdge, double rightEdge, double topEdge, double bottomEdge, double maxvelocity) throws IOException {
			if (currentSegment == 1 && x + getWidth() > rightEdge) { //
				moveto((leftEdge+50) - getWidth(), getY());
				System.out.println("Link is touching right");
				currentSegment = 2;
				Map = ImageIO.read(new File("res/Zelda/tiles/M4Doubled.png"));
				Barriers = ImageIO.read(new File("res/Zelda/tiles/M3Doubledspace.png"));
			}
			if (currentSegment == 1 && y + getHeight() > bottomEdge) { //Done
				moveto(getX(), topEdge+50);
				System.out.println("Link is touching bottom");
				currentSegment = 4;
				Map = ImageIO.read(new File("res/Zelda/tiles/N3Doubled.png"));
				Barriers = ImageIO.read(new File("res/Zelda/tiles/M3Doubledspace.png"));
			}

			if (currentSegment == 2 && y + getHeight() > bottomEdge) { //Done
				moveto(getX(), topEdge+50);
				System.out.println("Link is touching bottom");
				currentSegment = 3;
				Map = ImageIO.read(new File("res/Zelda/tiles/N4Doubled.png"));
				Barriers = ImageIO.read(new File("res/Zelda/tiles/M3Doubledspace.png"));
			}
			if (currentSegment == 2 && x < leftEdge+20) { //Done
				moveto(rightEdge-50, getY());
				System.out.println("Link is touching left");
				currentSegment = 1;
				Map = ImageIO.read(new File("res/Zelda/tiles/M3Doubled.png"));
				Barriers = ImageIO.read(new File("res/Zelda/tiles/M3Doubledspace.png"));
			}

			if (currentSegment == 3 && y < topEdge+20) { //Done
				moveto(getX(), (bottomEdge-10) - getHeight());
				System.out.println("Link is touching top");
				currentSegment = 2;
				Map = ImageIO.read(new File("res/Zelda/tiles/M4Doubled.png"));
				Barriers = ImageIO.read(new File("res/Zelda/tiles/M3Doubledspace.png"));
			}

			if (currentSegment == 3 && x < leftEdge+20) { //Done
				moveto(rightEdge-50, getY());
				System.out.println("Link is touching left");
				currentSegment = 4;
				Map = ImageIO.read(new File("res/Zelda/tiles/N3Doubled.png"));
				Barriers = ImageIO.read(new File("res/Zelda/tiles/M3Doubledspace.png"));
			}

			if (currentSegment == 4 && x > rightEdge+20) {
				moveto((leftEdge+50) - getWidth(), getY());
				System.out.println("Link is touching right");
				currentSegment = 3;
				Map = ImageIO.read(new File("res/Zelda/tiles/N4Doubled.png")); // tile with dungeon entrance
				Barriers = ImageIO.read(new File("res/Zelda/tiles/M3Doubledspace.png"));
			}

			if (currentSegment == 4 && y < topEdge+20) {
				moveto(getX(), (bottomEdge-10) - getHeight());
				System.out.println("Link is touching top");
				currentSegment = 1;
				Map = ImageIO.read(new File("res/Zelda/tiles/M3Doubled.png"));
				Barriers = ImageIO.read(new File("res/Zelda/tiles/M3Doubledspace.png"));
			}
		}
	}

	// rotates ImageObject
	private static AffineTransformOp affineTranform(ImageObject obj) {
		AffineTransform at = new AffineTransform();
		AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		return atop;
	}

	private static ArrayList<Point> loadRegion(BufferedImage mapkey, Color regionCOLOR) {
		ArrayList<Point> region = new ArrayList<>();
		int rgnCol = regionCOLOR.getRGB();

		for (int x = 0; x < WINWIDTH; x++) {
            for (int y = 0; y < WINHEIGHT; y++) {
                if (mapkey.getRGB(x, y) == rgnCol) {
					region.add(new Point(x, y));
				}
            }
        }

		return region;
	}

	// -------- GLOBAL VARIABLES --------
	private static Boolean endgame;
	private static Boolean GameOver = false;
	private static boolean gameActive = false;
	private static Boolean upPressed, downPressed, leftPressed, rightPressed;
	private static Boolean p1dead = false;
	private static Boolean SOUNDS_ENABLED = true; // ENABLE OR DISABLE FOR SOUND

	private static JButton startButton, quitButton;

	private static Color CELESTIAL = new Color(49, 151, 199);
	private static Color HIGHLIGHT = new Color(110, 168, 195);
	private static Color URANIAN = new Color(164, 210, 232);
	private static Color regionBLUE = new Color(0, 30, 255);
	private static Color regionRED = new Color(255, 0, 0);

	private static int XOFFSET, YOFFSET, WINWIDTH, WINHEIGHT;

	private static ImageObject p1;
	private static double p1width, p1height, p1originalX, p1originalY;

	private static JFrame appFrame;
	private static GamePanel gamePanel;

	private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;

	private static BufferedImage Barriers, Map;
	private static BufferedImage walk_left1, walk_left2, walk_right1, walk_right2, walk_down1, walk_down2, walk_up1, walk_up2;
	private static BufferedImage heart1, heart2, heart3;

	private static ArrayList<Point> regionDungeonDoor;
	private static ArrayList<Point> regionN4;

	private static double anim_counter = 1;

	private static Thread t1;
}