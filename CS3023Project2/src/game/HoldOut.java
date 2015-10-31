package game;
/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 2 - Hold Out 2: Revenge of the Slimy Menace Clones
 * 
 * Backstory:
 * In Hold Out, you play as a war hero codenamed Gas Cobra.
 * You along with an elite squadron of soldiers have been
 * handpicked to fight against the horde of radioactive circles
 * that are taking over the world.  However, along the way, you
 * and your team encountered a horde of slimes that used to be
 * circles. They quickly devour your team, leaving only you to
 * fend them off with your trusty mega beam cannon.
 * Will you survive?
 * 
 * Controls:
 * W:				Move up
 * A:				Move left
 * S:				Move down
 * D:				Move right
 * Mouse:			Click to shoot
 * P:				Pause
 * R:				Restart
 * Ctl+C, Q, Esc:	Quit game
 */

import image.*;
import sound.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.*;

public class HoldOut extends GameFrame
{
	private static int DEFAULT_FPS = 100;
	private final long ENEMY_ADD_INTERVAL = 3000;
	public final int INVULN_TIME = 500;
	private final int DAMAGE_RADIUS = 35;
	private final int PICKUP_RADIUS = 35;
	
	private Player player;
	private ArrayList <GunShot> shots;
  	private Enemies enemies;
  	private Pickups pickups;
  	
  	public Point2D mouseLoc;
  	
  	public Random r = new Random ();
  	
  	private int highScore = 0;
  	
  	private long lastEnemySpawnTime;
  	private long lastDamagedTime;
  	
  	private boolean restart = false;
  	
  	private boolean firstBloodPlayed = false;
  	private boolean lookAtHimGoPlayed = false;
  	
  	private Font font;
  	private FontMetrics metrics;
  	private DecimalFormat df = new DecimalFormat ("0.##");
  	
  	private Rectangle2D playArea;
  	
  	private ImagesLoader imLoad;
  	private ImageSFXs imSFX;
  	private BufferedImage background;
  	private ClipsLoader clipsLoad;
  	
  	// Pause, restart, and quit buttons
  	private Rectangle quitArea = new Rectangle (frameWidth - 100, 0, 100, 30);
  	private Rectangle pauseArea = new Rectangle (frameWidth - 220, 0, 120, 30);
  	private Rectangle restartArea = new Rectangle (frameWidth - 350, 0, 130, 30);
  	private volatile boolean isOverQuitButton = false;
  	private volatile boolean isOverPauseButton = false;
  	private volatile boolean isOverRestartButton = false;
  
  	public HoldOut (long period)
  	{
  		super (period);
  		
  		addKeyListener (new KeyAdapter ()
  		{
  			public void keyPressed (KeyEvent e)
  			{
  				HoldOut.this.keyPress (e.getKeyCode ());
  			} // end keyPressed
  			
  			public void keyReleased (KeyEvent e)
  			{
  				HoldOut.this.keyRelease (e.getKeyCode ());
  			} // end keyReleased
  		}); // end anonymous inner class
  	} // end constructor
  	
  	protected void simpleInitialize ()
  	{
  		if (!restart)
  		{
  	  		// Initialize image components
  			imLoad = new ImagesLoader ("imsInfo.txt");
  			imSFX = new ImageSFXs ();
  			background = imLoad.getImage ("background");
  			
  			// Initialize sound components
  			clipsLoad = new ClipsLoader ("clipsInfo.txt");
  			
  			// Start the music
  			new Thread ()
  			{
  				public void run ()
  				{
  					String [] s = {"heartOfCourage.wav"};
  					
  					// Loop the music
  					while (true)
  						BufferedPlayer.main (s);
  				} // end run
  			}.start (); // end anonymous inner class
  		} // end if
  		
  		playArea = new Rectangle2D.Double (0, 30, frameWidth, frameHeight - 30);
  		player = new Player (playArea, imLoad);
  		enemies = new Enemies (frameWidth, frameHeight, imLoad);
  		pickups = new Pickups (frameWidth, frameHeight, imLoad);
  		
  		shots = new ArrayList <GunShot> ();
  		
  	  	firstBloodPlayed = false;
  	  	lookAtHimGoPlayed = false;
  		
  		mouseLoc = new Point2D.Double (frameWidth / 2, frameHeight / 2);
  		lastEnemySpawnTime = System.currentTimeMillis ();
  		
  		timeSpentInGame = 0;
  		
  		font = new Font ("SansSerif", 1, 24);
  		metrics = getFontMetrics (font);
  	} // end simpleInitialize
  	
