package project;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import edu.princeton.cs.introcs.StdDraw;
import support.cse131.ArgsProcessor;

public class Main {
	
	private static double[][] bubbles = new double[30][4];
	private static Color[] bubbleColor = new Color[30];
	private static boolean shield = false;
	private static int level = 1;
	private static int phase = 1;
	private static int lives = 3;
	private static int points = 999;
	private static double px;
	private static double py;
	private static Map<String, Integer> obstacles = new HashMap<String, Integer>();
	private static Font papyrus;
	private static Font blackPearl;
	private static int[][] map = new int[16][16];
	private static Random rand = new Random();
	private static BackgroundSong song;
	//private static List<BackgroundSong> songs = new LinkedList<BackgroundSong>();

	public static void main(String[] args)
			throws FontFormatException, IOException, UnsupportedAudioFileException, LineUnavailableException {
		StdDraw.enableDoubleBuffering();

		// Set Up Fonts
		InputStream pStream = new BufferedInputStream(new FileInputStream("fonts/Papyrus.ttf"));
		InputStream bStream = new BufferedInputStream(new FileInputStream("fonts/BlackPearl.ttf"));
		papyrus = Font.createFont(Font.TRUETYPE_FONT, pStream);
		blackPearl = Font.createFont(Font.TRUETYPE_FONT, bStream);

		// Set Scale
		StdDraw.setCanvasSize(512 + 26, 512);
		StdDraw.setXscale(-.05, 1);

		px = 0.5; // x location of the demo point
		py = 0.05; // y location of the demo point

		// Set Initial Objects
		obstacles.put("1", 2);
		setObstacleLocations();
		setTreasureLocations();
		setPowerLocation();

		// Set Song
		song = new BackgroundSong(new File("sound/Hes A Pirate.wav"));
		song.play();

		// Create bubbles
		for (int a = 0; a < bubbles.length; a++) {
			bubbles[a][0] = rand.nextDouble();
			bubbles[a][1] = rand.nextInt(10) / 20.0;
			bubbles[a][2] = ((rand.nextInt(5) + 1) * 2) / 500.0;
			bubbles[a][3] = ((rand.nextInt(10) + 1) * 2) / (20000 / 3.0);
			bubbleColor[a] = ColorUtils.transparent(ColorUtils.randomColor(), .66);
			// System.out.println("Bubble" + (a + 1) + ": x = " + bubbles[a][0] + " & v = "
			// + bubbles[a][3]);
		}

		// Stuff for Timer
		boolean startTimer = false;
		boolean slowTimer = false;
		int regTime = 0;
		int slowTime = 0;

		// And Here We Go: Here's the Game
		while (true) {
			StdDraw.clear();
			drawBackground();
			drawObjects();
			int hit = checkObstacleHit(px, py);
			if (hit == 1) {
				startTimer = true;
			} else if (hit == 2) {
				// Shield
				shield = true;
			} else if (hit == 3) {
				// Slow
				slowTimer = true;
			}
			//
			// Should we move?
			//
//			StdDraw.setPenColor(Color.BLACK);
//			for (double a = -.0625; a < 1; a += .0625) {
//				StdDraw.line(a, 0, a, 1);
//			}
//			for (double a = .0625; a < 1; a += .0625) {
//				StdDraw.line(0, a, 1, a);
//			}
			if (lives <= 0) {
				gameOver();
				regTime = 0;
				startTimer = false;
			}
			if (!startTimer) {
				if (checkFor(KeyEvent.VK_LEFT)) {
					if (px - .05 > 0)
						px -= 0.003;
				}
				if (checkFor(KeyEvent.VK_RIGHT)) {
					if (px + .05 < 1)
						px += 0.003;
				}
				if (checkFor(KeyEvent.VK_DOWN)) {
					py -= .0005;
				}

				if (slowTimer)
					py += .001;
				else
					py += .003;

				if (py + .04 >= 1) {
					regTime = 0;
					startTimer = false;
					slowTime = 0;
					slowTimer = false;
					nextLevel();
					setObstacleLocations();
					setTreasureLocations();
					setPowerLocation();
					py = 0;
				}
			} else {
				regTime++;
				if (regTime == 1) {
					if (!shield)
						lives--;
					else
						shield = false;
				}
				if (regTime >= 100) {
					startTimer = false;
					regTime = 0;
				}
			}
			if (slowTimer) {
				slowTime++;
				if (slowTime >= 150) {
					slowTimer = false;
					slowTime = 0;
				}
			}
			StdDraw.picture(px, py, "images/Pirate.png", .1, .1);
			if (shield) {
				StdDraw.setPenColor(ColorUtils.transparent(Color.CYAN, .3));
				StdDraw.filledCircle(px, py, .045);
				StdDraw.setPenColor(Color.WHITE);
				StdDraw.circle(px, py, .045);
			}
			if (StdDraw.mouseX() >= .93 && StdDraw.mouseY() >= .93 && StdDraw.isMousePressed()) {
				settings();
			}
			StdDraw.show();
			StdDraw.pause(10); // 1/100 of a second
		}
	}

