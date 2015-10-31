package game;
/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 2 - Hold Out 2: Revenge of the Slimy Menace Clones
 */

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;
import image.*;

public class Enemies
{
	private final int MAX_ENEMIES = 20;
	
	private int frameWidth, frameHeight;
	protected int deaths = 0;
	private int numStartingEnemies = 5;
	
	protected ArrayList <Enemy> enemyList = new ArrayList <Enemy> ();
	
	private ImagesPlayer slimePlayer;
	private static final int PERIOD = 100;
	private int counter = 0;
	
	public Enemies (int fW, int fH, ImagesLoader imLoad)
	{
		slimePlayer = new ImagesPlayer ("BlueSlime_front", PERIOD, 3, true, imLoad);
		frameWidth = fW;
		frameHeight = fH;
	} // end constructor
	
	protected void addEnemy ()
	{
		if (numStartingEnemies < MAX_ENEMIES)
			numStartingEnemies ++;
	} // end addEnemy
	
	protected void draw (Graphics2D g, ImageSFXs imSFX)
	{
		for (int i = 0; i < enemyList.size (); i ++)
		{
			Enemy e = enemyList.get (i);
			slimePlayer.updateName (e.prefix + "Slime_" + e.direction);
			BufferedImage img = slimePlayer.getCurrentImage ();
			g.drawImage (img, (int) e.getX (), (int) e.getY (), null);
			
			float brightness = 1.0f + (((float) counter % 21) / 10.0f);
			
			// The enemy has been damaged recently
			if (System.currentTimeMillis () - e.lastDamaged < 200)
				imSFX.drawRedderImage ((Graphics2D) g, img, (int) e.getX (), (int) e.getY (), (float) brightness);
	  		
			counter = (counter + 1) % 100;
			
			// Draw enemy health bars
			if (e.health > 0)
			{
				g.setColor (Color.BLACK);
				g.fillRect ((int) e.getX () - 1, (int) e.getY () - 16, 32, 9);
				g.setColor (Color.RED);
				g.fillRect ((int) e.getX (), (int) e.getY () - 15, 30, 7);
				g.setColor (Color.GREEN);
				g.fillRect ((int) e.getX (), (int) e.getY () - 15, e.health * (30 / e.totalHealth), 7);
			} // end if
		} // end for
	} // end draw
	
	protected void enemyUpdate (Point2D point)
	{
		for (int i = 0; i < enemyList.size (); i ++)
		{
			enemyList.get (i).move (point);
			
			// Remove enemies that are dead
			if (enemyList.get (i).health <= 0)
			{
				deaths ++;
				enemyList.remove (i);
			} // end if
		} // end for
		
		// Repopulate the enemies list
		while (enemyList.size () < numStartingEnemies)
			enemyList.add (new Enemy (frameWidth, frameHeight));
		
		slimePlayer.updateTick ();
	} // end enemyUpdate
} // end Enemies

class Enemy
{
	protected int health;
	protected int totalHealth;
	protected long lastDamaged = 0;
	protected String prefix;
	protected String direction;
	private double speed;
	
	protected Point2D position;
	private Point2D player;
	
	private Random r = new Random ();
	
	public Enemy (int fW, int fH)
	{
		determineType ();
		
		// Determine where to spawn the enemies
		int spawnWall = r.nextInt (4);
		double spawnLocation = r.nextDouble ();
		
		// Spawn enemy on east wall
		if (spawnWall == 0)
			position = new Point2D.Double (0, fH * spawnLocation);
		
		// Spawn enemy on north wall
		else if (spawnWall == 1)
			position = new Point2D.Double (fW * spawnLocation, 0);
		
		// Spawn enemy on south wall
		else if (spawnWall == 2)
			position = new Point2D.Double (fW * spawnLocation, fH);
		
		// Spawn enemy on west wall
		else
			position = new Point2D.Double (fW, fH * spawnLocation);
	} // end constructor
	
	private void determineType ()
	{
		int prob = r.nextInt (1000) + 1;
		
		if (prob >= 1 && prob < 400) // Blue slime
		{
			health = 1;
			speed = 1;
			prefix = "Blue";
		} // end if
		
		else if (prob >= 400 && prob < 650) // Cyan slime
		{
			health = 2;
			speed = 0.75;
			prefix = "Cyan";
		} // end else if
		
		else if (prob >= 650 && prob < 750) // Orange slime
		{
			health = 1;
			speed = 1.5;
			prefix = "Orange";
		} // end else if
		
		else if (prob >= 750 && prob < 850) // Pink slime
		{
			health = 3;
			speed = 0.5;
			prefix = "Pink";
		} // end else if
		
		else if (prob >= 850 && prob < 900) // Purple slime
		{
			health = 5;
			speed = 0.25;
			prefix = "Purple";
		} // end else if
		
		else if (prob >= 900 && prob < 950) // Red slime
		{
			health = 1;
			speed = 1.75;
			prefix = "Red";
		} // end else if
		
		else // Yellow slime
		{
			health = 1;
			speed = 1.75;
			prefix = "Yellow";
		} // end else if
		
		totalHealth = health;
	} // end determineType
	
	protected void move (Point2D point)
	{
		// If enemy is not dead
		if (health > 0)
		{
			player = new Point2D.Double (point.getX (), point.getY ());
			
			// Determine which way the enemy faces
			if (position.getY () >= player.getY () && position.getY () - player.getY () >= Math.abs (position.getX () - player.getX()))
				direction = "back";
			
			else if (position.getX () < player.getX () && position.getX () - player.getX () <= Math.abs(position.getY () - player.getY()))
				direction = "right";
			
			else if (position.getY () < player.getY () && player.getY () - position.getY () >= Math.abs(position.getX () - player.getX()))
				direction = "front";
			
			else if (position.getX () >= player.getX () && position.getX () - player.getX () > Math.abs(position.getY () - player.getY()))
				direction = "left";
			
			// If enemy is within reach of the player
			if (position.distance (player) < speed && position.distance (player) > 0)
			{
				position.setLocation (player.getX (), player.getY ());
				return;
			} // end if
			
			double xChange = (position.getX () - player.getX ()) / position.distance (player);
			double yChange = (position.getY () - player.getY ()) / position.distance (player);
			
			position.setLocation (position.getX () - speed * xChange, position.getY () - speed * yChange);
		} // end if
	} // end move
	
	public double getX ()
	{
		return position.getX ();
	} // end getX
	
	public double getY ()
	{
		return position.getY();
	} // end getY
	
	protected void damage ()
	{
		if (System.currentTimeMillis () - lastDamaged >= 200)
		{
			lastDamaged = System.currentTimeMillis ();
			health --;
		} // end if
	} // end damage
} // end Enemy