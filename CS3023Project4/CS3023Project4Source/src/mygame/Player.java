/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 4 - Space Jelly v.2
 */
package mygame;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class Player
{
    private boolean hasBall;
    
    private Vector3f walkDirection = new Vector3f (0, 0, 0);
    
    private CharacterControl playerControl;
    
    private int playerScore;
    
    public Player (BulletAppState bulletAppState)
    {
        hasBall = false;
        playerScore = 0;
        CapsuleCollisionShape pShape = new CapsuleCollisionShape (0.4f, 10.9f);
        playerControl = new CharacterControl (pShape, 1.5f);
        playerControl.setFallSpeed (15);
        playerControl.setGravity (15);
        playerControl.setJumpSpeed (25);
        playerControl.setPhysicsLocation (new Vector3f (0, 6, 15));
        bulletAppState.getPhysicsSpace ().add (playerControl);
    } // end constructor
    
    public void move (Camera cam, boolean up, boolean left, boolean down, boolean right)
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
    } // end move
    
    public boolean getHasBall ()
    {
        return hasBall;
    } // end playerHasBall
    
    public void setHasBall (boolean hB)
    {
        hasBall = hB;
    } // end setHasBall
    
    public int getScore ()
    {
        return playerScore;
    } // end getPlayerScore
    
    public void setScore (int pS)
    {
        playerScore = pS;
    } // end setPlayerScore
    
    public CharacterControl getControl ()
    {
        return playerControl;
    } // end getPlayerControl
} // end Player