	/**
	 * Check to see if a given key is pressed at the moment. This method does not
	 * wait for a key to be pressed, so if nothing is pressed, it returns false
	 * right away.
	 * 
	 * The event constants are found at:
	 * https://docs.oracle.com/javase/7/docs/api/java/awt/event/KeyEvent.html
	 * 
	 * @param key the integer code of the key
	 * @return true if that key is down, false otherwise
	 */
	private static boolean checkFor(int key) {
		if (StdDraw.isKeyPressed(key)) {
			return true;
		} else {
			return false;
		}
	}

	public static void settings()
			throws FontFormatException, IOException, UnsupportedAudioFileException, LineUnavailableException {
		StdDraw.setPenColor(ColorUtils.transparent(new Color(159, 162, 176), .7));
		StdDraw.filledRectangle(.5, .5, .4, .45);
		StdDraw.setPenColor(Color.BLACK);
		StdDraw.setFont(blackPearl.deriveFont(40f));
		StdDraw.text(.5, .88, "Paused");
		StdDraw.line(.15, .83, .85, .83);
		StdDraw.setFont(papyrus.deriveFont(30f));
		for (int a = 0; a < 5; a++) {
			StdDraw.setPenColor(ColorUtils.transparent(new Color(0, 105, 148), .05));
			for (int b = 0; b < 20; b++) {
				StdDraw.filledRectangle(.5, .765 - (a * .1), .3 - (b * .005), .045);
			}
		}
		StdDraw.setPenColor(Color.BLACK);
		StdDraw.text(.5, .78, "Resume");
		StdDraw.text(.5, .68, "New Game");
		StdDraw.text(.5, .58, "Instructions");
		StdDraw.text(.5, .48, "Soundtrack");
		StdDraw.text(.5, .38, "Quit");
		StdDraw.picture(.225, .175, "images/TreasureClear.png", .25, .25);
		StdDraw.show();
		while (true) {
			if (StdDraw.isMousePressed()) {
				double x = StdDraw.mouseX();
				double y = StdDraw.mouseY();
				if (y > .765 - .045 && y < .765 + .045 && x > .2 && x < .8) {
					// System.out.println("Resume");
					break;
				} else if (y > .765 - .045 - .1 && y < .765 + .045 - .1 && x > .2 && x < .8) {
					// System.out.println("New Game");
					newGame();
					break;
				} else if (y > .765 - .045 - .2 && y < .765 + .045 - .2 && x > .2 && x < .8) {
					// System.out.println("Instructions");
					instructions();
					break;
				} else if (y > .765 - .045 - .3 && y < .765 + .045 - .3 && x > .2 && x < .8) {
					// System.out.println("Soundtrack");
					song.stop();
					File newSong = ArgsProcessor.chooseFile("sound");
					//System.out.println("Picked");
					song = new BackgroundSong(newSong);
					//System.out.println("Playing?");
					song.play();
					//System.out.println("Played");
				} else if (y > .765 - .045 - .4 && y < .765 + .045 - .4 && x > .2 && x < .8) {
					// System.out.println("Quit");
					System.exit(0);
				}
			}
			//System.out.println("Well...?");
		}
		//System.out.println("And done");
	}

