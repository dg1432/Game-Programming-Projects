package game;

/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 1 - Hold Out
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.*;

public class GunShot
{
	private final int HIT_BOX_SIZE = 30;
	private final int GUNSHOT_DISTANCE = 1000;
	
	private Line2D bulletPath;
	private Point2D source, destination;
	
	private long createTime;
	
	public GunShot (Point2D src, Point2D dest)
	{
		// Create a Line from the player in the direction of the mouse click
		source = src;
		destination = dest;
		
		double xChange = destination.getX () - source.getX ();
		double yChange = destination.getY () - source.getY ();
		
		destination.setLocation (source.getX () + xChange * GUNSHOT_DISTANCE / source.distance (destination),
				source.getY () + yChange * GUNSHOT_DISTANCE / source.distance (destination));
		
		bulletPath = new Line2D.Double (source, destination);
		
		createTime = System.currentTimeMillis ();
	} // end constructor
	
	protected boolean collision (Point2D point)
	{
		// If the circle is within HIT_BOX_WIDTH of the GunShot path
		if (bulletPath.ptSegDist (point) <= HIT_BOX_SIZE)
			return true;
		
		return false;
	} // end collision
	
	protected void draw (Graphics g)
	{
		int angle = (int) Math.toDegrees (Math.asin ((source.getX () - destination.getX ()) /
				source.distance (destination)));
		
		if (source.getY () > destination.getY ())
			angle = 180 - angle;
		
		g.setColor (Color.YELLOW);
		g.drawLine ((int) source.getX () - 20, (int) source.getY () - 20, (int) destination.getX (), (int) destination.getY ());
	} // end draw
	
	public long getCreateTime ()
	{
		return createTime;
	} // end getCreateTime
} // end GunShot