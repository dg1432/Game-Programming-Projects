package game;

/*
 * David Glover, February 2014, adapted from
 * Roger Mailler, January 2009, adapted from
 * Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th
 *
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 2 - Hold Out 2: Revenge of the Slimy Menace Clones
 */


import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.text.DecimalFormat;
import javax.swing.JFrame;

public abstract class GameFrame extends JFrame implements Runnable
{
	private static final int NUM_BUFFERS = 2; // used for page flipping

	// record stats every 1 second (roughly)
	private static long MAX_STATS_INTERVAL = 1000000000L;

	private static final int NO_DELAYS_PER_YIELD = 16;

	/*
	 * Number of frames with a delay of 0 ms before the animation thread yields
	 * to other running threads.
	 */
	private static int MAX_FRAME_SKIPS = 5;

	private static int NUM_FPS = 10; // number of FPS values stored to get an average

	protected int frameWidth, frameHeight; // panel dimensions

	private Thread animator; // the thread that performs the animation
	protected boolean running = false; // used to stop the animation thread
	protected boolean isPaused = false;
	private boolean finishedOff = false;

	protected long period; // period between drawing in _nanosecs_

	// used for gathering statistics
	private long statsInterval = 0L; // in ns
	private long prevStatsTime;
	private long totalElapsedTime = 0L;
	private long gameStartTime;
	public int timeSpentInGame = 0; // in seconds
	protected int totalTimeSpentInGame = 0;
	protected long restartTime = 0;

	private long frameCount = 0;
	private double fpsStore [] ;
	private long statsCount = 0;
	protected double averageFPS = 0.0;

	private long framesSkipped = 0L;
	private long totalFramesSkipped = 0L;
	private double upsStore [] ;
	protected double averageUPS = 0.0;
	private DecimalFormat df = new DecimalFormat ("0.##"); // 2 decimal points

	// used at game termination
	protected boolean gameOver = false;

	// used for full-screen exclusive mode
	private GraphicsDevice gd;
	private Graphics gScr;
	private BufferStrategy bufferStrategy;

