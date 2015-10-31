package game;

/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 2 - Hold Out 2: Revenge of the Slimy Menace Clones
 */

import image.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class Player
{
	private static final int PERIOD = 100;
	
	protected Point2D position;
	
	private double speed = 4;
	protected int health = 10;
	
	private Rectangle2D playArea;
	
	protected boolean up, down, left, right = false;
	private String direction;
	
	private ImagesPlayer playerPlayer;
	private int counter = 0;
	
	public Player (Rectangle2D pA, ImagesLoader imLoad)
	{
		direction = "front";
		playerPlayer = new ImagesPlayer ("Player_" + direction, PERIOD, 3, true, imLoad);
		playArea = pA;
		position = new Point2D.Double ((playArea.getWidth () / 2), playArea.getHeight () / 2);
	} // end constructor
	
	protected void draw (Graphics g, long lastDamagedTime, ImageSFXs imSFX)
	{
		playerPlayer.updateName ("Player_" + direction);
		BufferedImage img = playerPlayer.getCurrentImage ();
		g.drawImage (img, (int) position.getX (), (int) position.getY (), null);
		
		float brightness = 1.0f + (((float) counter % 21) / 10.0f);
		
		// The player has been damaged recently
		if (System.currentTimeMillis () - lastDamagedTime < 200)
			imSFX.drawRedderImage ((Graphics2D) g, img, (int) position.getX (), (int) position.getY (), (float) brightness);
  		
		counter = (counter + 1) % 100;
	} // end draw
	
	protected void move ()
	{
		double newX = 0;
		double newY = 0;
		
		if (up == true && position.getY () - 3 >= playArea.getY ()) // W
		{
			newY -= speed;
			direction = "back";
		} // end if
		
		if (left == true && position.getX () - 3 >= playArea.getY () - 30) // A
		{
			newX -= speed;
			direction = "left";
		} // end if
	    
		if (down == true && position.getY () + 108 <= playArea.getHeight () + 74 ) // S
		{
			newY += speed;
			direction = "front";
		} // end if
		
		if (right == true && position.getX () + 85 <= playArea.getWidth () + 45) // D
		{
			newX += speed;
			direction = "right";
		} // end if
		
		position.setLocation (position.getX () + newX, position.getY () + newY);
		
		// Only update the Player image if they are moving
		if (up || left || down || right)
			playerPlayer.updateTick ();
	} // end move
} // end UserCharacter