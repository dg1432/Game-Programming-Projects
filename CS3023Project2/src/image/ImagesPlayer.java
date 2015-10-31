package image;
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

import java.awt.image.BufferedImage;

/**
 * ImagesPLayer is aimed at displaying the sequence of images making up a 'n',
 * 's', or 'g' image file, as loaded by ImagesLoader.
 * 
 * The ImagesPlayer constructor is supplied with the intended duration for
 * showing the entire sequence (seqDuration). This is used to calculate
 * showPeriod, the amount of time each image should be shown before the next
 * image is displayed.
 * 
 * The animation period (animPeriod) input argument states how often the
 * ImagesPlayer's updateTick() method will be called. The intention is that
 * updateTick() will be called periodically from the update() method in the
 * top-level animation framework.
 * 
 * The current animation time is calculated when updateTick() is called, and
 * used to calculate imPosition, imPosition specifies which image should be
 * returned when getCurrentImage() is called.
 * 
 * The ImagesPlayer can be set to cycle, stop, resume, or restart at a given
 * image position.
 * 
 * When the sequence finishes, a callback, sequenceEnded(), can be invoked in a
 * specified object implementing the ImagesPlayerWatcher interface.
 */
public class ImagesPlayer
{
	private String imName;
	private boolean isRepeating, ticksIgnored;
	private ImagesLoader imsLoader;

	// period used by animation loop (in ms)
	private int animPeriod;
	
	private long animTotalTime;

	// period the current image is shown (in ms)
	private int showPeriod;
	
	// total duration of the entire image sequence (in secs)
	private double seqDuration;

	private int numImages;
	
	// position of current displayable image
	private int imPosition;

	private ImagesPlayerWatcher watcher = null;

	public ImagesPlayer (String nm, int ap, double d, boolean isr, ImagesLoader il)
	{
		imName = nm;
		animPeriod = ap;
		seqDuration = d;
		isRepeating = isr;
		imsLoader = il;

		animTotalTime = 0L;

		if (seqDuration < 0.5)
		{
			System.out.println ("Warning: minimum sequence duration is 0.5 sec.");
			seqDuration = 0.5;
		} // end if

		if (!imsLoader.isLoaded (imName))
		{
			System.out.println (imName + " is not known by the ImagesLoader");
			numImages = 0;
			imPosition = -1;
			ticksIgnored = true;
		} // end if
		
		else
		{
			numImages = imsLoader.numImages (imName);
			imPosition = 0;
			ticksIgnored = false;
			showPeriod = (int) (1000 * seqDuration / numImages);
		} // end else
	} // end ImagesPlayer

	/** We assume that this method is called every animPeriod ms */
	public void updateTick ()
	{
		if (!ticksIgnored)
		{
			// update total animation time, modulo the animation sequence duration
			animTotalTime = (animTotalTime + animPeriod) % (long) (1000 * seqDuration);

			// calculate current displayable image position in range 0 to num - 1
			imPosition = (int) (animTotalTime / showPeriod); 

			if ((imPosition == numImages - 1) && (!isRepeating))
			{
				// at end of sequence
				ticksIgnored = true; // stop at this image
				if (watcher != null)
					watcher.sequenceEnded (imName); // call callback
			} // end if
		} // end if
	} // end updateTick

	public BufferedImage getCurrentImage ()
	{
		if (numImages != 0)
			return imsLoader.getImage (imName, imPosition);
		
		else
			return null;
	} // end getCurrentImage

	public void setWatcher (ImagesPlayerWatcher w)
	{
		watcher = w;
	} // end setWatcher

	/**
	 * updateTick() calls will no longer update the total animation time or
	 * imPosition.
	 */
	public void stop ()
	{
		ticksIgnored = true;
	} // end stop

	public boolean isStopped ()
	{
		return ticksIgnored;
	} // end isStopped

	public boolean atSequenceEnd ()
	{
		// are we at the last image and not cycling through them?
		return ((imPosition == numImages - 1) && (!isRepeating));
	} // end atSequenceEnd

	/**
	 * Start showing the images again, starting with image number imPosn. This
	 * requires a resetting of the animation time as well.
	 */
	public void restartAt (int imPosn)
	{
		if (numImages != 0)
		{
			if ((imPosn < 0) || (imPosn > numImages - 1))
			{
				System.out.println ("Out of range restart, starting at 0");
				imPosn = 0;
			} // end if

			imPosition = imPosn;
			
			// calculate a suitable animation time
			animTotalTime = (long) imPosition * showPeriod;
			ticksIgnored = false;
		} // end if
	} // end restartAt

	public void resume ()
	{
		// start at previous image position
		if (numImages != 0)
			ticksIgnored = false;
	} // end resume
	
	public void updateName (String newName)
	{
		imName = newName;
	} // end updateName
} // end of ImagesPlayer class