	public static void drawBackground() {
		// Picture and Bubbles
		StdDraw.picture(0.5, 0.5, "images/OceanMap.jpeg", 1.25, 1.25);
		for (int a = 0; a < bubbles.length; a++) {
			StdDraw.setPenColor(bubbleColor[a]);
			StdDraw.filledCircle(bubbles[a][0], bubbles[a][1], bubbles[a][2]);
		}
		advanceBubbles();
		// Settings Cog
		StdDraw.picture(.96, .96, "images/Cog.png", .07, .07);
		// Level Indicator
		StdDraw.setPenColor(ColorUtils.transparent(new Color(0, 105, 148), .7));
		StdDraw.filledRectangle(0, .95, .04, .04);
		StdDraw.setPenColor(new Color(163, 35, 35));
		StdDraw.setFont(papyrus.deriveFont(40f));
		StdDraw.text(0, .97, level + "");
		// Lives Indicator
		StdDraw.setPenColor(ColorUtils.transparent(new Color(0, 105, 148), .7));
		StdDraw.filledRectangle(0, .1, .04, .09);
		for (int a = 0; a < lives; a++) {
			StdDraw.picture(0, .05 * (a + 1), "images/HeartFull.png", .05, .05);
		}
		for (int a = 3; a > lives; a--) {
			StdDraw.picture(0.0007, .05 * a, "images/HeartEmpty.png", .041, .04);
		}
		// Points Indicator
		StdDraw.setPenColor(ColorUtils.transparent(new Color(0, 105, 148), .7));
		StdDraw.filledRectangle(0, .55, .04, .2);
		StdDraw.picture(0, .71, "images/JewelsIconClear.png", .0825, .0625);
		StdDraw.setPenColor(new Color(146, 137, 13));
		StdDraw.text(0, .66, formatPoints().substring(0, 1));
		StdDraw.text(0, .60, formatPoints().substring(1, 2));
		StdDraw.text(0, .54, formatPoints().substring(2, 3));
		StdDraw.text(0, .48, formatPoints().substring(3));
	}

	public static void advanceBubbles() {
		for (int a = 0; a < bubbles.length; a++) {
			bubbles[a][1] += bubbles[a][3];
			if (bubbles[a][2] + bubbles[a][1] >= 1) {
				StdDraw.setPenColor(bubbleColor[a]);
				StdDraw.filledCircle(bubbles[a][0], bubbles[a][1] - 1 - bubbles[a][2], bubbles[a][2]);
			}
			if (bubbles[a][1] >= 1 + 2 * bubbles[a][2]) {
				bubbles[a][1] = bubbles[a][2];
			}
		}
	}

