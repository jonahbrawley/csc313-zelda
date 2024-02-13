import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
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

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.*;
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

			// Create and add the image label
		    // ImageIcon titleImage = new ImageIcon("title.png");
		    // titleLabel = new JLabel(titleImage);
		    // gamePanel.add(titleLabel, gbc);

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

			// bindKey((JPanel) gamePanel, "W");
			// bindKey((JPanel) gamePanel, "A");
			// bindKey((JPanel) gamePanel, "S");
			// bindKey((JPanel) gamePanel, "D");

		gamePanel.setBackground(CELESTIAL);
		appFrame.getContentPane().add(gamePanel, "Center");
		appFrame.setVisible(true);

		// BackgroundSound menu_theme = new BackgroundSound("menu.wav", true);
		
		// if (SOUNDS_ENABLED) {
		// 	menu_theme.play();
		// }
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

		try { // IO
			player1 = ImageIO.read( new File("res/Zelda/player/Link.png") );
            OffTrack = ImageIO.read( new File("res/Zelda/tiles/M3Doubledspace.png") );
            OnTrack = ImageIO.read( new File("res/Zelda/tiles/M3Doubled.png") );
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

			p1 = new ImageObject(p1originalX, p1originalY, p1width, p1height, 900);

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

				g2D.drawImage(OffTrack, XOFFSET, YOFFSET, null);
				g2D.drawImage(OnTrack, XOFFSET, YOFFSET, null);

				// dont draw player objects if they are "dead" (for 3 seconds)
				if (!p1dead) {
					g2D.drawImage(rotateImageObject(p1).filter(player1, null), (int)(p1.getX() + 0.5),
					(int)(p1.getY() + 0.5), null);
				}

				g2D.dispose();
			}
		}

		public void startAnimation() { timer.start(); }

		public void stopAnimation() { timer.stop(); }
	}

	// updating player one movement
	private static class PlayerOneMover implements Runnable {
		public PlayerOneMover() {
			velocitystep = 0.02; // aka accel
			rotatestep = 0.03;
			p1.maxvelocity = 2;
			brakingforce = 0.02;
		}

		public void run() {
			while (!endgame) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) { }

				if (upPressed == true) {
					if (p1velocity < p1.maxvelocity) {
						p1velocity = (p1velocity) + velocitystep;
					} else if (p1velocity >= p1.maxvelocity) { // ensure max vel not exceeded
						p1velocity = p1.maxvelocity;
					}
				}

				if (downPressed == true) {
					if (p1velocity < -1) { // ensure max rev speed
						p1velocity = -1;
					} else {
						p1velocity = p1velocity - brakingforce;
					}
				}

				if (leftPressed == true) {
					if (p1velocity < 0) {
						p1.rotate(-rotatestep);
					} else {
						p1.rotate(rotatestep);
					}
				}

				if (rightPressed == true) {
					if (p1velocity < 0) {
						p1.rotate(rotatestep);
					} else {
						p1.rotate(-rotatestep);
					}
				}

				// apply drag force
				if (!upPressed && !downPressed && !leftPressed && !rightPressed
					&& p1velocity != 0) {
					if ((p1velocity - 0.1) < 0) {
						p1velocity = 0;
					} else {
						p1velocity = p1velocity - 0.04; 
					}
				}

				p1.move(-p1velocity * Math.cos(p1.getAngle() - Math.PI / 2.0),
					p1velocity * Math.sin(p1.getAngle() - Math.PI / 2.0));
				p1.screenBounds(XOFFSET, WINWIDTH, YOFFSET, WINHEIGHT, p1.maxvelocity);
			}
		}
		private double velocitystep, rotatestep, brakingforce;
		private boolean isCollidingWithSky, isCollidingWithDirt;
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

		public ImageObject(double xinput, double yinput, double xwidthinput,
			double yheightinput, double angleinput) {
			x = xinput;
			y = yinput;
			xwidth = xwidthinput;
			yheight = yheightinput;
			angle = angleinput;
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

		public void screenBounds(double leftEdge, double rightEdge, double topEdge, double bottomEdge, double velocity) {
			if (x < leftEdge) { 
				moveto(leftEdge, getY());
				velocity = velocity*0.9;
			}
			if (x + getWidth() > rightEdge) { 
				moveto(rightEdge - getWidth(), getY()); 
				velocity = velocity*0.9;
			}
			if (y < topEdge) { 
				moveto(getX(), topEdge); 
				velocity = velocity*0.9;
			}
			if (y + getHeight() > bottomEdge) { 
				moveto(getX(), bottomEdge - getHeight()); 
				velocity = velocity*0.9;
			}
		}

		public void rotate(double input) {
			angle = angle + input;
			while (angle > (Math.PI*2)) { angle = angle - (Math.PI*2); }
			while (angle < 0) { angle = angle + (Math.PI*2); }
		}
	}

	// rotates ImageObject
	private static AffineTransformOp rotateImageObject(ImageObject obj) {
		AffineTransform at = AffineTransform.getRotateInstance(-obj.getAngle(),
			obj.getWidth()/2.0, obj.getHeight()/2.0);
		AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		return atop;
	}

	private static double calculateDistance(double x1, double y1, double x2, double y2) {
	    // Calculate Euclidean distance between two points
	    return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	// -------- GLOBAL VARIABLES --------
	private static Boolean endgame;
	private static Boolean GameOver = false;
	private static boolean gameActive = false;
	private static Boolean upPressed, downPressed, leftPressed, rightPressed;
	private static Boolean p1FallRecentlyPlayed = false;
	private static Boolean p1dead = false;
	private static Boolean SOUNDS_ENABLED = true; // ENABLE OR DISABLE FOR SOUND

	private static JButton startButton, quitButton;

	private static Color CELESTIAL = new Color(49, 151, 199);
	private static Color HIGHLIGHT = new Color(110, 168, 195);
	private static Color URANIAN = new Color(164, 210, 232);

	private static int XOFFSET, YOFFSET, WINWIDTH, WINHEIGHT;

	private static ImageObject p1;
	private static double p1width, p1height, p1originalX, p1originalY, p1velocity;

	private static JFrame appFrame;
	private static GamePanel gamePanel;

	private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;

	private static BufferedImage OffTrack, OnTrack;
	private static BufferedImage player1;

	private static Thread t1;
}