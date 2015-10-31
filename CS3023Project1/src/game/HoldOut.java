package game;

/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 1 - Hold Out
 * 
 * Backstory:
 * In the world of Square-Root, circles and squares coexist peacefully.
 * ...
 * UNTIL NOW!!!
 * A nuclear plant built on the circles' territory has melted, resulting in
 * all circles becoming infected and changing colors whenever they move.
 * The squares have tried to fight back, but to no avail. You are the
 * last remaining square of Square-Root. This is your fate.
 * 
 * Controls:
 * W:				Move up
 * A:				Move left
 * S:				Move down
 * D:				Move right
 * Mouse:			Click to shoot
 * R:				Restart
 * Ctl+C, Q, Esc:	Quit game
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class HoldOut extends GameFrame
{
	private static int DEFAULT_FPS = 100;
	private final long ENEMY_ADD_INTERVAL = 2500;
	private final int INVULN_TIME = 100;
	private final int DAMAGE_RADIUS = 35;
	
	private Player player;
	private ArrayList <GunShot> shots;
  	private Enemies enemies;
  	
  	public Point2D mousePoint;
  	
  	private int highScore = 0;
  	
  	private long lastEnemySpawnTime;
  	private long lastDamagedTime;
  	
  	private boolean restart = false;
  	
  	private Font font;
  	private FontMetrics metrics;
  	private DecimalFormat df = new DecimalFormat ("0.##");
  	
  	private Rectangle2D playArea;
  	
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
  		}); // end anonymous inner class
  		
  		addKeyListener (new KeyAdapter ()
  		{
  			public void keyReleased (KeyEvent e)
  			{
  				HoldOut.this.keyRelease (e.getKeyCode ());
  			} // end keyReleased
  		}); // end anonymous inner class
  	} // end constructor
  	
  	protected void simpleInitialize ()
  	{
  		playArea = new Rectangle2D.Double (0, 30, frameWidth, frameHeight - 30);
  		player = new Player (playArea);
  		enemies = new Enemies (frameWidth, frameHeight);
  		
  		shots = new ArrayList <GunShot> ();
  		
  		mousePoint = new Point2D.Double (frameWidth / 2, frameHeight / 2);
  		lastEnemySpawnTime = System.currentTimeMillis ();
  		
  		timeSpentInGame = 0;
  		
  		font = new Font ("SansSerif", 1, 24);
  		metrics = getFontMetrics (font);
  	} // end simpleInitialize
  	
  	protected void simpleRender (Graphics2D gScr)
  	{
  		// Draw the play area
  		gScr.setColor (Color.BLACK);
  		gScr.fillRect ((int) playArea.getX (), (int) playArea.getY (), (int) playArea.getWidth (), (int) playArea.getHeight ());
  		
		// Draw the mission objective
		gScr.setColor (Color.WHITE);
		font = new Font ("SansSerif", 1, 36);
		gScr.setFont (font);
		gScr.drawString ("Destroy the radioactive circles!", (frameWidth / 2) - 280, frameHeight - 50);
  		
  		// Draw the Player
  		player.draw (gScr);
  		
  		// Draw the Enemies
  		enemies.draw (gScr);
  		
  		// Draw the GunShots
  		for (int i = 0; i < shots.size (); i ++)
  			shots.get (i).draw (gScr);
  		
  		// Draw the interface area
  		drawInterface (gScr);
  	} // end simpleRender
  	
  	private void drawInterface (Graphics g)
  	{
  		// Draw top bar of interface
  		g.setColor (Color.GRAY);
  		g.fillRect (0, 0, frameWidth, (int) playArea.getY ());
  		g.setColor (Color.BLACK);
  		g.drawLine (275, 0, 275, 30);
  		g.drawLine (675, 0, 675, 30);
  		g.drawLine ((int) restartArea.getX (), 0, (int) restartArea.getX (), 30);
  		g.drawLine ((int) pauseArea.getX (), 0, (int) pauseArea.getX (), 30);
  		g.drawLine ((int) quitArea.getX (), 0, (int) quitArea.getX (), 30);
  		
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
  		g.setColor (Color.RED);
  		g.fillRect (20, 80, 150, 20);
		g.setColor (Color.GREEN);
		g.fillRect (20, 80, 15 * player.health, 20);
		
		// Draw the current and high scores
		g.setColor (Color.WHITE);
		g.drawString ("Current score: " + enemies.getDeaths (), 20, 123);
		g.drawString ("High score: " + highScore, 685, 23);
  	} // end drawInterface
  	
  	protected void simpleUpdate ()
  	{
  		// Move the player
  		player.move ();
  		
  		// Move the enemies
  		Point2D tmpPoint = new Point2D.Double (player.getPosition ().getX () - 30, player.getPosition ().getY () - 30);
  		enemies.enemyUpdate (tmpPoint);
  		
  		// If the player collides with a radioactive circle, decrease the player's health
  		for (int i = 0; i < enemies.enemyList.size (); i ++)
  		{
  			enemies.enemyList.get (i).move (tmpPoint);
  			
  			if (tmpPoint.distance (enemies.enemyList.get (i).getPosition ()) < DAMAGE_RADIUS &&
  					System.currentTimeMillis () - lastDamagedTime > INVULN_TIME)
  			{
  				player.health --;
  				lastDamagedTime = System.currentTimeMillis ();
  				
  				// If the player is dead
  				if (player.health <= 0)
  				{
  					gameOver = true;
  					if (highScore < enemies.getDeaths ())
  						highScore = enemies.getDeaths ();
  				} // end if
  			} // end if
  		} // end for
  		
  		// Spawn enemies every (ENEMY_ADD_INTERVAL/1000) seconds
  		if (System.currentTimeMillis() - lastEnemySpawnTime >= ENEMY_ADD_INTERVAL)
  		{
  			enemies.addEnemy ();
  			lastEnemySpawnTime = System.currentTimeMillis ();
  		} // end if
  		
  		// If any GunShots collide with radioactive circles, damage them
  		for (int i = 0; i < shots.size (); i ++)
  		{
  			for (int j = 0; j < enemies.enemyList.size (); j ++)
  			{
  				if (shots.get (i).collision (enemies.enemyList.get (j).getPosition ()))
  					enemies.enemyList.get (j).damage ();
  			} // end for
  			
  			if (System.currentTimeMillis () - shots.get (i).getCreateTime () > 40)
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
  	
  	protected void gameOverMessage (Graphics2D g)
  	{
  		g.setColor (Color.WHITE);
  		font = new Font ("SansSerif", 1, 48);
  		g.setFont (font);
  		g.drawString ("Game over", (frameWidth / 2) - 150, (frameHeight / 2) - 50);
  		
  		String message = "";
  		
  		// Print hateful messages, unless the player destroys over 9000 radioactive circles, which is OK
  		if (enemies.getDeaths () < 10)
  		{
  			message = "Try harder, n00b.";
  			g.drawString (message, (frameWidth / 2) - 220, (frameHeight / 2) + 20);
  		} // end if
  		
  		else if (enemies.getDeaths () >= 9000)
  		{
  			message = "It's over 9000!!!";
  			g.drawString (message, (frameWidth / 2) - 195, (frameHeight / 2) + 20);
  		} // end else if
  		
  		else if (enemies.getDeaths () >= 1000)
  		{
  			message = "Go outside. Now.";
  			g.drawString (message, (frameWidth / 2) - 210, (frameHeight / 2) + 20);
  		} // end else if
  		
  		else if (enemies.getDeaths () >= 500)
  		{
  			message = "Nice job!";
  			g.drawString (message, (frameWidth / 2) - 120, (frameHeight / 2) + 20);
  		} // end else if
  		
  		else if (enemies.getDeaths () >= 200)
  		{
  			message = "Hey, you're pretty good!";
  			g.drawString (message, (frameWidth / 2) - 300, (frameHeight / 2) + 20);
  		} // end else if
  		
  		else if (enemies.getDeaths () >= 100)
  		{
  			message = "Not bad.";
  			g.drawString (message, (frameWidth / 2) - 115, (frameHeight / 2) + 20);
  		} // end else if
  		
  		else if (enemies.getDeaths () >= 50)
  		{
  			message = "You're getting better.";
  			g.drawString (message, (frameWidth / 2) - 260, (frameHeight / 2) + 20);
  		} // end else if
  		
  		else if (enemies.getDeaths () >= 10)
  		{
  			message = "Meh. You can do better.";
  			g.drawString (message, (frameWidth / 2) - 290, (frameHeight / 2) + 20);
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
  			isPaused = !isPaused;
  		
  		else if (isOverQuitButton)
  			running = false;
  		
  		else if (isOverRestartButton)
  			restartGame ();
  		
  		// The player shoots
  		else if (!isPaused && !gameOver)
  		{
  			Point2D target = new Point2D.Double (x, y);
  			shots.add (new GunShot (player.getPosition (), target));
  		} // end else if
  	} // end mousePress
  	
  	protected void mouseMove (int x, int y)
  	{
  		if (running)
  		{
  			// The mouse is over the buttons
  			isOverPauseButton = (pauseArea.contains (x, y));
  			isOverQuitButton = (quitArea.contains (x, y));
  			isOverRestartButton = (restartArea.contains (x, y));
  		} // end if
  		
  		// The mouse is anywhere else
  		if (!gameOver)
  			mousePoint.setLocation (x, y);
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