	public GameFrame(long period)
	{
		this.period = period;

		initFullScreen ();

		simpleInitialize ();

		addMouseListener (new MouseAdapter ()
		{
			public void mousePressed (MouseEvent e)
			{
				GameFrame.this.mousePress (e.getX (), e.getY ());
			} // end mousePressed
		} ); // end anonymous inner class

		addMouseMotionListener (new MouseMotionAdapter ()
		{
			public void mouseMoved (MouseEvent e)
			{
				GameFrame.this.mouseMove (e.getX (), e.getY ());
			} // end mouseMoved
		} ); // end anonymous inner class

		addKeyListener (new KeyAdapter ()
		{
			public void keyPressed (KeyEvent e)
			{
				int keyCode = e.getKeyCode ();
				
				// listen for esc, q, end, ctrl-c on the canvas to
				// allow a convenient exit from the full screen configuration
				if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_Q) || (keyCode == KeyEvent.VK_END) || ((keyCode == KeyEvent.VK_C) && e.isControlDown ()))
					running = false;
				
				else if (keyCode == KeyEvent.VK_P)
					isPaused = !isPaused;
				
				else
					keyPress (keyCode);
			} // end keyPressed
		} ); // end anonymous inner class
		
		// Initialize timing elements
		fpsStore = new double [NUM_FPS];
		upsStore = new double [NUM_FPS];
		for (int i = 0; i < NUM_FPS; i ++)
		{
			fpsStore [i] = 0.0;
			upsStore [i] = 0.0;
		} // end for

		gameStart ();
	} // end constructor

	private void initFullScreen ()
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment ();
		gd = ge.getDefaultScreenDevice ();

		setUndecorated (true); // no menu bar, borders, etc. or Swing components
		setIgnoreRepaint (true); // turn off all paint events since doing active rendering
		setResizable (false);

		if (!gd.isFullScreenSupported ())
		{
			System.out.println ("Full-screen exclusive mode not supported");
			System.exit (0);
		} // end if
		
		gd.setFullScreenWindow (this); // switch on full-screen exclusive mode

		// we can now adjust the display modes, if we wish
		showCurrentMode ();

		reportCapabilities ();

		frameWidth = getBounds ().width;
		frameHeight = getBounds ().height;

		setBufferStrategy ();
	} // end initFullScreen

	private void reportCapabilities ()
	{
		GraphicsConfiguration gc = gd.getDefaultConfiguration ();

		// Image Capabilities
		ImageCapabilities imageCaps = gc.getImageCapabilities ();
		System.out.println ("Image Caps. isAccelerated: " + imageCaps.isAccelerated ());
		System.out.println ("Image Caps. isTrueVolatile: " + imageCaps.isTrueVolatile ());

		// Buffer Capabilities
		BufferCapabilities bufferCaps = gc.getBufferCapabilities ();
		System.out.println ("Buffer Caps. isPageFlipping: " + bufferCaps.isPageFlipping ());
		System.out.println ("Buffer Caps. Flip Contents: " + getFlipText(bufferCaps.getFlipContents ()));
		System.out.println ("Buffer Caps. Full-screen Required: " + bufferCaps.isFullScreenRequired ());
		System.out.println ("Buffer Caps. MultiBuffers: " + bufferCaps.isMultiBufferAvailable ());
	} // end reportCapabilities

	private String getFlipText (BufferCapabilities.FlipContents flip)
	{
		if (flip == null)
			return "false";
		
		else if (flip == BufferCapabilities.FlipContents.UNDEFINED)
			return "Undefined";
		
		else if (flip == BufferCapabilities.FlipContents.BACKGROUND)
			return "Background";
		
		else if (flip == BufferCapabilities.FlipContents.PRIOR)
			return "Prior";
		
		else
			// if (flip == BufferCapabilities.FlipContents.COPIED)
			return "Copied";
	} // end getFlipTest

	private void setBufferStrategy ()
	{
		createBufferStrategy (NUM_BUFFERS);
		this.bufferStrategy = getBufferStrategy (); // store for later
	} // end setBufferStrategy

	/**
	 * Starts the animation loop
	 */
	private void gameStart ()
	// initialize and start the thread
	{
		if (animator == null || !running)
		{
			animator = new Thread (this);
			animator.start ();
		} // end if
	} // end startGame

	// ------------- game life cycle methods ------------
	public void resumeGame ()
	{
		isPaused = false;
	} // end resumeGame

	public void pauseGame ()
	{
		isPaused = true;
	} // end pauseGame

	public void stopGame ()
	{
		running = false;
	} // end stopGame
	// ----------------------------------------------

	public void run ()
	/* The frames of the animation are drawn inside the while loop. */
	{
		long beforeTime, afterTime, timeDiff, sleepTime;
		long overSleepTime = 0L;
		int noDelays = 0;
		long excess = 0L;

		gameStartTime = System.nanoTime ();
		prevStatsTime = gameStartTime;
		beforeTime = gameStartTime;

		running = true;

		while (running)
		{
			gameUpdate ();
			screenUpdate ();

			afterTime = System.nanoTime ();
			timeDiff = afterTime - beforeTime;
			sleepTime = period - timeDiff - overSleepTime;

			if (sleepTime > 0)
			{
				// some time left in this cycle
				try
				{
					Thread.sleep (sleepTime / 1000000L); // nano -> ms
				} // end try
				
				catch (InterruptedException e)
				{
					
				} // end catch
				
				overSleepTime = System.nanoTime () - afterTime - sleepTime;
			} // end if
			
			else
			{
				// sleepTime <= 0; the frame took longer than the period
				excess -= sleepTime; // store excess time value
				overSleepTime = 0L;

				if (noDelays ++ >= NO_DELAYS_PER_YIELD)
				{
					Thread.yield (); // give another thread a chance to run
					noDelays = 0;
				} // end if
			} // end else

			beforeTime = System.nanoTime ();

			/*
			 * If frame animation is taking too long, update the game state
			 * without rendering it, to get the updates/sec nearer to the
			 * required FPS.
			 */
			int skips = 0;
			while (excess > period && skips < MAX_FRAME_SKIPS)
			{
				excess -= period;
				gameUpdate (); // update state but don't render
				skips ++;
			} // end while
			
			framesSkipped += skips;
			storeStats ();
		} // end while
		
		finishOff ();
		System.exit (0); // so window disappears
	} // end run

	/**
	 * Renders to the backbuffer
	 */
	private void gameRender (Graphics gScr)
	{
		// clear the background
		gScr.setColor (Color.white);
		gScr.fillRect (0, 0, frameWidth, frameHeight);

		simpleRender ((Graphics2D) gScr);

		if (gameOver)
			gameOverMessage ((Graphics2D) gScr);
		
		if (isPaused && !gameOver)
			drawPauseScreen ((Graphics2D) gScr);
	} // end gameRender

	private void screenUpdate ()
	{
		// use active rendering
		try
		{
			gScr = (Graphics2D) bufferStrategy.getDrawGraphics ();
			gameRender (gScr);
			gScr.dispose ();
			
			if (!bufferStrategy.contentsLost ())
				bufferStrategy.show ();
			
			else
				System.out.println ("Contents Lost");
		} // end try
		
		catch (Exception e)
		{
			e.printStackTrace ();
			running = false;
		} // end catch
	} // end screenUpdate

	/**
	 * Should update the game state
	 */
	private void gameUpdate ()
	{
		if (!isPaused && !gameOver)
			simpleUpdate ();
	} // end gameUpdate

	private void storeStats ()
	/*
	 * The statistics: - the summed periods for all the iterations in this
	 * interval (period is the amount of time a single frame iteration should
	 * take), the actual elapsed time in this interval, the error between these
	 * two numbers;
	 * 
	 * - the total frame count, which is the total number of calls to run();
	 * 
	 * - the frames skipped in this interval, the total number of frames
	 * skipped. A frame skip is a game update without a corresponding render;
	 * 
	 * - the FPS (frames/sec) and UPS (updates/sec) for this interval, the
	 * average FPS & UPS over the last NUM_FPSs intervals.
	 * 
	 * The data is collected every MAX_STATS_INTERVAL (1 sec).
	 */
	{
		frameCount ++;
		statsInterval += period;

		if (statsInterval >= MAX_STATS_INTERVAL)
		{
			// record stats every
			// MAX_STATS_INTERVAL
			long timeNow = System.nanoTime ();
			
			if (!gameOver) // used to reset the timer in-game
			{
				if (restartTime == 0)
					timeSpentInGame = (int) ((timeNow - gameStartTime) / 1000000000L); // ns -> secs
				
				else
					timeSpentInGame = (int) ((timeNow - restartTime) / 1000000000L); // ns -> secs
			} // end if
			
			totalTimeSpentInGame = (int) ((timeNow - gameStartTime) / 1000000000L); // ns -> secs

			long realElapsedTime = timeNow - prevStatsTime; // time since last stats collection
			totalElapsedTime += realElapsedTime;

			totalFramesSkipped += framesSkipped;

			double actualFPS = 0; // calculate the latest FPS and UPS
			double actualUPS = 0;
			
			if (totalElapsedTime > 0)
			{
				actualFPS = (((double) frameCount / totalElapsedTime) * 1000000000L);
				actualUPS = (((double) (frameCount + totalFramesSkipped) / totalElapsedTime) * 1000000000L);
			} // end if

			// store the latest FPS and UPS
			fpsStore [(int) statsCount % NUM_FPS] = actualFPS;
			upsStore [(int) statsCount % NUM_FPS] = actualUPS;
			statsCount = statsCount + 1;

			double totalFPS = 0.0; // total the stored FPSs and UPSs
			double totalUPS = 0.0;
			
			for (int i = 0; i < NUM_FPS; i ++)
			{
				totalFPS += fpsStore [i];
				totalUPS += upsStore [i];
			} // end for

			if (statsCount < NUM_FPS)
			{
				// obtain the average FPS and UPS
				averageFPS = totalFPS / statsCount;
				averageUPS = totalUPS / statsCount;
			} // end if
			
			else
			{
				averageFPS = totalFPS / NUM_FPS;
				averageUPS = totalUPS / NUM_FPS;
			} // end else
			
			framesSkipped = 0;
			prevStatsTime = timeNow;
			statsInterval = 0L; // reset
		} // end if
	} // end storeStats

	private void finishOff ()
	/*
	 * Tasks to do before terminating. Called at end of run() and via the
	 * shutdown hook in readyForTermination().
	 * 
	 * The call at the end of run() is not really necessary, but included for
	 * safety. The flag stops the code being called twice.
	 */
	{
		if (!finishedOff)
		{
			finishedOff = true;
			printStats ();
			restoreScreen ();
			System.exit (0);
		} // end if
	} // end finishedOff

	private void printStats ()
	{
		System.out.println ("Frame Count/Loss: " + frameCount + " / " + totalFramesSkipped);
		System.out.println ("Average FPS: " + df.format (averageFPS));
		System.out.println ("Average UPS: " + df.format (averageUPS));
		System.out.println ("Time Spent: " + totalTimeSpentInGame + " secs");
	} // end printStats

	private void restoreScreen ()
	/*
	 * Switch off full screen mode. This also resets the display mode if it's
	 * been changed.
	 */
	{
		Window w = gd.getFullScreenWindow ();
		
		if (w != null)
			w.dispose();
		
		gd.setFullScreenWindow (null);
	} // end restoreScreen

	// ------------------ display mode methods -------------------
	private void setDisplayMode (int width, int height, int bitDepth)
	// attempt to set the display mode to the given width, height, and bit depth
	{
		if (!gd.isDisplayChangeSupported ())
		{
			System.out.println ("Display mode changing not supported");
			return;
		} // end if

		if (!isDisplayModeAvailable (width, height, bitDepth))
		{
			System.out.println ("Display mode (" + width + "," + height + "," + bitDepth + ") not available");
			return;
		} // end if

		DisplayMode dm = new DisplayMode (width, height, bitDepth, DisplayMode.REFRESH_RATE_UNKNOWN); // any refresh rate
		try
		{
			gd.setDisplayMode (dm);
			System.out.println ("Display mode set to: (" + width + "," + height + "," + bitDepth + ")");
		} // end try
		
		catch (IllegalArgumentException e)
		{
			System.out.println ("Error setting Display mode (" + width + "," + height + "," + bitDepth + ")");
		} // end catch

		try
		{
			// sleep to give time for the display to be changed
			Thread.sleep(1000); // 1 sec
		} // end try
		
		catch (InterruptedException ex)
		{
			
		} // end catch
	} // end setDisplayMode

	private boolean isDisplayModeAvailable (int width, int height, int bitDepth)
	/*
	 * Check that a displayMode with this width, height, bit depth is available.
	 * We don't care about the refresh rate, which is probably
	 * REFRESH_RATE_UNKNOWN anyway.
	 */
	{
		DisplayMode [] modes = gd.getDisplayModes ();
		showModes (modes);

		for (int i = 0; i < modes.length; i ++)
		{
			if (width == modes [i].getWidth () && height == modes [i].getHeight () && bitDepth == modes [i].getBitDepth ())
				return true;
		} // end for
		
		return false;
	} // end isDisplayModeAvailable

	private void showModes (DisplayMode [] modes)
	// pretty print the display mode information in modes
	{
		System.out.println ("Modes");
		for (int i = 0; i < modes.length; i ++)
		{
			System.out.print ("(" + modes [i].getWidth () + "," + modes [i].getHeight () + "," + modes [i].getBitDepth () + "," + modes [i].getRefreshRate () + ")  ");
			
			if ((i + 1) % 4 == 0)
				System.out.println ();
		} // end for
		
		System.out.println ();
	} // end showModes

	private void showCurrentMode ()
	// print the display mode details for the graphics device
	{
		DisplayMode dm = gd.getDisplayMode ();
		System.out.println ("Current Display Mode: (" + dm.getWidth () + "," + dm.getHeight () + "," + dm.getBitDepth () + "," + dm.getRefreshRate () + ")  ");
	} // end showCurrentMode
	
	public void resetTime ()
	{
		timeSpentInGame = 0;
		restartTime = System.nanoTime ();
	} // end resetTime

	/**
	 * Should implement game specific rendering
	 * 
	 * @param g
	 */
	protected abstract void simpleRender (Graphics2D g);

	/**
	 * Should display a game specific game over message
	 * 
	 * @param g
	 */
	protected abstract void gameOverMessage (Graphics2D g);
	
	/**
	 * Should display a game specific pause screen
	 * 
	 * @param g
	 */
	protected abstract void drawPauseScreen (Graphics2D g);

	/**
	 * Should update the game data
	 */
	protected abstract void simpleUpdate ();

	/**
	 * This just gets called when a click occurs, no default behavior
	 */
	protected abstract void mousePress (int x, int y);

	/**
	 * This just gets called when a movement occurs, no default behavior
	 */
	protected abstract void mouseMove (int x, int y);

	/**
	 * Should be overridden to initialize the game specific components
	 */
	protected abstract void simpleInitialize ();
	
	/**
	 * This just gets called when a key is pressed, no default behavior
	 */
	protected abstract void keyPress (int keyCode);
	
	/**
	 * This just gets called when a key is released, no default behavior
	 */
	protected abstract void keyRelease (int keyCode);
} // end of GamePanel class