	public static void instructions() {
		// System.out.println("Instructions");
		StdDraw.clear();
		StdDraw.picture(0.5, 0.5, "images/OceanMap.jpeg", 1.25, 1.25);
		// Level Indicator
		StdDraw.setPenColor(ColorUtils.transparent(new Color(0, 105, 148), .7));
		StdDraw.filledRectangle(0, .95, .04, .04);
		StdDraw.setPenColor(new Color(163, 35, 35));
		StdDraw.setFont(papyrus.deriveFont(40f));
		StdDraw.text(0, .97, level + "");
		// Lives Indicator
		StdDraw.setPenColor(ColorUtils.transparent(new Color(0, 105, 148), .7));
		StdDraw.filledRectangle(0, .1, .04, .09);
		for (int a = 0; a < lives; a++) {
			StdDraw.picture(0, .05 * (a + 1), "images/HeartFull.png", .05, .05);
		}
		for (int a = 3; a > lives; a--) {
			StdDraw.picture(0.0007, .05 * a, "images/HeartEmpty.png", .041, .04);
		}
		// Points Indicator
		StdDraw.setPenColor(ColorUtils.transparent(new Color(0, 105, 148), .7));
		StdDraw.filledRectangle(0, .55, .04, .2);
		StdDraw.picture(0, .71, "images/JewelsIconClear.png", .0825, .0625);
		StdDraw.setPenColor(new Color(146, 137, 13));
		StdDraw.text(0, .66, formatPoints().substring(0, 1));
		StdDraw.text(0, .60, formatPoints().substring(1, 2));
		StdDraw.text(0, .54, formatPoints().substring(2, 3));
		StdDraw.text(0, .48, formatPoints().substring(3));
		// Title
		StdDraw.setPenColor(ColorUtils.transparent(new Color(78, 195, 201), .65));
		StdDraw.filledRectangle(.5, .5, .4, .47);
		StdDraw.setPenColor(Color.BLACK);
		StdDraw.setFont(blackPearl.deriveFont(40f));
		StdDraw.text(.5, .91, "Pirates!");
		StdDraw.line(.15, .865, .85, .865);
		// How To Play!
		StdDraw.setFont(papyrus.deriveFont(20f));
		StdDraw.text(.5, .85, "Ahoy, and welcome to Pirates!");
		StdDraw.text(.5, .8, "Ye 'ave just purloined a few large coffers");
		StdDraw.text(.5, .745, "full o' booty and hafta get it to your island stash.");
		StdDraw.text(.5, .635, "But avast! Ye eye some stuff in the water!");
		StdDraw.text(.5, .58, "Avoid things that could hurt yer ship, and");
		StdDraw.text(.5, .525, "pick up treasure along the way.");
		StdDraw.text(.5, .47, "Every obstacle ye hit will remove a life...");
		StdDraw.text(.5, .415, "And ye'll drop some treasure!");
		StdDraw.text(.5, .36, "Get as far as ye can with three lives,");
		StdDraw.text(.5, .305, "(shown in the bottom left corner)");
		StdDraw.text(.5, .25, "And get as much treasure as ye can!");
		StdDraw.text(.5, .195, "(shown on the middle left side)");
		StdDraw.setFont(papyrus.deriveFont(15f));
		StdDraw.text(.5, .09, "[click to get back to game]");
		StdDraw.show();
		while (StdDraw.isMousePressed()) {
			StdDraw.pause(100);
		}
		while (!StdDraw.isMousePressed()) {
			StdDraw.pause(100);
		}
		while (StdDraw.isMousePressed()) {
			StdDraw.pause(100);
		}
	}

	public static void nextLevel() {
		px = .5;
		py = .05;
		StdDraw.setPenColor(ColorUtils.transparent(new Color(78, 195, 201), .65));
		StdDraw.filledRectangle(.5, .54, .45, .12);
		StdDraw.setFont(papyrus.deriveFont(40f));
		StdDraw.setPenColor(Color.BLACK);
		StdDraw.text(.5, .6, "Aye Matey! Next Level?");
		StdDraw.picture(.37, .48, "images/Quit.png", .14, .14);
		StdDraw.picture(.6, .48, "images/Continue.png", .125, .125);
		StdDraw.setFont(papyrus.deriveFont(20f));
		StdDraw.text(.19, .5, "Quit Game");
		StdDraw.text(.78, .5, "Continue");
		StdDraw.show();
		while (StdDraw.isMousePressed()) {
			StdDraw.pause(100);
		}
		while (!StdDraw.isMousePressed()) {
			StdDraw.pause(100);
		}
		double x = StdDraw.mouseX();
		double y = StdDraw.mouseY();
		if (x > .32 && x < .42 && y < .53 && y > .42) {
			System.exit(0);
		} else if (x > .55 && x < .65 && y < .53 && y > .42) {
			level++;
			if (level % 5 == 1) {
				phase++;
				obstacles.put(phase + "", 5);
				for (String key : obstacles.keySet()) {
					obstacles.replace(key, obstacles.get(key) - 3);
				}
			} else {
				for (String key : obstacles.keySet()) {
					obstacles.replace(key, obstacles.get(key) + 1);
				}
			}
			for (int a = 0; a < map.length; a++) {
				for (int b = 0; b < map[0].length; b++) {
					map[a][b] = 0;
				}
			}
		}
	}

	public static void setObstacleLocations() {
		for (int a = 0; a < obstacles.size(); a++) {
			int obs = obstacles.get((a + 1) + "");
			for (int b = 0; b < obs; b++) {
				int y = rand.nextInt(16);
				int x = rand.nextInt(16);
				while (map[x][y] == 1 || y == 0 || y == 1 || (x == 15 && y == 15) || x == 0) {
					y = rand.nextInt(16);
					x = rand.nextInt(16);
				}
				map[y][x] = (a + 1);
			}
		}
	}