  	protected void simpleRender (Graphics2D gScr)
  	{
  		// Draw the play area
  		gScr.drawImage (background, 0, 0, null);
  		
  		// Draw the Pickups
  		pickups.draw (gScr, imLoad);
  		
  		// Draw the Player
  		player.draw (gScr, lastDamagedTime, imSFX);
  		
  		// Draw the Enemies
  		enemies.draw (gScr, imSFX);
  		
  		// Draw the GunShots
  		for (int i = 0; i < shots.size (); i ++)
  			shots.get (i).draw (gScr, imLoad, imSFX);
  		
  		// Draw the interface area
  		drawInterface (gScr);
  	} // end simpleRender
  	
  	private void drawInterface (Graphics g)
  	{
  		// Draw top bar of interface
  		g.setColor (Color.GRAY);
  		g.fillRect (0, 0, frameWidth, (int) playArea.getY ());
  		g.setColor (Color.BLACK);
  		g.drawLine (275, 0, 275, 29);
  		g.drawLine (675, 0, 675, 29);
  		g.drawLine ((int) restartArea.getX (), 0, (int) restartArea.getX (), 29);
  		g.drawLine ((int) pauseArea.getX (), 0, (int) pauseArea.getX (), 29);
  		g.drawLine ((int) quitArea.getX (), 0, (int) quitArea.getX (), 29);
  		
  		// Draw the amount of time spent in the game
  		g.setColor (Color.BLUE);
  		font = new Font ("SansSerif", 1, 24);
  		g.setFont (font);
  		g.drawString ("Time Spent: " + timeSpentInGame + " secs", 10, 23);
  		
	    // Report the average FPS and UPS
  		g.setColor (Color.BLUE);
		g.drawString ("Average FPS/UPS: " + df.format (averageFPS) + ", " + df.format (averageUPS), 300, 23);
  		
  		// Draw the restart, pause, and quit buttons
  		drawButtons (g);
  		
  		// Draw the Player's health
  		g.setColor (Color.WHITE);
  		g.drawString ("Health = " + player.health, 20, 70);
  		
		// Draw the Player's health bar
  		g.setColor (Color.BLACK);
  		g.fillRect (19, 79, 152, 22);
  		g.setColor (Color.RED);
  		g.fillRect (20, 80, 150, 20);
		g.setColor (Color.GREEN);
		g.fillRect (20, 80, 15 * player.health, 20);
		
		// Draw the current and high scores
		g.setColor (Color.WHITE);
		g.drawString ("Current score: " + enemies.deaths, 20, 123);
		g.drawString ("High score: " + highScore, 685, 23);
  	} // end drawInterface
  	
  	protected void drawPauseScreen (Graphics2D g)
  	{
  		g.setColor (Color.WHITE);
  		font = new Font ("SansSerif", 1, 36);
  		g.setFont (font);
  		g.drawString ("WASD - Move the player", (frameWidth / 2) - 200, (frameHeight / 2) - 100);
  		g.drawString ("Left click - Shoot", (frameWidth / 2) - 135, (frameHeight / 2));
  		g.drawString ("Ctl+C, Q, Esc - Quit", (frameWidth / 2) - 155, (frameHeight / 2) + 100);
  	} // end drawPauseScreen
  	
