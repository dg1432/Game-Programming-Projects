package mygame;
/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 3 - Space Jelly
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

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.jme3.ui.Picture;

import java.util.Random;

public class Main extends SimpleApplication implements ActionListener
{
    private final int MAX_HEIGHT = 65;
    
    private AudioNode bgMusic;
    
    private BulletAppState bulletAppState;
    
    private RigidBodyControl landscapeControl;
    private RigidBodyControl ballControl;
    private RigidBodyControl goalControl;
    private CharacterControl playerControl;
    
    private Geometry ball;
    private Geometry goal;
    
    private AmbientLight ambient;
    private DirectionalLight sun;
    
    private Vector3f walkDirection = new Vector3f (0, 0, 0);
    
    private boolean left, right, up, down;
    private boolean playerHasBall;
    
    private int score;
    
    private Random r = new Random ();
    
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
        
        score = 0;
        
        initializeEnvironment ();
        initializeKeys ();
        
        initializePlayer ();
        
        initializeBall ();
        ball.setLocalTranslation (0, 10, 0);
        
        initializeGoal ();
        
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
        sun.setDirection (new Vector3f (1, 0, -2).normalizeLocal ());
        rootNode.addLight (sun);
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
            playerControl.jump ();
        
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
        if (name.equals ("Shoot") && !isPressed && playerHasBall)
        {
            initializeBall ();
        
            // Player no longer has the ball
            playerHasBall = false;
        } // end if
    } // end onAction
    
    public void initializePlayer ()
    {
        playerHasBall = false;
        CapsuleCollisionShape pShape = new CapsuleCollisionShape (0.4f, 10.9f);
        playerControl = new CharacterControl (pShape, 1.5f);
        playerControl.setFallSpeed (10);
        playerControl.setGravity (10);
        playerControl.setJumpSpeed (25);
        playerControl.setPhysicsLocation (new Vector3f (0, 6, 15));
        bulletAppState.getPhysicsSpace ().add (playerControl);
    } // end initializePlayer
    
    public void initializeBall ()
    {
        // Create and texture the ball
        Sphere s = new Sphere (16, 16, 1f);
        ball = new Geometry ("Ball", s);
        s.setTextureMode (Sphere.TextureMode.Projected);
        Material m = new Material (assetManager, "Common/MatDefs/Light/Lighting.j3md");
        m.setTexture ("DiffuseMap", assetManager.loadTexture("Materials/Basketball.jpg"));
        ball.setMaterial (m);
        ball.setName ("Basketball");
        rootNode.attachChild (ball);
        ballControl = new RigidBodyControl (0.5f);
        
        // Spawn the ball in front of the player if they are about to shoot it
        if (playerHasBall)
            ball.setLocalTranslation (cam.getLocation ().addLocal (cam.getDirection ().mult (7)));
        
        ball.addControl (ballControl);
        bulletAppState.getPhysicsSpace ().add (ballControl);
        
        // Shoot the ball in the direction the player is looking if they have it
        if (playerHasBall)
            ballControl.setLinearVelocity (cam.getDirection ().multLocal (35));
        
        ballControl.setRestitution (0.75f);
    } // end initializeBall
    
    public void initializeGoal ()
    {
        Torus t = new Torus (12, 40, 0.5f, 3.5f);
        goal = new Geometry ("Torus", t);
        Material m = new Material (assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor ("Color", ColorRGBA.Red);
        goal.setMaterial (m);
        
        int yHeight = r.nextInt ( 5 + (2 * score)) + score + 10;
        
        // Don't let the goal go any higher than 65
        if (yHeight > MAX_HEIGHT)
            yHeight = MAX_HEIGHT;
        
        goal.setLocalTranslation (r.nextInt (60) - 60, yHeight, r.nextInt (63) - 63);
        goalControl = new RigidBodyControl (0.5f);
        goal.addControl (goalControl);
        goal.setName ("Goal");
        
        rootNode.attachChild (goal);
    } // end initializeGoal
    
    public void initializeAudio ()
    {
        bgMusic = new AudioNode (assetManager, "Sounds/BackgroundMusic.ogg", true);
        bgMusic.setPositional (false);
        rootNode.attachChild (bgMusic);
        bgMusic.play ();
    } // end initializeAudio
    
    @Override
    public void simpleUpdate (float tpf)
    {
        movePlayer ();
        drawInterface ();
        loopAudio ();
        
        // Player picks up ball
        if (playerControl.getPhysicsLocation ().distance (ballControl.getPhysicsLocation ()) < 6)
        {
            destroyBall ();
            playerHasBall = true;
        } // end if
        
        // Despawn the ball and respawn it back in the center of the map if it falls off
        if (ballControl.getPhysicsLocation ().getY () < 0)
        {
            destroyBall ();
            initializeBall ();
            ball.setLocalTranslation (0, 10, 0);
        } // end if
        
        // The player gets a point if the ball is in range of the goal
        if (ballControl.getPhysicsLocation ().distance (goalControl.getPhysicsLocation ()) < 5)
        {
            destroyBall ();
            initializeBall ();
            ball.setLocalTranslation (0, 10, 0);
            
            destroyGoal ();
            initializeGoal ();
            
            score ++;
        } // end if
        
        goal.rotate (0, 5 * tpf, 0);
    } // end simpleUpdate
    
    public void movePlayer ()
    {
        Vector3f camDirection = cam.getDirection ().multLocal (0.35f);
        Vector3f camLeft = cam.getLeft ().multLocal (0.35f);
        
        walkDirection.set (0, 0, 0);
        
        if (up)
            walkDirection.addLocal (camDirection);
        
        if (left)
            walkDirection.addLocal (camLeft);
        
        if (down)
            walkDirection.addLocal (camDirection.negate ());
        
        if (right)
            walkDirection.addLocal (camLeft.negate ());
        
        walkDirection.setY (0);
        playerControl.setWalkDirection (walkDirection);
        
        cam.setLocation (playerControl.getPhysicsLocation ());
    } // end movePlayer
    
    public void drawInterface ()
    {
        guiNode.detachAllChildren ();
        
        BitmapText text = new BitmapText (guiFont, false);
        text.setSize (guiFont.getCharSet ().getRenderedSize () * 2);
        text.setName ("text");
        
        /*
         * If the player has fallen off the map, notify them
         */
        if (playerControl.getPhysicsLocation ().getY () < 0)
        {
            text.setText ("You fell off the map.\n"
                        + "  Press R to restart.");
            text.setLocalTranslation ((settings.getWidth () / 2) - (text.getLineWidth () / 2),
                                      (settings.getHeight () / 2) + text.getLineHeight (), 0);
            guiNode.attachChild (text);
        } // end if
        
         /*
         * If the player performs a wicked sick jump, congraulate them
         */
        else if (playerControl.getPhysicsLocation ().getY () > 6)
        {
            text.setText ("WOAH!  Nice jump!");
            text.setLocalTranslation ((settings.getWidth () / 2) - (text.getLineWidth () / 2),
                                      (settings.getHeight () / 2) + text.getLineHeight (), 0);
            guiNode.attachChild (text);
        } // end if
        
        /*
         * Draw a nice picture indicating whether the player has the ball or not
         */
        if (playerHasBall)
        {
            Picture bballImg = new Picture ("HUD Picture");
            bballImg.setImage (assetManager, "Materials/BasketballSprite.png", true);
            bballImg.setWidth (settings.getWidth () / 12);
            bballImg.setHeight (settings.getHeight () / 8);
            bballImg.setPosition (settings.getWidth () - (settings.getWidth () / 12) - 5,
                                  settings.getHeight () - (settings.getHeight () / 8) - 5);
            bballImg.setName ("Bball_sprite");
            guiNode.attachChild (bballImg);
        } // end if
        
        /*
         * Update the player's displayed score
         */
        guiNode.detachChildNamed ("Score");
        BitmapText pScore = new BitmapText (guiFont, false);
        pScore.setSize (guiFont.getCharSet ().getRenderedSize () * 2);
        pScore.setText ("Score: " + score);
        pScore.setLocalTranslation (settings.getWidth () - pScore.getLineWidth () - 5,
                                    pScore.getLineHeight () + 5, 0);
        pScore.setName ("Score");
        guiNode.attachChild (pScore);
        
        /*
         * Draw the crosshairs
         */
        guiFont = assetManager.loadFont ("Interface/Fonts/Default.fnt");
        BitmapText cross = new BitmapText (guiFont, false);
        cross.setSize (guiFont.getCharSet ().getRenderedSize ());
        cross.setText ("+");
        cross.setLocalTranslation ((settings.getWidth () / 2) - cross.getLineWidth (),
                                   (settings.getHeight () / 2) + cross.getLineHeight (), 0);
        guiNode.attachChild (cross);
        
        /*
         * Print instructions on the screen
         */
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
            bgMusic = new AudioNode (assetManager, "Sounds/BackgroundMusic.ogg", true);
            bgMusic.setPositional (false);
            rootNode.attachChild (bgMusic);
            bgMusic.play ();
        } // end if
    } // end loopAudio
    
    public void destroyBall ()
    {
        rootNode.detachChildNamed ("Basketball");
        bulletAppState.getPhysicsSpace ().remove (ballControl);
    } // end destroyBall
    
    public void destroyGoal ()
    {
        rootNode.detachChildNamed ("Goal");
    } // end destroyGoal
} // end Main