	public static void drawObjects() {
		for (int a = 0; a < map.length; a++) {
			for (int b = 0; b < map[0].length; b++) {
				double x = (b + 1) / 16.0 - 1.0 / 32;
				double y = (a + 1) / 16.0 - 1.0 / 32;
				if (map[a][b] == 1) {
//					StdDraw.setPenColor(ColorUtils.transparent(new Color(1, 157, 160), .3));
//					StdDraw.filledRectangle(x, y, 1.0 / 32, 1.0 / 32);
					StdDraw.picture(x, y, "images/BarrelClear.png", .0525, .0625);
				} else if (map[a][b] == 2) {
//					StdDraw.setPenColor(ColorUtils.transparent(new Color(1, 157, 160), .7));
//					StdDraw.filledRectangle(x, y, 1.0 / 32, 1.0 / 32);
					StdDraw.picture(x, y, "images/CrateClear.png", .0625, .0625);
				} else if (map[a][b] == 3) {
//					StdDraw.setPenColor(ColorUtils.transparent(new Color(1, 157, 160), .7));
//					StdDraw.filledRectangle(x, y, 1.0 / 32, 1.0 / 32);
					StdDraw.picture(x, y, "images/Wheel.png", .0825, .0625);
				} else if (map[a][b] == 4) {
//					StdDraw.setPenColor(ColorUtils.transparent(new Color(1, 157, 160), .7));
//					StdDraw.filledRectangle(x, y, 1.0 / 32, 1.0 / 32);
					StdDraw.picture(x, y, "images/Debris.png", .0625, .0625);
				} else if (map[a][b] == 5) {
//					StdDraw.setPenColor(ColorUtils.transparent(new Color(1, 157, 160), .7));
//					StdDraw.filledRectangle(x, y, 1.0 / 32, 1.0 / 32);
					StdDraw.picture(x, y, "images/Rock.png", .0625, .0625);
				} else if (map[a][b] == -3) {
//					StdDraw.setPenColor(ColorUtils.transparent(new Color(1, 157, 160), .7));
//					StdDraw.filledRectangle(x, y, 1.0 / 32, 1.0 / 32);
					StdDraw.picture(x, y, "images/RedJewel.png", .0625, .0625);
				} else if (map[a][b] == -2) {
//					StdDraw.setPenColor(ColorUtils.transparent(new Color(1, 157, 160), .7));
//					StdDraw.filledRectangle(x, y, 1.0 / 32, 1.0 / 32);
					StdDraw.picture(x, y, "images/BlueJewel.png", .0625, .0625);
				} else if (map[a][b] == -1) {
//					StdDraw.setPenColor(ColorUtils.transparent(new Color(1, 157, 160), .7));
//					StdDraw.filledRectangle(x, y, 1.0 / 32, 1.0 / 32);
					StdDraw.picture(x, y, "images/OrangeJewel.png", .0925, .0625);
				} else if (map[a][b] == 100) {
//					StdDraw.setPenColor(ColorUtils.transparent(new Color(1, 157, 160), .7));
//					StdDraw.filledRectangle(x, y, 1.0 / 32, 1.0 / 32);
					StdDraw.picture(x, y, "images/HP.png", .3, .175);
				} else if (map[a][b] == 99) {
//					StdDraw.setPenColor(ColorUtils.transparent(new Color(1, 157, 160), .7));
//					StdDraw.filledRectangle(x, y, 1.0 / 32, 1.0 / 32);
					StdDraw.picture(x, y, "images/Shield.png", .3, .175);
				} else if (map[a][b] == 98) {
//					StdDraw.setPenColor(ColorUtils.transparent(new Color(1, 157, 160), .7));
//					StdDraw.filledRectangle(x, y, 1.0 / 32, 1.0 / 32);
					StdDraw.picture(x, y, "images/Slow.png", .3, .175);
				}
			}
		}
	}