  	protected void simpleUpdate ()
  	{
  		// Move the player
  		player.move ();
  		
  		// Move the enemies
  		Point2D tmpPoint = new Point2D.Double (player.position.getX () - 10, player.position.getY () + 40);
  		enemies.enemyUpdate (tmpPoint);
  		
  		// Update the pickups
  		pickups.updatePickups ();
  		
  		// If the player collides with a pickup, increase the player's health
  		for (int i = 0; i < pickups.pickupList.size (); i ++)
  		{
  			if (tmpPoint.distance (pickups.pickupList.get (i).position) < PICKUP_RADIUS && player.health < 10)
  			{
  				clipsLoad.play ("healthPickup", false);
  				player.health ++;
  				pickups.pickupList.get (i).setPicked (true);
  				pickups.removePickup ();
  			} // end if
  		} // end for
  		
  		// If the player collides with an enemy, decrease the player's health
  		for (int i = 0; i < enemies.enemyList.size (); i ++)
  		{
  			enemies.enemyList.get (i).move (tmpPoint);
  			
  			if (tmpPoint.distance (enemies.enemyList.get (i).position) < DAMAGE_RADIUS &&
  					System.currentTimeMillis () - lastDamagedTime > INVULN_TIME)
  			{
  				player.health --;
  				lastDamagedTime = System.currentTimeMillis ();
  				clipsLoad.play ("playerGrunt", false);
  				
  				// If the player is dead
  				if (player.health <= 0)
  				{
  					// Play an awesome sound effect when the player dies
  					gameOverAudio ();
  					
  					gameOver = true;
  					if (highScore < enemies.deaths)
  						highScore = enemies.deaths;
  				} // end if
  			} // end if
  		} // end for
  		
  		// Spawn enemies every (ENEMY_ADD_INTERVAL/1000) seconds
  		if (System.currentTimeMillis() - lastEnemySpawnTime >= ENEMY_ADD_INTERVAL)
  		{
  			enemies.addEnemy ();
  			lastEnemySpawnTime = System.currentTimeMillis ();
  		} // end if
  		
  		// If any GunShots collide with enemies, damage them and potentially drop health somewhere
  		for (int i = 0; i < shots.size (); i ++)
  		{
  			for (int j = 0; j < enemies.enemyList.size (); j ++)
  			{
  				if (shots.get (i).collidesWith (enemies.enemyList.get (j).position))
  				{
  					enemies.enemyList.get (j).damage ();
  					
  	  				int healthProb = r.nextInt (100);
  	  				
  	  				if (healthProb > 97)
  	  					pickups.addPickup ();
  	  				
  					if (enemies.deaths >= 0 && !firstBloodPlayed)
  					{
  						clipsLoad.play ("firstBlood", false);
  						firstBloodPlayed = true;
  					} // end if
  					
  					else if (enemies.deaths >= 500 && !lookAtHimGoPlayed)
  					{
  						clipsLoad.play ("lookAtHimGo", false);
  						lookAtHimGoPlayed = true;
  					} // end else if
  				} // end if
  			} // end for
  			
  			if (System.currentTimeMillis () - shots.get (i).createTime > 40)
  			{
  				shots.remove (i);
  			} // end if
  		} // end for
  	} // end simpleUpdate
  	
  	private void drawButtons (Graphics g)
  	{
  		g.setColor (Color.WHITE);
  		
  		if (isOverRestartButton)
  			g.setColor (Color.GREEN);
  		
  		g.drawString ("Restart", restartArea.x + 25, restartArea.y + 23);
    
  		g.setColor (Color.WHITE);
  		if (isOverPauseButton)
  			g.setColor (Color.GREEN);
  		
  		if (isPaused)
  			g.drawString ("Paused", pauseArea.x + 20, pauseArea.y + 23);
  		
  		else
  			g.drawString ("Pause", pauseArea.x + 25, pauseArea.y + 23);
  		
  		g.setColor (Color.WHITE);
  		
  		if (isOverQuitButton)
  			g.setColor (Color.GREEN);
  		
  		g.drawString ("Quit", quitArea.x + 25, quitArea.y + 23);
  		
  		g.setColor (Color.WHITE);
  	} // end drawButtons
  	
  	protected void gameOverAudio ()
  	{
		if (enemies.deaths < 10)
			clipsLoad.play ("disgusting", false);
		
		else if (enemies.deaths >= 9000)
			clipsLoad.play ("over9000", false);
		
		else if (enemies.deaths >= 1000)
			clipsLoad.play ("godLike", false);
		
		else if (enemies.deaths >= 500)
			clipsLoad.play ("wickedSick", false);
		
		else if (enemies.deaths >= 200)
			clipsLoad.play ("yourePrettyGood", false);
		
		else if (enemies.deaths >= 100)
			clipsLoad.play ("youveDoneThisBefore", false);
		
		else if (enemies.deaths >= 50)
			clipsLoad.play ("theyreNotEvenTrying", false);
		
		else if (enemies.deaths >= 10)
			clipsLoad.play ("whoIsThisGuy", false);
  	} // end gameOverAudio
  	
