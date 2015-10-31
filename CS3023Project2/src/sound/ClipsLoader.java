package sound;

import java.awt.*;
import java.util.*;
import java.io.*;

public class ClipsLoader
{
	private final static String SOUND_DIR = "Sounds/";

	private HashMap clipsMap;

	public ClipsLoader (String soundsFnm)
	{
		clipsMap = new HashMap ();
    	loadSoundsFile (soundsFnm);
	} // end constructor

	public ClipsLoader ()
	{
		clipsMap = new HashMap ();
	} // end constructor

	private void loadSoundsFile (String soundsFnm)
	/* The file format are lines of:
        <name> <filename>         // a single sound file
     	and blank lines and comment lines.
	 */
	{ 
		String sndsFNm = SOUND_DIR + soundsFnm;
		System.out.println ("Reading file: " + sndsFNm);
		try
		{
			InputStream in = this.getClass ().getResourceAsStream (sndsFNm);
			BufferedReader br = new BufferedReader (new InputStreamReader (in));
			StringTokenizer tokens;
			String line, name, fnm;
			while ((line = br.readLine()) != null)
			{
				if (line.length () == 0) // blank line
					continue;
				
				if (line.startsWith ("//")) // comment
					continue;

				tokens = new StringTokenizer (line);
				if (tokens.countTokens () != 2)
					System.out.println ("Wrong no. of arguments for " + line);
				
				else
				{
					name = tokens.nextToken ();
					fnm = tokens.nextToken ();
					load (name, fnm);
				} // end else
			} // end while
			
			br.close ();
		} // end try
		
		catch (IOException e) 
		{
			System.out.println ("Error reading file: " + sndsFNm);
			System.exit (1);
		} // end catch
	} // end loadSoundsFile

	public void load (String name, String fnm)
	// create a ClipInfo object for name and store it
	{
		if (clipsMap.containsKey (name))
			System.out.println ("Error: " + name + "already stored");
		
		else
		{
			clipsMap.put (name, new ClipInfo (name, fnm));
			System.out.println ("-- " + name + "/" + fnm);
		} // end else
	} // end load

	public void close (String name)
	// close the specified clip
	{
		ClipInfo ci = (ClipInfo) clipsMap.get (name);
		if (ci == null)
			System.out.println ( "Error: " + name + "not stored");
		else
			ci.close ();
	} // end close

	public void play (String name, boolean toLoop)
	// play (perhaps loop) the specified clip
	{
		ClipInfo ci = (ClipInfo) clipsMap.get (name);
		if (ci == null)
			System.out.println ("Error: " + name + "not stored");
		
		else
			ci.play (toLoop);
	} // end play

	public void stop (String name)
	// stop the clip, resetting it to the beginning
	{
		ClipInfo ci = (ClipInfo) clipsMap.get (name);
		if (ci == null)
			System.out.println ("Error: " + name + "not stored");
		
		else
			ci.stop ();
	} // end stop

	public void pause (String name)
	{
		ClipInfo ci = (ClipInfo) clipsMap.get (name);
		if (ci == null)
			System.out.println ( "Error: " + name + "not stored");
		
		else
			ci.pause ();
	} // end pause

	public void resume (String name)
	{
		ClipInfo ci = (ClipInfo) clipsMap.get (name);
		if (ci == null)
			System.out.println ("Error: " + name + "not stored");
		
		else
			ci.resume ();
	} // end resume

	public void setWatcher (String name, SoundsWatcher sw)
	/* Set up a watcher for the clip. It will be notified when
       the clip loops or stops.
     */
	{
		ClipInfo ci = (ClipInfo) clipsMap.get (name);
		if (ci == null)
			System.out.println ("Error: " + name + "not stored");
		
		else
			ci.setWatcher (sw);
	} // end setWatcher
}  // end ClipsLoader