	public static int checkObstacleHit(double x, double y) {
		int hitDirection = 0;
		boolean hit = false;
		for (int a = 0; a < map.length && !hit; a++) {
			for (int b = 0; b < map[0].length && !hit; b++) {
				if (map[a][b] != 0) {
					double ox = (b + 1) / 16.0 - 1.0 / 32;
					double oy = (a + 1) / 16.0 - 1.0 / 32;
					double[] bounds = getBounds(ox, oy);
					double up = bounds[0];
					double down = bounds[1];
					double left = bounds[2];
					double right = bounds[3];
					if (y + 5.0 / 128 > down && y - 5.0 / 128 < up) {
						if (x + 5.0 / 128 > left && x - 5.0 / 128 < right) {
							hit = true;
						}
					}
					if (hit) {
						if (map[a][b] < 0) {
							hitDirection = -1;
							if (map[a][b] == -3)
								points += 100;
							if (map[a][b] == -2)
								points += 50;
							if (map[a][b] == -1)
								points += 30;
						}
						if (map[a][b] > 0 && map[a][b] <= 4) {
							if (map[a][b] == 1)
								points -= 10;
							else if (map[a][b] == 2)
								points -= 25;
							else if (map[a][b] == 3)
								points -= 40;
							else if (map[a][b] == 4)
								points -= 50;
							hitDirection = 1;
						}
						if (map[a][b] <= 100 && map[a][b] >= 98) {
							if (map[a][b] == 100) {
								if (lives < 3)
									lives++;
							} else if (map[a][b] == 99) {
								hitDirection = 2;
							} else if (map[a][b] == 98) {
								hitDirection = 3;
							}
						}
						map[a][b] = 0;
					}
				}
			}
		}
		return hitDirection;
	}

	public static double[] getBounds(double x, double y) {
		double up = y + 1.0 / 32;
		double right = x + 1.0 / 32;
		double down = y - 1.0 / 32;
		double left = x - 1.0 / 32;
		return new double[] { up, down, left, right };
	}

	public static String formatPoints() {
		String sPoints = points + "";
		while (sPoints.length() < 4) {
			sPoints = "0" + sPoints;
		}
		return sPoints;
	}

	public static void setTreasureLocations() {
		for (int a = 0; a < 3; a++) {
			int value = rand.nextInt(3) - 3;
			int x = rand.nextInt(16);
			int y = rand.nextInt(16);
			while (map[y][x] != 0 || y == 1 || y == 0 || (x == 15 && y == 15) || x == 0) {
				x = rand.nextInt(16);
				y = rand.nextInt(16);
			}
			map[y][x] = value;
		}
	}

	public static void gameOver() throws FileNotFoundException {
		StdDraw.setPenColor(ColorUtils.transparent(new Color(78, 195, 201), .65));
		StdDraw.filledRectangle(.5, .54, .45, .12);
		StdDraw.setFont(papyrus.deriveFont(40f));
		StdDraw.setPenColor(Color.BLACK);
		StdDraw.text(.5, .6, "Arrrr! Ye've Sank!");
		StdDraw.picture(.37, .48, "images/Quit.png", .14, .14);
		StdDraw.picture(.6, .48, "images/Restart.png", .125, .125);
		StdDraw.setFont(papyrus.deriveFont(20f));
		StdDraw.text(.19, .5, "Quit Game");
		StdDraw.text(.78, .5, "New Game");
		StdDraw.show();
		while (StdDraw.isMousePressed()) {

		}
		while (!StdDraw.isMousePressed()) {

		}
		double x = StdDraw.mouseX();
		double y = StdDraw.mouseY();
		if (x > .32 && x < .42 && y < .53 && y > .42) {
			System.exit(0);
		} else if (x > .55 && x < .65 && y < .53 && y > .42) {
			newGame();
		}
	}

	public static void newGame(){
		px = .5;
		py = .05;
		lives = 3;
		level = 1;
		points = 999;
		obstacles.clear();
		obstacles.put("1", 2);
		for (int a = 0; a < 16; a++) {
			for (int b = 0; b < 16; b++) {
				map[a][b] = 0;
			}
		}
		setObstacleLocations();
		setTreasureLocations();
		setPowerLocation();
	}

	public static void setPowerLocation() {
		if (rand.nextDouble() > 0) {
			int type = rand.nextInt(3);
			int x = 0, y = 0;
			while (x < 5 || y == 0 || y == 15 || map[x][y] != 0) {
				x = rand.nextInt(16);
				y = rand.nextInt(16);
			}
			map[x][y] = 100 - type;
		}
	}
}
