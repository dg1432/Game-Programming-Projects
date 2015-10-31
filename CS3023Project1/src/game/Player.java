package game;

/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 1 - Hold Out
 */

import java.awt.*;
import java.awt.geom.*;

public class Player
{
	private final int pWidth = 50;
	private final int pHeight = 50;
	
	private Point2D position;
	
	private double speed = 3;
	protected int health;
	
	private Rectangle2D playArea;
	
	protected boolean up, down, left, right = false;
	
	public Player (Rectangle2D pA)
	{
		playArea = pA;
		position = new Point2D.Double (playArea.getWidth () / 2, playArea.getHeight () / 2);
		health = 10;
	} // end constructor
	
	protected void draw (Graphics g)
	{
		g.setColor (Color.RED);
		g.fillRect ((int) position.getX () - 45, (int) position.getY () - 45, pWidth, pHeight);
	} // end draw
	
	protected void move ()
	{
		double newX = 0;
		double newY = 0;
		
		if (up == true && position.getY () - 50 >= playArea.getY ()) // W
			newY -= speed;
		
		if (left == true && position.getX () - 50 >= playArea.getY () - 30) // A
			newX -= speed;
	    
		if (down == true && position.getY () + 50 <= playArea.getHeight () + 74 ) // S
			newY += speed;
		
		if (right == true && position.getX () + 50 <= playArea.getWidth () + 45) // D
			newX += speed;
		
		position.setLocation (position.getX () + newX, position.getY () + newY);
	} // end move
	
	public Point2D getPosition ()
	{
		return position;
	} // end getPosition
	
	public int getWidth ()
	{
		return pWidth;
	} // end getWidth
	
	public int getHeight ()
	{
		return pHeight;
	} // end getHeight
} // end UserCharacter