package image;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;

import java.awt.geom.AffineTransform;

import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.awt.image.RescaleOp;
import java.awt.image.ShortLookupTable;
import java.awt.image.WritableRaster;

import java.util.Random;

/**
* Various image operations used as special effects.
* 
* These methods are called from ImagesTests, usually from methods which
* implement a series of images changes (an animation effect) spread over
* several 'ticks' of the update/redraw animation cycle.
* 
* The types of SFXs and their public methods:
* 
* drawImage() based effects draw a resized image: drawResizedImage() return a
* flipped image: getFlippedImage() draw a flipped image: drawVerticalFlip(),
* drawHorizFlip()
* 
* Alpha composite effect draw a faded image: drawFadedImage();
* 
* Affine transform effect return a rotated image: getRotatedImage()
* 
* Convolution effect draw a blurred image: drawBlurredImage(); draw a blurred
* image with a specified blur size: drawBlurredImage()
* 
* LookupOp effect draw a redder image: drawRedderImage() -- there are LookupOp
* and RescaleOp versions
* 
* RescaleOp effects draw a brighter image: drawBrighterImage() draw a negated
* image: drawNegatedImage()
* 
* BandCombineOp effect draw the image with mixed up colours:
* drawMixedColouredImage()
* 
* Pixel effects make some of the image's pixels transparent: eraseImageParts()
* change some of the image's pixels to red or yellow: zapImageParts()
*/
public class ImageSFXs
{
	// constants used to specify the type of image flipping required
	public static final int VERTICAL_FLIP = 0;
	public static final int HORIZONTAL_FLIP = 1;
	public static final int DOUBLE_FLIP = 2; // flip horizontally and vertically

	private GraphicsConfiguration gc;

	// pre-defined image operations
	private RescaleOp negOp, negOpTrans; // image negation
	private ConvolveOp blurOp; // image blurring

	public ImageSFXs ()
	{
		// get the GraphicsConfiguration so images can be copied easily and efficiently
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment ();
		gc = ge.getDefaultScreenDevice ().getDefaultConfiguration ();

		initEffects ();
	} // end constructor

	private void initEffects ()
	{
		// Create pre-defined image operations for image negation and blurring.
		// image negative. Multiply each colour value by -1.0 and add 255
		negOp = new RescaleOp (-1.0f, 255f, null);

		// image negative for images with transparency
		float [] negFactors = {-1.0f, -1.0f, -1.0f, 1.0f}; // don't change the alpha
		float [] offsets = {255f, 255f, 255f, 0.0f};
		negOpTrans = new RescaleOp (negFactors, offsets, null);

		// blur by convolving the image with a matrix
		float ninth = 1.0f / 9.0f;

		// the 'hello world' of Image Ops :)
		float [] blurKernel = {ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth};

		blurOp = new ConvolveOp (new Kernel (3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, null);
	} // end initEffects

	// --------------- affine transform effect: rotation -----------------

	/**
	 * Create a new BufferedImage which is the input image, rotated angle
	 * degrees clockwise.
	 * 
	 * An issue is edge clipping. The simplest solution is to design the image
	 * with plenty of (transparent) border.
	 */
	public BufferedImage getRotatedImage (BufferedImage src, int angle)
	{
		if (src == null)
		{
			System.out.println ("getRotatedImage: input image is null");
			return null;
		} // end if

		int transparency = src.getColorModel ().getTransparency ();
		BufferedImage dest = gc.createCompatibleImage (src.getWidth (), src.getHeight (), transparency);
		Graphics2D g2d = dest.createGraphics ();

		AffineTransform origAT = g2d.getTransform (); // save original transform

		// rotate the coord. system of the dest. image around its center
		AffineTransform rot = new AffineTransform ();
		rot.rotate (Math.toRadians (angle), src.getWidth () / 2, src.getHeight () / 2);
		g2d.transform (rot);

		g2d.drawImage (src, 0, 0, null); // copy in the image

		g2d.setTransform (origAT); // restore original transform
		g2d.dispose ();

		return dest;
	} // end getRotatedImage

	// --------------- LookupOp effect: redden -----------------

	/**
	 * Draw the image with its redness is increased, and its greenness and
	 * blueness decreased. Any alpha channel is left unchanged.
	 */
	public void drawRedderImage (Graphics2D g2d, BufferedImage im, int x, int y, float brightness)
	{
		if (im == null)
		{
			System.out.println ("drawRedderImage: input image is null");
			return;
		} // end if

		if (brightness < 0.0f)
		{
			System.out.println ("Brightness must be >= 0.0f; setting to 0.0f");
			brightness = 0.0f;
		} // end if
		
		// brightness may be less than 1.0 to make the image less red

		short [] brighten = new short [256]; // for red channel
		short [] lessen = new short [256]; // for green and blue channels
		short [] noChange = new short [256]; // for the alpha channel

		for (int i = 0; i < 256; i++)
		{
			float brightVal = 64.0f + (brightness * i);
			
			if (brightVal > 255.0f)
				brightVal = 255.0f;
			
			brighten [i] = (short) brightVal;
			lessen [i] = (short) ((float) i / brightness);
			noChange [i] = (short) i;
		} // end for

		short [] [] brightenRed;
		if (hasAlpha (im))
		{
			brightenRed = new short [4] [];
			brightenRed [0] = brighten;
			brightenRed [1] = lessen;
			brightenRed [2] = lessen;
			brightenRed [3] = noChange; // without this the LookupOp fails
			// which is a bug (?)
		} // end if
		
		else
		{
			// not transparent
			brightenRed = new short [3] [];
			brightenRed [0] = brighten;
			brightenRed [1] = lessen;
			brightenRed [2] = lessen;
		} // end else
		
		LookupTable table = new ShortLookupTable (0, brightenRed);
		LookupOp brightenRedOp = new LookupOp (table, null);

		g2d.drawImage (im, brightenRedOp, x, y);
	} // end drawRedderImage

	// --------- useful support methods --------------

	public boolean hasAlpha (BufferedImage im)
	{
		// does im have an alpha channel?
		if (im == null)
			return false;

		int transparency = im.getColorModel ().getTransparency ();

		if ((transparency == Transparency.BITMASK) || (transparency == Transparency.TRANSLUCENT))
			return true;
		
		else
			return false;
	} // end hasAlpha
} // end ImageSFXs