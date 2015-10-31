/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 4 - Space Jelly v.2
 * 
 * Controls:
 * W - Forward
 * A - Strafe left
 * S - Move backward
 * D - Strafe right
 * Spacebar - Jump
 * Left click - Shoot ball
 * R - Restart game
 */

package mygame;

import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.jme3.shadow.PssmShadowFilter;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.ui.Picture;

public class Main extends SimpleApplication implements ActionListener
{
    private AudioNode bgMusic;
    
    private BulletAppState bulletAppState;
    
    private RigidBodyControl landscapeControl;
    
    private AmbientLight ambient;
    private DirectionalLight sun;
    
    private boolean left, right, up, down;
    
    private PssmShadowRenderer pssmRenderer;
    private PssmShadowFilter pssmFilter;
    
    private Player player;
    private Opponent opponent;
    private Ball ball;
    
    private Goal playerGoal;
    private Goal opponentGoal;
    
    public static void main (String [] args)
    {
        Main app = new Main ();
        app.start ();
    } // end main
    
    @Override
    public void simpleInitApp ()
    {
        bulletAppState = new BulletAppState ();
        stateManager.attach (bulletAppState);
        
        initializeEnvironment ();
        initializeKeys ();
        
        player = new Player (bulletAppState);
        initializeOpponent ();
        
        initializeGoals ();
        
        initializeBall ();
        ball.getGeom ().setLocalTranslation (0, 10, 0);
        
        initializeAudio ();
    } // end simpleInitApp

