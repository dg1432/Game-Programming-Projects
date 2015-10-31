package game;
/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 2 - Hold Out 2: Revenge of the Slimy Menace Clones
 */

import image.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.util.*;

public class Pickups
{
	private final int MAX_PICKUPS = 5;
	
	private int startNumPickups = 0;
	private int frameWidth, frameHeight;
	
	protected ArrayList <Pickup> pickupList = new ArrayList <Pickup> ();
	
	public Pickups (int fW, int fH, ImagesLoader imLoad)
	{
		frameWidth = fW;
		frameHeight = fH;
	} // end constructor
	
	public void addPickup ()
	{
		if (startNumPickups < MAX_PICKUPS)
			startNumPickups ++;
	} // end addPickup
	
	public void removePickup ()
	{
		if (startNumPickups > 0)
			startNumPickups --;
	} // end removePickup
	
	public void updatePickups ()
	{
		for (int i = 0; i < pickupList.size (); i ++)
		{
			if (pickupList.get (i).pickedUp)
				pickupList.remove (i);
		} // end for
		
		while (pickupList.size () < startNumPickups)
			pickupList.add (new Pickup (frameWidth, frameHeight));
	} // end updatePickups
	
	public void draw (Graphics g, ImagesLoader imLoad)
	{
		for (int i = 0; i < pickupList.size (); i ++)
		{
			Pickup p = pickupList.get (i);
			BufferedImage img = imLoad.getImage (p.type); 
			g.drawImage (img, (int) p.x, (int) p.y, null);
		} // end for
	} // end draw
} // end Pickups

class Pickup
{
	protected boolean pickedUp = false;
	protected long pickedUpTime = 0;
	private Random r = new Random ();
	
	protected double x;
	protected double y;
	
	protected String type;
	
	protected Point2D position;
	
	public Pickup (int fW, int fH)
	{
		type = "heart";
		x = r.nextInt (fW - 40) + 40;
		y = r.nextInt (fH - 40) + 40;
		position = new Point2D.Double(x, y);
	} // end constructor
	
	public void setPicked (boolean pick)
	{
		pickedUp = pick;
	} // end setPicked
} // end Pickup