  	protected void gameOverMessage (Graphics2D g)
  	{
  		g.setColor (Color.WHITE);
  		font = new Font ("SansSerif", 1, 48);
  		g.setFont (font);
  		g.drawString ("Game over", (frameWidth / 2) - 150, (frameHeight / 2) - 50);
  		
  		String message = "";
  		
  		// Complement the player, unless they got under 10 kills
  		if (enemies.deaths < 10)
  		{
  			message = "That was disgusting.";
  			g.drawString (message, (frameWidth / 2) - 257, (frameHeight / 2) + 20);
  		} // end if
  		
  		else if (enemies.deaths >= 9000)
  		{
  			message = "It's over 9000!!!";
  			g.drawString (message, (frameWidth / 2) - 195, (frameHeight / 2) + 20);
  		} // end else if
  		
  		else if (enemies.deaths >= 1000)
  		{
  			message = "That was god-like.";
  			g.drawString (message, (frameWidth / 2) - 225, (frameHeight / 2) + 20);
  		} // end else if
  		
  		else if (enemies.deaths >= 500)
  		{
			message = "Wicked sick!";
  			g.drawString (message, (frameWidth / 2) - 168, (frameHeight / 2) + 20);
  		} // end else if
  		
  		else if (enemies.deaths >= 200)
  		{
  			message = "Hey, you're pretty good!";
  			g.drawString (message, (frameWidth / 2) - 300, (frameHeight / 2) + 20);
  		} // end else if
  		
  		else if (enemies.deaths >= 100)
  		{
  			message = "You've done this before, haven't you?";
  			g.drawString (message, (frameWidth / 2) - 430, (frameHeight / 2) + 20);
  		} // end else if
  		
  		else if (enemies.deaths >= 50)
  		{
  			message = "They're not even trying.";
  			g.drawString (message, (frameWidth / 2) - 285, (frameHeight / 2) + 20);
  		} // end else if
  		
  		else if (enemies.deaths >= 10)
  		{
  			message = "Who is this guy?";
  			g.drawString (message, (frameWidth / 2) - 215, (frameHeight / 2) + 20);
  		} // end else if
  	} // end gameOverMessage
  	
  	protected void keyPress (int keyCode)
  	{
  		// Move the player
  		if (keyCode == KeyEvent.VK_W)
  			player.up = true;
  		
  		if (keyCode == KeyEvent.VK_A)
  			player.left = true;
  		
  		if (keyCode == KeyEvent.VK_S)
  			player.down = true;
  		
  		if (keyCode == KeyEvent.VK_D)
  			player.right = true;
  		
  		// Restart the game
  		if (keyCode == KeyEvent.VK_R)
  			restartGame ();
  	} // end keyPress
  	
  	protected void keyRelease (int keyCode)
  	{
  		if (keyCode == KeyEvent.VK_W)
  			player.up = false;
  		
  		if (keyCode == KeyEvent.VK_A)
  			player.left = false;
  		
  		if (keyCode == KeyEvent.VK_S)
  			player.down = false;
  		
  		if (keyCode == KeyEvent.VK_D)
  			player.right = false;
  	} // end keyRelease
  	
  	protected void mousePress (int x, int y)
  	{
  		// Button logic
  		if (isOverPauseButton)
  		{
  			clipsLoad.play ("click", false);
  			isPaused = !isPaused;
  		} // end if
  		
  		else if (isOverQuitButton)
  		{
  			clipsLoad.play ("click", false);
  			long time = System.currentTimeMillis ();
  			while (System.currentTimeMillis () - time < 200) {} // Allow time for the sound effect to play
  			running = false;
  		} // end else if
  		
  		else if (isOverRestartButton)
  		{
  			clipsLoad.play ("click", false);
  			restartGame ();
  		} // end else if
  		
  		// The player shoots
  		else if (!isPaused && !gameOver)
  		{
  			Point2D target = new Point2D.Double (x, y);
  			Point2D tmpPt = new Point2D.Double(player.position.getX () + 35, player.position.getY () + 40);
  			clipsLoad.play ("gunShot", false);
  			shots.add (new GunShot (tmpPt, target));
  		} // end else if
  	} // end mousePress
  	
  	protected void mouseMove (int x, int y)
  	{
  		if (running)
  		{
  			// The mouse is over the buttons
  			isOverPauseButton = pauseArea.contains (x, y);
  			isOverQuitButton = quitArea.contains (x, y);
  			isOverRestartButton = restartArea.contains (x, y);
  		} // end if
  		
  		// The mouse is anywhere else
  		if (!gameOver)
  			mouseLoc.setLocation (x, y);
  	} // end mouseMove
  	
	private void restartGame ()
  	{
  		// Restart the game and the time
  		gameOver = false;
  		restart = true;
  		resetTime ();
  		simpleInitialize ();
  	} // end restartGame

  	public static void main (String [] args)
  	{
  		int fps = DEFAULT_FPS;
  		if (args.length != 0)
  			fps = Integer.parseInt (args [0]);
  		
  		long period = 1000L / fps;
  		System.out.println ("fps: " + fps + "; period: " + period + " ms");
  		new HoldOut (period * 1000000L);
  	} // end main
} // end HoldOut