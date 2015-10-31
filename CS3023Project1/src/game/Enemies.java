package game;

/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 1 - Hold Out
 */

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

public class Enemies
{
	private final int MAX_ENEMIES = 25;
	private final int eWidth = 20;
	private final int eHeight = 20;
	
	private int frameWidth, frameHeight;
	private int deaths;
	private int numSimultaneousEnemies;
	
	protected ArrayList <Enemy> enemyList;
	private Random r = new Random ();
	
	public Enemies (int fW, int fH)
	{
		frameWidth = fW;
		frameHeight = fH;
		deaths = 0;
		numSimultaneousEnemies = 10;
		enemyList = new ArrayList <Enemy> ();
	} // end constructor
	
	protected void addEnemy ()
	{
		if (numSimultaneousEnemies < MAX_ENEMIES)
			numSimultaneousEnemies ++;
	} // end addEnemy
	
	protected void draw (Graphics2D g)
	{
		Color [] cArray = {Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW};
		int color;
		
		// Crazy, colorful stuff happens here
		for (int i = 0; i < enemyList.size (); i ++)
		{
			Enemy e = enemyList.get (i);
			color = r.nextInt (cArray.length - 1);
			g.setColor (cArray [color]);
			g.fillOval ((int) e.getX (), (int) e.getY (), eWidth, eHeight);
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
		while (enemyList.size () < numSimultaneousEnemies)
		{
			enemyList.add (new Enemy (frameWidth, frameHeight));
		} // end while
	} // end enemyUpdate
	
	public int getDeaths ()
	{
		return deaths;
	} // end getDeaths
} // end Enemies

class Enemy
{
	protected int health;
	private double speed;
	private long lastDamaged;
	
	private Point2D position;
	private Point2D player;
	
	private Random r = new Random ();
	
	public Enemy (int fW, int fH)
	{
		health = 1;
		speed = 1;
		lastDamaged = 0;
		
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
	
	protected void move (Point2D point)
	{
		// If enemy is dead
		if (health <= 0)
			return;
		
		player = new Point2D.Double (point.getX (), point.getY ());
		
		// If enemy is within reach of the player
		if (position.distance (player) < speed && position.distance (player) > 0)
		{
			position.setLocation (player.getX (), player.getY ());
			return;
		} // end if
		
		double xChange = (position.getX () - player.getX ()) / position.distance (player);
		double yChange = (position.getY () - player.getY ()) / position.distance (player);
		
		position.setLocation (position.getX () - speed * xChange, position.getY () - speed * yChange);
	} // end move
	
	public double getX ()
	{
		return position.getX ();
	} // end getX
	
	public double getY ()
	{
		return position.getY();
	} // end getY
	
	public Point2D getPosition ()
	{
		return position;
	} // end getPosition
	
	protected void damage ()
	{
		if (System.currentTimeMillis () - lastDamaged >= 200)
		{
			lastDamaged = System.currentTimeMillis ();
			health --;
		} // end if
	} // end damage
} // end Enemy