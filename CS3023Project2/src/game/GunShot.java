package game;

/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 2 - Hold Out 2: Revenge of the Slimy Menace Clones
 */

import image.*;
import java.awt.Graphics;
import java.awt.geom.*;

public class GunShot
{
	private final int HIT_BOX_SIZE = 50;
	private final int GUNSHOT_DISTANCE = 1000;
	
	private Line2D bulletPath;
	private Point2D source, destination;
	
	protected long createTime;
	
	public GunShot (Point2D src, Point2D dest)
	{
		// Create a line from the player in the direction of the mouse click
		source = src;
		destination = dest;
		
		double xChange = destination.getX () - source.getX ();
		double yChange = destination.getY () - source.getY ();
		
		destination.setLocation (source.getX () + xChange * GUNSHOT_DISTANCE / source.distance (destination),
				source.getY () + yChange * GUNSHOT_DISTANCE / source.distance (destination));
		
		bulletPath = new Line2D.Double (source, destination);
		
		createTime = System.currentTimeMillis ();
	} // end constructor
	
	protected boolean collidesWith (Point2D point)
	{
		// If the circle is within HIT_BOX_WIDTH of the GunShot path
		if (bulletPath.ptSegDist (point) <= HIT_BOX_SIZE)
			return true;
		
		return false;
	} // end collision
	
	protected void draw (Graphics g, ImagesLoader imLoad, ImageSFXs imSFX)
	{
		int angle = (int) Math.toDegrees (Math.asin ((source.getX () - destination.getX ()) / source.distance (destination)));
		
		if (source.getY () > destination.getY ())
			angle = 180 - angle;
		
		g.drawImage (imSFX.getRotatedImage (imLoad.getImage ("Gunshot"), angle - 90), (int) ((source.getX () + destination.getX ()) / 2) - 520,
										   (int) ((source.getY () + destination.getY ()) / 2) - 520, null);
	} // end draw
} // end GunShot