    public void initializeEnvironment ()
    {
        // Initialize the skybox
        Spatial sky = assetManager.loadModel ("Scenes/Space.j3o");
        rootNode.attachChild (sky);
        
        // Initialize the terrain
        Spatial terrain = assetManager.loadModel ("Scenes/Ground.j3o");
        landscapeControl = new RigidBodyControl (0);
        terrain.addControl (landscapeControl);
        terrain.setShadowMode (RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild (terrain);
        bulletAppState.getPhysicsSpace ().add (landscapeControl);
        landscapeControl.setRestitution (0.8f);
        
        // Initialize the ambient lighting
        ambient = new AmbientLight ();
        ambient.setColor (ColorRGBA.White.mult (3.8f));
        rootNode.addLight (ambient);
        
        // Initialize the directional lighting
        sun = new DirectionalLight ();
        sun.setColor (ColorRGBA.Orange.mult (0.1f));
        sun.setDirection (new Vector3f (2, -1, 1));
        rootNode.addLight (sun);
        
        // Initialize shadows
        pssmRenderer = new PssmShadowRenderer (assetManager, 1024, 3);
        pssmRenderer.setDirection (sun.getDirection ().normalizeLocal ());
        pssmRenderer.setLambda (0.55f);
        pssmRenderer.setShadowIntensity (0.6f);
        pssmRenderer.setCompareMode (PssmShadowRenderer.CompareMode.Software);
        pssmRenderer.setFilterMode (PssmShadowRenderer.FilterMode.Dither);
        viewPort.addProcessor (pssmRenderer);
        
        pssmFilter = new PssmShadowFilter (assetManager, 1024, 3);
        pssmRenderer.setDirection (sun.getDirection ().normalizeLocal ());
        pssmFilter.setLambda (0.55f);
        pssmFilter.setShadowIntensity (0.6f);
        pssmFilter.setCompareMode (PssmShadowRenderer.CompareMode.Software);
        pssmFilter.setFilterMode (PssmShadowRenderer.FilterMode.Dither);
        pssmFilter.setEnabled (false);
        
        FilterPostProcessor fpp = new FilterPostProcessor (assetManager);
        fpp.addFilter (pssmFilter);
        viewPort.addProcessor (fpp);
    } // end initializeEnvironment
    
    public void initializeKeys ()
    {
        inputManager.addMapping ("Up", new KeyTrigger (KeyInput.KEY_W));
        inputManager.addMapping ("Left", new KeyTrigger (KeyInput.KEY_A));
        inputManager.addMapping ("Down", new KeyTrigger (KeyInput.KEY_S));
        inputManager.addMapping ("Right", new KeyTrigger (KeyInput.KEY_D));
        inputManager.addMapping ("Jump", new KeyTrigger (KeyInput.KEY_SPACE));
        inputManager.addMapping ("Restart", new KeyTrigger (KeyInput.KEY_R));
        inputManager.addMapping ("Shoot", new MouseButtonTrigger (MouseInput.BUTTON_LEFT));
        
        inputManager.addListener (this, "Up");
        inputManager.addListener (this, "Left");
        inputManager.addListener (this, "Down");
        inputManager.addListener (this, "Right");
        inputManager.addListener (this, "Jump");
        inputManager.addListener (this, "Restart");
        inputManager.addListener (this, "Shoot");
    } // end initializeKeys

    public void onAction (String name, boolean isPressed, float tpf)
    {
        // Keyboard inputs
        if (name.equals ("Up"))
            up = isPressed;
        
        if (name.equals ("Left"))
            left = isPressed;
        
        if (name.equals ("Down"))
            down = isPressed;
        
        if (name.equals ("Right"))
            right = isPressed;
        
        if (name.equals ("Jump"))
            player.getControl ().jump ();
        
        if (name.equals ("Restart"))
        {
            // Destroy ALL the things!
            rootNode.removeLight (ambient);
            rootNode.removeLight (sun);
            rootNode.detachAllChildren ();
            guiNode.detachAllChildren ();
            bgMusic.stop ();
            
            // Then restart the game
            simpleInitApp ();
        } // end if
        
        // Mouse input
        if (name.equals ("Shoot") && !isPressed && player.getHasBall ())
        {
            initializeBall ();
        
            // Player no longer has the ball
            player.setHasBall (false);
            
            // Opponent plays block animation
            opponent.getBlockChannel ().setAnim ("pull", 0.1f);
            opponent.getBlockChannel ().setLoopMode (LoopMode.DontLoop);
        } // end if
    } // end onAction
    
    public void initializeOpponent ()
    {
        opponent = new Opponent (assetManager, bulletAppState, rootNode);
    } // end initializeOpponent
    
    public void initializeBall ()
    {
        ball = new Ball (assetManager, rootNode, bulletAppState, player,
                         opponent, cam, opponentGoal.getControl ());
    } // end initializeBall
    
    public void initializeGoals ()
    {
        playerGoal = new Goal (0, assetManager, rootNode);
        opponentGoal = new Goal (1, assetManager, rootNode);
    } // end initializeGoal
    
    public void initializeAudio ()
    {
        // Play the intro to the best basketball in space movie
        bgMusic = new AudioNode (assetManager, "Sounds/SpaceJam_1.ogg", true);
        bgMusic.setPositional (false);
        rootNode.attachChild (bgMusic);
        bgMusic.play ();
    } // end initializeAudio
    
    public void simpleUpdate (float tpf)
    {
        player.move (cam, up, left, down, right);
        opponent.move (bulletAppState, player, ball, ball.getControl ());
        drawInterface ();
        loopAudio ();
        updateAnimation ();
        updateOpponentRotation ();
        
        playerGoal.getGeom ().rotate (0, 5 * tpf, 0);
        opponentGoal.getGeom ().rotate (0, 5 * tpf, 0);
        
        // Player picks up ball
        if (player.getControl ().getPhysicsLocation ().distance
           (ball.getGeom ().getLocalTranslation ()) < 6)
        {
            destroyBall ();
            player.setHasBall (true);
        } // end if
        
        // Opponent picks up ball
        if (opponent.getControl ().getPhysicsLocation ().distance
           (ball.getGeom ().getLocalTranslation ()) < 14)
        {
            destroyBall ();
            opponent.setHasBall (true);
            opponent.setHasShot (false);
        } // end if
        
        // Despawn the ball and respawn it back in the center of the map if it falls off
        if (ball.getControl ().getPhysicsLocation ().getY () < 0)
        {
            destroyBall ();
            initializeBall ();
            ball.getGeom ().setLocalTranslation (0, 10, 0);
        } // end if
        
        // The player gets a point if the ball is in range of the goal
        if (ball.getGeom ().getLocalTranslation ().distance
           (playerGoal.getGeom ().getLocalTranslation ()) < 5)
        {
            destroyBall ();
            initializeBall ();
            ball.getGeom ().setLocalTranslation (0, 10, 0);
            
            destroyGoal ();
            initializeGoals ();
            player.setScore (player.getScore () + 1);
        } // end if
        
        // The opponent gets a point if the ball is in range of the goal
        if (ball.getGeom ().getLocalTranslation ().distance
           (opponentGoal.getGeom ().getLocalTranslation ()) < 5)
        {
            destroyBall ();
            initializeBall ();
            ball.getGeom ().setLocalTranslation (0, 10, 0);
            
            destroyGoal ();
            initializeGoals ();
            opponent.setScore (opponent.getScore () + 1);
        } // end if
    } // end simpleUpdate
    
    public void drawInterface ()
    {
        guiNode.detachAllChildren ();
        
        BitmapText text = new BitmapText (guiFont, false);
        text.setSize (guiFont.getCharSet ().getRenderedSize () * 2);
        text.setName ("text");
        
        // If the player has fallen off the map, notify them
        if (player.getControl ().getPhysicsLocation ().getY () < 0)
        {
            text.setText ("You fell off the map.\n"
                        + "  Press R to restart.");
            text.setLocalTranslation ((settings.getWidth () / 2) - (text.getLineWidth () / 2),
                                      (settings.getHeight () / 2) + text.getLineHeight (), 0);
            guiNode.attachChild (text);
        } // end if
        
        // If the player performs a wicked sick jump, congraulate them
        else if (player.getControl ().getPhysicsLocation ().getY () > 6)
        {
            text.setText ("WOAH!  Nice jump!");
            text.setLocalTranslation ((settings.getWidth () / 2) - (text.getLineWidth () / 2),
                                      (settings.getHeight () / 2) + text.getLineHeight (), 0);
            guiNode.attachChild (text);
        } // end if
        
        // Draw a nice picture indicating whether the player or opponent have the ball or not
        BitmapText pText = new BitmapText (guiFont, false);
        BitmapText oText = new BitmapText (guiFont, false);
        pText.setSize (guiFont.getCharSet ().getRenderedSize ());
        pText.setText ("Player has ball");
        pText.setLocalTranslation (15, settings.getHeight () - 10, 0);
        guiNode.attachChild (pText);
        oText.setText ("Opponent has ball");
        oText.setLocalTranslation (settings.getWidth () - 150, settings.getHeight () - 10, 0);
        guiNode.attachChild (oText);
        
        Picture bballImg = new Picture ("HUD Picture");
        bballImg.setImage (assetManager, "Materials/BasketballSprite.png", true);
        bballImg.setWidth (settings.getWidth () / 16);
        bballImg.setHeight (settings.getHeight () / 12);
        bballImg.setName ("Bball_sprite");
        
        if (player.getHasBall ())
        {
            bballImg.setPosition (15, settings.getHeight () - (settings.getHeight () / 8) - 10);
            guiNode.attachChild (bballImg);
        } // end if
        
        else if (opponent.getHasBall ())
        {
            bballImg.setPosition (settings.getWidth () - (settings.getWidth () / 12),
                                  settings.getHeight () - (settings.getHeight () / 8) - 10);
            guiNode.attachChild (bballImg);
        } // end else if
        
        // Update the player's displayed score
        BitmapText pScore = new BitmapText (guiFont, false);
        pScore.setSize (guiFont.getCharSet ().getRenderedSize () * 2);
        pScore.setText ("Player score: " + player.getScore ());
        pScore.setLocalTranslation (settings.getWidth () - pScore.getLineWidth () - 5,
                                    pScore.getLineHeight () + 50, 0);
        guiNode.attachChild (pScore);
        
        // Update the opponent's displayed score
        BitmapText oScore = new BitmapText (guiFont, false);
        oScore.setSize (guiFont.getCharSet ().getRenderedSize () * 2);
        oScore.setText ("Opponent score: " + opponent.getScore ());
        oScore.setLocalTranslation (settings.getWidth () - oScore.getLineWidth () - 5,
                                    oScore.getLineHeight () + 5, 0);
        guiNode.attachChild (oScore);
        
        // Draw the crosshairs
        guiFont = assetManager.loadFont ("Interface/Fonts/Default.fnt");
        BitmapText cross = new BitmapText (guiFont, false);
        cross.setSize (guiFont.getCharSet ().getRenderedSize ());
        cross.setText ("+");
        cross.setLocalTranslation ((settings.getWidth () / 2) - cross.getLineWidth (),
                                   (settings.getHeight () / 2) + cross.getLineHeight (), 0);
        guiNode.attachChild (cross);
        
        // Print instructions on the screen
        BitmapText instructions = new BitmapText (guiFont, false);
        instructions.setSize (guiFont.getCharSet ().getRenderedSize ());
        instructions.setText ("WASD-Move    Click-Shoot ball    Space-Jump    R-Restart");
        instructions.setLocalTranslation (5, 20, 0);
        guiNode.attachChild (instructions);
    } // end drawInterface
    
    public void loopAudio ()
    {
        if (bgMusic.getStatus () == AudioSource.Status.Stopped)
        {
            // Continue the best theme from a basketball in space movie
            bgMusic = new AudioNode (assetManager, "Sounds/SpaceJam_2.ogg", true);
            bgMusic.setPositional (false);
            rootNode.attachChild (bgMusic);
            bgMusic.play ();
        } // end if
    } // end loopAudio
    
    public void updateAnimation ()
    {
        // The opponent will stop walking when they are close to the player holding the ball
        // or if they are shooting.
        if (opponent.getControl ().getPhysicsLocation ().distance
           (player.getControl ().getPhysicsLocation ()) < 20 && player.getHasBall () ||
            opponent.getControl ().getPhysicsLocation ().distance (opponent.getShootingSpot ()) < 2)
            opponent.getWalkChannel ().setAnim ("stand");
        
        // The opponent will walk otherwise
        else if (opponent.getWalkChannel ().getAnimationName ().equals ("stand") &&
                (opponent.getControl ().getPhysicsLocation ().distance
                (player.getControl ().getPhysicsLocation ()) >= 20))
            opponent.getWalkChannel ().setAnim ("Walk");
    } // end updateAnimation
    
    public void updateOpponentRotation ()
    {
        // If the player has the ball, the opponent looks toward the player
        if (player.getHasBall ())
        {
            Vector3f tmp = new Vector3f (player.getControl ().getPhysicsLocation ().x, 9,
                                         player.getControl ().getPhysicsLocation ().z);
            opponent.getModel ().lookAt (tmp, Vector3f.UNIT_Y);
        } // end if
        
        // If neither the player nor the opponent have the ball, the opponent looks toward the ball
        else if (!player.getHasBall () && !opponent.getHasBall ())
        {
            Vector3f tmp = new Vector3f (ball.getControl ().getPhysicsLocation ().x, 9,
                                         ball.getControl ().getPhysicsLocation ().z);
            opponent.getModel ().lookAt (tmp, Vector3f.UNIT_Y);
        } // end else if
        
        // If the opponent has the ball and have not reached their shooting position,
        // they look toward where they're going
        else if (opponent.getHasBall () && opponent.getControl ().getPhysicsLocation ().distance
                (opponent.getShootingSpot ()) > 2)
        {
            Vector3f tmp = new Vector3f (opponent.getShootingSpot ().x, 9, opponent.getShootingSpot ().z);
            opponent.getModel ().lookAt (tmp, Vector3f.UNIT_Y);
        } // end else if
        
        // If the opponent has the ball and they have reached their shooting position,
        // they look toward their goal
        else if (opponent.getHasBall () && opponent.getControl ().getPhysicsLocation ().distance
                (opponent.getShootingSpot ()) <= 2)
        {
            Vector3f tmp = new Vector3f (opponentGoal.getControl ().getPhysicsLocation ().x, 9,
                                         opponentGoal.getControl ().getPhysicsLocation ().z);
            opponent.getModel ().lookAt (tmp, Vector3f.UNIT_Y);
            
            // The opponent shoots...and misses!
            if (!opponent.getHasShot ())
            {
                opponent.setIsShooting (true);
                opponent.shoot ();
                initializeBall ();
                opponent.setHasShot (true);
                opponent.setHasBall (false);
                opponent.setIsShooting (false);
            } // end if
        } // end else if
    } // end updateOpponentRotation
    
    public void destroyBall ()
    {
        rootNode.detachChildNamed ("Basketball");
        ball.getControl ().destroy ();
        bulletAppState.getPhysicsSpace ().remove (ball.getControl ());
    } // end destroyBall
    
    public void destroyGoal ()
    {
        // Destroy both goals
        rootNode.detachChildNamed ("Goal");
        rootNode.detachChildNamed ("Goal");
        
        rootNode.detachChildNamed ("Emitter");
    } // end destroyGoal
} // end Main