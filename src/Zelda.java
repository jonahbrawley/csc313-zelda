import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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

		gbc.ipady = 10;
		gbc.ipadx = 50;

		    // Add a rigid area to create space between the image and buttons
		    gamePanel.add(Box.createRigidArea(new Dimension(0, 0)), gbc);

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
		
		if (SOUNDS_ENABLED) {
			overworldtheme.play();
		}
	}

	public static void setup() {
		XOFFSET = 0;
		YOFFSET = 0;
		WINWIDTH = 320; // tile width 160 but x2
        WINHEIGHT = 255; // tile height 128 but x2
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

            Map = ImageIO.read( new File("res/Zelda/tiles/M3Doubled.png") );
			n3 = ImageIO.read(new File("res/Zelda/tiles/N3Doubled.png")); // bottom left
			n4 = ImageIO.read(new File("res/Zelda/tiles/N4Doubled.png")); // bottom middle
			n5 = ImageIO.read(new File("res/Zelda/tiles/N5Doubled.png")); // bottom middle
			m3 = ImageIO.read(new File("res/Zelda/tiles/M3Doubled.png")); // top left
			m4 = ImageIO.read(new File("res/Zelda/tiles/M4Doubled.png")); // top right

			d5 = ImageIO.read(new File("res/Zelda/tiles/FaceShrineT5.png")); // dungeon 1
            d4 = ImageIO.read(new File("res/Zelda/tiles/FaceShrineT4.png")); // dungeon 2
            d3 = ImageIO.read(new File("res/Zelda/tiles/FaceShrineT3.png")); // dungeon 3
            d2 = ImageIO.read(new File("res/Zelda/tiles/FaceShrineT2.png")); // dungeon 4
            d1 = ImageIO.read(new File("res/Zelda/tiles/FaceShrineT1.png")); // dungeon 5


			heart1 = ImageIO.read(new File("res/Zelda/healthbar/healthheart.png"));
			heart2 = ImageIO.read(new File("res/Zelda/healthbar/healthheart.png"));
			heart3 = ImageIO.read(new File("res/Zelda/healthbar/healthheart.png"));

			// screen M3
			BufferedImage M3MapKey = ImageIO.read(new File("res/Zelda/tiles/M3MapKey.png"));
			regionM3 = loadRegion(M3MapKey, regionRED);

			// screen M4
			BufferedImage M4MapKey = ImageIO.read(new File("res/Zelda/tiles/M4MapKey.png"));
			regionM4 = loadRegion(M4MapKey, regionRED);

			// screen N3
			BufferedImage N3MapKey = ImageIO.read(new File("res/Zelda/tiles/N3MapKey.png"));
			regionN3 = loadRegion(N3MapKey, regionRED);

			// screen N4
			BufferedImage N4MapKey = ImageIO.read(new File("res/Zelda/tiles/N4MapKey.png"));
			regionDungeonDoor = loadRegion(N4MapKey, regionBLUE);
			regionN4 = loadRegion(N4MapKey, regionRED);

			// screen N5
			BufferedImage N5MapKey = ImageIO.read(new File("res/Zelda/tiles/N5MapKey.png"));
			regionN5 = loadRegion(N5MapKey, regionRED);

			// D1
			BufferedImage D1MapKey = ImageIO.read(new File("res/Zelda/tiles/N5MapKey.png"));
			regionD1 = loadRegion(D1MapKey, regionRED);

			// D2
			BufferedImage D2MapKey = ImageIO.read(new File("res/Zelda/tiles/N5MapKey.png"));
			regionD2 = loadRegion(D2MapKey, regionRED);

			// D3 
			BufferedImage D3MapKey = ImageIO.read(new File("res/Zelda/tiles/N5MapKey.png"));
			regionD3 = loadRegion(D3MapKey, regionRED);
			
			// D4
			BufferedImage D4MapKey = ImageIO.read(new File("res/Zelda/tiles/N5MapKey.png"));
			regionD4 = loadRegion(D4MapKey, regionRED);
			
			// D5
			BufferedImage D5MapKey = ImageIO.read(new File("res/Zelda/tiles/N5MapKey.png"));
			regionD5 = loadRegion(D5MapKey, regionRED);

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

				//System.out.println(p1.getX()+ " " + p1.getY());

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
			speed = 1.0;
			validloc = new Point2D.Double(p1.getX(), p1.getY());
		}

		public void run() {
			while (!endgame) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) { }

				// add players points
				Point2D.Double point1 = new Point2D.Double(p1.getX(), p1.getY()+30.0);
				Point2D.Double point2 = new Point2D.Double(p1.getX()+30.0, p1.getY()+30.0);

				updateVloc(p1.currentSegment, point1, point2);
				System.out.println(p1.getX() + " " + p1.getY());

				if (upPressed == true) {
					p1.move(0.0, -speed);
				}

				if (downPressed == true) {
					p1.move(0.0, speed);
				}

				if (leftPressed == true) {
					p1.move(-speed, 0.0);
				}

				if (rightPressed == true) {
					p1.move(speed, 0.0);
				}

				try {
					p1.enemyHitBoxes();

					// boundary checks below
					if (p1.currentSegment == 1) { // M3
						if ( (regionM3.contains(point1) || regionM3.contains(point2)) ) {
							p1.moveto( validloc.x, validloc.y );
						}
					}

					if (p1.currentSegment == 2) { // M4
						if ( (regionM4.contains(point1) || regionM4.contains(point2)) ) {
							p1.moveto( validloc.x, validloc.y );
						}
					}

					if (p1.currentSegment == 3) { // N4
						// DUNGEON DOOR CHECK
						if ( (regionDungeonDoor.contains(point1) || regionDungeonDoor.contains(point2)) ) {
							System.out.println("Player entered dungeon!");
							p1.moveto(145.0, 230.0);
							p1.currentSegment = 6;
							
						}

						// N4 hard boundaries check
						if ( (regionN4.contains(point1) || regionN4.contains(point2)) ) {
							p1.moveto( validloc.x, validloc.y );
						}
					}

					if (p1.currentSegment == 4) { // N3
						if ( (regionN3.contains(point1) || regionN3.contains(point2)) ) {
							p1.moveto( validloc.x, validloc.y );
						}
					}

					// if (p1.currentSegment == 5) {
					// 	if ( (regionN5.contains(point1) || regionN5.contains(point2)) ) {
					// 		p1.moveto( validloc.x, validloc.y );
					// 	}					
					// }

					// if (p1.currentSegment == 6) {
					// 	if ( (regionD5.contains(point1) || regionD5.contains(point2)) ) {
					// 		p1.moveto( validloc.x, validloc.y );
					// 	}		
					// }
					// if (p1.currentSegment == 7) {
					// 	if ( (regionD4.contains(point1) || regionD4.contains(point2)) ) {
					// 		p1.moveto( validloc.x, validloc.y );
					// 	}		
					// }
					// if (p1.currentSegment == 8) {
					// 	if ( (regionD3.contains(point1) || regionD3.contains(point2)) ) {
					// 		p1.moveto( validloc.x, validloc.y );
					// 	}		
					// }
					// if (p1.currentSegment == 9) {
					// 	if ( (regionD2.contains(point1) || regionD2.contains(point2)) ) {
					// 		p1.moveto( validloc.x, validloc.y );
					// 	}		
					// }
					// if (p1.currentSegment == 10) {
					// 	if ( (regionD1.contains(point1) || regionD1.contains(point2)) ) {
					// 		p1.moveto( validloc.x, validloc.y );
					// 	}		
					// }
					
					p1.screenBounds(0.0, 320.0, 0.0, 256.0);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		private Double speed;
			
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
		private Thread t = new Thread(this);

		public BackgroundSound(String file, Boolean isLoop) {
			this.file = file;
			this.loopAudio = isLoop;
		}

		public void play() {
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
		private Double x, y, xwidth, yheight;
		public int currentSegment; 
		public ImageObject(Double xinput, Double yinput, Double xwidthinput,
			Double yheightinput) {
			x = xinput;
			y = yinput;
			xwidth = xwidthinput;
			yheight = yheightinput;
			currentSegment = 1; // currSegment == what map tile you are on
		}

		public Double getX() { return x; }

		public Double getY() { return y; }

		public Double getWidth() { return xwidth; }

		public Double getHeight() { return yheight; }

		//public double getAngle() { return angle; }

		public void move(Double xinput, Double yinput) {
			x = x + xinput; 
			y = y + yinput;
		}

		public void moveto(Double xinput, Double yinput) {
			x = xinput; 
			y = yinput;
		}

		public void enemyHitBoxes() throws IOException {
			if (currentSegment == 1  && p1.getX() > 73 && p1.getX() < 117 && p1.getY() < 49 && p1.getY() > 6) {
				if (isHittingEnemy == false && heart3alreadyDied == false) {
					System.out.println("got hit OUCH");
					isHittingEnemy = true;
					heart3alreadyDied = true;
					heart3 = ImageIO.read(new File("res/Zelda/healthbar/blankheart.png"));
				}
			}
		}

		public void screenBounds(double leftEdge, double rightEdge, double topEdge, double bottomEdge) throws IOException {
			System.out.println("Current segment: " + currentSegment);

			// Ensure the player stays within the screen boundaries
			if (x < leftEdge) {
				x = leftEdge;
			} else if (x + getWidth() > rightEdge) {
				x = rightEdge - getWidth();
			}

			if (y < topEdge) {
				y = topEdge;
			} else if (y + getHeight() > bottomEdge) {
				y = bottomEdge - getHeight();
			}

			if (currentSegment == 1) { // in top left overworld
				Map = m3;
				if (x > rightEdge - 31) {
					moveto((leftEdge+5), getY());
					System.out.println("Link is touching right");
					currentSegment = 2;
				}
				if ( y + getHeight() > bottomEdge - 21){
					moveto(getX(), topEdge + 2);
					System.out.println("Link is touching bottom");
					currentSegment = 4;
				}
				
			}
			if (currentSegment == 2) { // in top right overworld
				Map = m4;
				if (x < leftEdge + 1){
					moveto(rightEdge - 32, getY());
					System.out.println("Move to M3");
					currentSegment = 1;
				} 
				
			}
			if (currentSegment == 3) { // in bottom middle overworld
				Map = n4;
				if (x < leftEdge + 1){
					moveto(rightEdge - 32, getY());
					System.out.println("Move to N3");
					currentSegment = 4;
				}
				if (x > 288.0){
					moveto(leftEdge + 5, getY());
					System.out.println("Move to N5");
					currentSegment = 5;
				}
			}

			if (currentSegment == 4) { // in bottom left overworld
				Map = n3;
				if (x > rightEdge - 31) {
					moveto((leftEdge + 5), 189.0);
					System.out.println("Link is touching right");
					currentSegment = 3;
				}

				if (y < topEdge + 1){
					moveto(getX(), (bottomEdge - 30) - getHeight());
					System.out.println("Move to M3");
					currentSegment = 1;
				}
			}

			if (currentSegment == 5) { // in bottom right overworld
				Map = n5;
				if (x < 5.0 && y > 170.0) {
					moveto(285.0, 189.0);
					currentSegment = 3;
					Map = n4;
				}
			}

			if (currentSegment == 6){ // first dungeon d5
				Map = d5;
				if (y < 10.0){
					moveto(146.0, 230.0);
					currentSegment = 7;
				}
			}

			if (currentSegment == 7){ // second dungeon d4
				Map = d4;
				if (y < 6.0){
					moveto(127.0, 220.0);
					currentSegment = 8;
				}
				if (y > 235.0){
					moveto(146.0, 10.0);
					currentSegment = 6;
				}
			}
			if (currentSegment == 8){ // third dungeon d3
				Map = d3;
				if (y < 6.0){
					moveto(x, 230.0);
					currentSegment = 9;
				}
				if (y > 235.0){
					moveto(146.0, 10.0);
					currentSegment = 7;
				}
			}

			if (currentSegment == 9){ // fourth dungeon d2
				Map = d2;
				if (y < 6.0){
					moveto(146.0, 230.0);
					currentSegment = 10;
				}
				if (y > 235.0){
					moveto(146.0, 10.0);
					currentSegment = 8;
				}
			}

			if (currentSegment == 10){
				Map = d1;
				if (y > 235.0){
					moveto(146.0, 10.0);
					currentSegment = 9;
				}
				if (y < 6.0){
					moveto(190.0, 71.0);
					currentSegment = 3;
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

	private static ArrayList<Point2D.Double> loadRegion(BufferedImage mapkey, Color regionCOLOR) {
		ArrayList<Point2D.Double> region = new ArrayList<>();
		int rgnCol = regionCOLOR.getRGB();

		for (int x = 0; x < WINWIDTH; x++) {
            for (int y = 0; y < WINHEIGHT; y++) {
                if (mapkey.getRGB(x, y) == rgnCol) {
					region.add(new Point2D.Double(x, y));
				}
            }
        }

		return region;
	}

	private static void updateVloc(int currSegment, Point2D.Double point1, Point2D.Double point2) {
		if (currSegment == 1) {
			if (!regionM3.contains(point1) && !regionM3.contains(point2)) {
				validloc = new Point2D.Double(p1.getX(), p1.getY());
			}
		}
		if (currSegment == 2) {
			if (!regionM4.contains(point1) && !regionM4.contains(point2)) {
				validloc = new Point2D.Double(p1.getX(), p1.getY());
			}
		}
		if (currSegment == 3) {
			if (!regionN4.contains(point1) && !regionN4.contains(point2)) {
				validloc = new Point2D.Double(p1.getX(), p1.getY());
			}
		}
		if (currSegment == 4) {
			if (!regionN3.contains(point1) && !regionN3.contains(point2)) {
				validloc = new Point2D.Double(p1.getX(), p1.getY());
			}
		}
		if (currSegment == 5) {
			if (!regionN5.contains(point1) && !regionN5.contains(point2)) {
				validloc = new Point2D.Double(p1.getX(), p1.getY());
			}
		}
		if (currSegment == 6) {
			if (!regionD1.contains(point1) && !regionD1.contains(point2)) {
				validloc = new Point2D.Double(p1.getX(), p1.getY());
			}
		}
		if (currSegment == 7) {
			if (!regionD2.contains(point1) && !regionD2.contains(point2)) {
				validloc = new Point2D.Double(p1.getX(), p1.getY());
			}
		}
		if (currSegment == 8) {
			if (!regionD3.contains(point1) && !regionD3.contains(point2)) {
				validloc = new Point2D.Double(p1.getX(), p1.getY());
			}
		}
		if (currSegment == 9) {
			if (!regionD4.contains(point1) && !regionD4.contains(point2)) {
				validloc = new Point2D.Double(p1.getX(), p1.getY());
			}
		}
		if (currSegment == 10) {
			if (!regionD5.contains(point1) && !regionD5.contains(point2)) {
				validloc = new Point2D.Double(p1.getX(), p1.getY());
			}
		}
	}

	// -------- GLOBAL VARIABLES --------
	private static Boolean endgame;
	private static Boolean GameOver = false;
	private static boolean gameActive = false;
	private static Boolean upPressed, downPressed, leftPressed, rightPressed;
	private static Boolean p1dead = false;

	private static Boolean heart3alreadyDied = false;
	private static Boolean isHittingEnemy = false;

	private static Boolean SOUNDS_ENABLED = false; // ENABLE OR DISABLE FOR SOUND
	private static BackgroundSound overworldtheme = new BackgroundSound("res/overworld.wav", true);

	private static JButton startButton, quitButton;

	private static Color CELESTIAL = new Color(89, 181, 96);
	private static Color HIGHLIGHT = new Color(79, 102, 80);
	private static Color URANIAN = new Color(167, 217, 171);

	private static Color regionBLUE = new Color(0, 30, 255); // dungeon door
	private static Color regionRED = new Color(255, 0, 0); // where link cannot move
	private static Color regionGREEN = new Color(24, 255, 0); // enemies!

	private static int XOFFSET, YOFFSET, WINWIDTH, WINHEIGHT;

	private static ImageObject p1;
	private static double p1width, p1height, p1originalX, p1originalY;

	private static JFrame appFrame;
	private static GamePanel gamePanel;

	private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;

	private static BufferedImage Map, n3, n4, n5, m3, m4, d1, d2, d3, d4, d5;
	private static BufferedImage walk_left1, walk_left2, walk_right1, walk_right2, walk_down1, walk_down2, walk_up1, walk_up2;
	private static BufferedImage heart1, heart2, heart3;

	private static ArrayList<Point2D.Double> regionM3, regionM4;
	private static ArrayList<Point2D.Double> regionN3, regionN4, regionN5;
	private static ArrayList<Point2D.Double> regionD1, regionD2, regionD3, regionD4, regionD5;
	private static ArrayList<Point2D.Double> regionDungeonDoor;

	private static double anim_counter = 1;
	private static Point2D.Double validloc = new Point2D.Double();

	private static Thread t1;
	}
