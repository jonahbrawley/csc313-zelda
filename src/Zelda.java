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
		p1originalX = (double) XOFFSET + ((double) WINWIDTH / 2.15) - (p1width / 2.0);
		p1originalY = (double) YOFFSET + ((double) WINHEIGHT / 1.48) - (p1height / 2.0);

		try { // Get link graphics
			walk_left1 = ImageIO.read( new File("res/Zelda/player/walk_left1.png") );
			walk_left2 = ImageIO.read( new File("res/Zelda/player/walk_left2.png") );
			walk_right1 = ImageIO.read( new File("res/Zelda/player/walk_right1.png") );
			walk_right2 = ImageIO.read( new File("res/Zelda/player/walk_right2.png") );
			walk_down1 = ImageIO.read( new File("res/Zelda/player/walk_down1.png") );
			walk_down2 = ImageIO.read( new File("res/Zelda/player/walk_down2.png") );
			walk_up1 = ImageIO.read( new File("res/Zelda/player/walk_up1.png") );
			walk_up2 = ImageIO.read( new File("res/Zelda/player/walk_up2.png") );

			leftHeartOutline = ImageIO.read (new File ("res/Zelda/player/heartOutlineLeft.png") ) ;
			rightHeartOutline = ImageIO.read (new File ("res/Zelda/player/heartOutlineRight.png") ) ;
			leftHeart = ImageIO.read (new File ( "res/Zelda/player/heartLeft.png" ) ) ;
			rightHeart = ImageIO.read (new File ("res/Zelda/player/heartRight.png") ) ;

			Barriers = ImageIO.read( new File("res/Zelda/tiles/M3Doubledspace.png") );
            Map = ImageIO.read( new File("res/Zelda/tiles/M3Doubled.png") );
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
			p1.setLife(6);
			p1.setMaxLife(6);

			try { Thread.sleep(50); } catch (InterruptedException ie) { }

			endgame = false; // this variable is super not useful
			gameActive = true;
			gamePanel.startAnimation();

			lastDropLife = System.currentTimeMillis();
			t1 = new Thread( new PlayerOneMover() );
			t2 = new Thread( new HealthTracker() );

			t1.start();
			t2.start();
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

				g2D.drawImage(Barriers, XOFFSET, YOFFSET, null);
				g2D.drawImage(Map, XOFFSET, YOFFSET, null);

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

				healthDraw();

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
					p1.screenBounds(XOFFSET, WINWIDTH, YOFFSET, WINHEIGHT, p1.maxvelocity);
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
		private double x, y, xwidth, yheight, angle;
		public double maxvelocity;

		public ImageObject() {
			//maxFrames = 1;
			//currentFrame = 0;
			life = 1;
			maxLife = 1;
			dropLife = 0;
		}

		public ImageObject(double xinput, double yinput, double xwidthinput,
			double yheightinput/** , double angleinput*/) {
			x = xinput;
			y = yinput;
			xwidth = xwidthinput;
			yheight = yheightinput;
			angle = 75.0;
		}

		public double getX() { return x; }

		public double getY() { return y; }

		public double getWidth() { return xwidth; }

		public double getHeight() { return yheight; }

		public double getAngle() { return angle; }

		public void move(double xinput, double yinput) {
			x = x + xinput; 
			y = y + yinput;
		}

		public void moveto(double xinput, double yinput) {
			x = xinput; 
			y = yinput;
		}

		int currentSegment = 1;
		public void screenBounds(double leftEdge, double rightEdge, double topEdge, double bottomEdge, double maxvelocity) throws IOException {

			if (currentSegment == 1 && x + getWidth() > rightEdge) { //Done
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
				Map = ImageIO.read(new File("res/Zelda/tiles/N4Doubled.png"));
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
		
		public int getLife() {
			return life;
		}
	
		public void setLife(int input) {
			life = input;
		}
	
		public int getMaxLife() {
			return maxLife;
		}
	
		public void setMaxLife ( int input ) {
			maxLife = input ;
		}

		public int getDropLife ( ) {
			return dropLife ;
		}

		public void setDropLife ( int input ) {
			dropLife = input ;
		}

		private int life ;
		private int maxLife ;
		private int dropLife ;
	}

	// tracks health of player or enemy
	private static class HealthTracker implements Runnable {
		public void run() {
			while (endgame == false) {
				Long curTimeLong = new Long(System.currentTimeMillis());
				if (availableToDropLife && p1.getDropLife() > 0) {
					int newLife = p1.getLife() - p1.getDropLife();
					p1.setDropLife(0);
					availableToDropLife = false;
					lastDropLife = System.currentTimeMillis();
					p1.setLife(newLife);

					try {
						// AudioInputStream ais = AudioSystem.getAudioInputStream(
						// 	new File ("hurt.wav").getAbsoluteFile());
						// Clip hurtclip = AudioSystem.getClip();
						// hurtclip.open(ais);
						// hurtclip.start();
					}
					catch (Exception e) { }
				} else {
					if (curTimeLong - lastDropLife > dropLifeLifeTime) {
						availableToDropLife = true;
					}
				}
			}
		}
	}

	// rotates ImageObject
	private static AffineTransformOp affineTranform(ImageObject obj) {
		AffineTransform at = new AffineTransform();
		AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		return atop;
	}

	private static AffineTransformOp rotateImageObject(ImageObject obj) {
		AffineTransform at = AffineTransform.getRotateInstance(-obj.getAngle(), obj.getWidth() / 2.0, obj.getHeight() / 2.0);
		AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		return atop;
	}

	private static void healthDraw() {
		Graphics g = appFrame.getGraphics();
		Graphics2D g2D = (Graphics2D) g;

		int leftScale = 10;
		int leftOffset = 10;
		int rightOffset = 10;
		int interiorOffset = 2;
		int halfInteriorOffset = 1;

		for (int i = 0; i < p1.getMaxLife(); i++) {
			if (i % 2 == 0) {
				g2D.drawImage(rotateImageObject(p1).filter(leftHeartOutline, null), 
				leftScale * i + leftOffset + XOFFSET, YOFFSET, null);
			} else {
				g2D.drawImage(rotateImageObject(p1).filter(rightHeartOutline, null),
				leftScale * i + rightOffset + XOFFSET, YOFFSET, null);
			}
		}

		for (int i = 0; i < p1.getLife(); i++) {
			if (i % 2 == 0) {
				g2D.drawImage(rotateImageObject(p1).filter(leftHeart, null),
				leftScale * i + leftOffset + interiorOffset + XOFFSET, interiorOffset + YOFFSET, null);
			} else {
				g2D.drawImage(rotateImageObject(p1).filter(rightHeart, null), 
				leftScale * i + leftOffset - halfInteriorOffset + XOFFSET, interiorOffset + YOFFSET, null);
			}
		}
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

	private static int XOFFSET, YOFFSET, WINWIDTH, WINHEIGHT;

	private static ImageObject p1;
	private static double p1width, p1height, p1originalX, p1originalY;

	private static JFrame appFrame;
	private static GamePanel gamePanel;

	private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;

	private static BufferedImage Barriers, Map;
	private static BufferedImage walk_left1, walk_left2, walk_right1, walk_right2, walk_down1, walk_down2, walk_up1, walk_up2;
	private static double anim_counter = 1;

	private static BufferedImage leftHeartOutline;
	private static BufferedImage rightHeartOutline;
	private static BufferedImage leftHeart;
	private static BufferedImage rightHeart;

	private static Long dropLifeLifeTime;
	private static Long lastDropLife;
	private static Boolean availableToDropLife;

	private static Thread t1;
	private static Thread t2;
}