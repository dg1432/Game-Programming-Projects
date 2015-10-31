/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 4 - Space Jelly v.2
 */
package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.util.TangentBinormalGenerator;
import java.util.Random;

public class Opponent implements AnimEventListener
{
    private boolean hasBall;
    
    private RigidBodyControl opponentControl;
    private Node opponentModel;
    
    private AnimControl animControl;
    private AnimChannel walkChannel;
    private AnimChannel blockChannel;
    
    private int opponentScore;
    
    private Random r;
    
    private boolean hasPickedShootingSpot;
    private boolean hasShot;
    private boolean isShooting;
    private Vector3f shootingSpot = new Vector3f (0, 0, 0);
    
    public Opponent (AssetManager assetManager, BulletAppState bulletAppState, Node rootNode)
    {
        hasBall = false;
        hasShot = false;
        hasPickedShootingSpot = false;
        isShooting = false;
        
        opponentScore = 0;
        r = new Random ();
        CapsuleCollisionShape capsule = new CapsuleCollisionShape (2f, 4f);
        opponentControl = new RigidBodyControl (capsule, 100f);
        opponentModel = (Node) assetManager.loadModel ("Models/Oto/Oto.mesh.xml");
        opponentModel.setLocalScale (1.8f);
        opponentModel.setLocalTranslation (0, 9, -20);
        
        // Shadow stuff
        opponentModel.setShadowMode (RenderQueue.ShadowMode.CastAndReceive);
        TangentBinormalGenerator.generate (opponentModel);
        
        opponentControl.setPhysicsLocation (new Vector3f (0, -5, -10));
        bulletAppState.getPhysicsSpace ().add (opponentControl);
        rootNode.attachChild (opponentModel);
        
        animControl = opponentModel.getControl (AnimControl.class);
        animControl.addListener (this);
        walkChannel = animControl.createChannel ();
        blockChannel = animControl.createChannel ();
        
        walkChannel.setAnim ("stand");
        
        // Blocking only uses the arms of Oto
        blockChannel.addBone (animControl.getSkeleton ().getBone ("uparm.right"));
        blockChannel.addBone (animControl.getSkeleton ().getBone ("arm.right"));
        blockChannel.addBone (animControl.getSkeleton ().getBone ("hand.right"));
        blockChannel.addBone (animControl.getSkeleton ().getBone ("uparm.left"));
        blockChannel.addBone (animControl.getSkeleton ().getBone ("arm.left"));
        blockChannel.addBone (animControl.getSkeleton ().getBone ("hand.left"));
    } // end constructor
    
    public void move (BulletAppState bulletAppState, Player player, Ball ball, RigidBodyControl ballControl)
    {
        opponentControl.setPhysicsLocation (new Vector3f (opponentModel.getLocalTranslation ().x,
                                            14, opponentModel.getLocalTranslation().z));
        bulletAppState.getPhysicsSpace ().add (opponentControl);
        
        float speed = 0.02f;
        Vector3f currPos = opponentModel.getLocalTranslation ();
        Vector3f destPos = shootingSpot;
        
        if (!hasBall)
        {
            // Go toward player if they have the ball
            if (player.getHasBall ())
                destPos = player.getControl ().getPhysicsLocation ();
        
            // Go toward the ball if noone has it
            else
                destPos = ballControl.getPhysicsLocation ();
        } // end if
        
        // Go toward the shooting spot if the opponent has the ball
        else if (hasBall && !hasPickedShootingSpot)
        {
            shootingSpot = new Vector3f (0 - r.nextInt (45), 14, r.nextInt (90) - 45);
            destPos = shootingSpot;
            hasPickedShootingSpot = true;
        } // end else
        
        Vector3f directionOfTravel = new Vector3f (destPos.x - currPos.x, 14, destPos.z - currPos.z);
        
        directionOfTravel.normalize ();
        
        if (directionOfTravel.distance (currPos) > 2)
            opponentModel.move (directionOfTravel.x * speed, 0, directionOfTravel.z * speed);
        
        // Keep the opponent from walking off the edge
        if (currPos.x < -59)
            currPos.setX (-59);
            
        if (currPos.x > 59)
            currPos.setX (59);
            
        if (currPos.z < -59)
            currPos.setZ (-59);
            
        if (currPos.z > 59)
            currPos.setZ (59);
    } // end move
    
    public void shoot ()
    {
        blockChannel.setAnim ("pull", 0.1f);
        blockChannel.setLoopMode (LoopMode.DontLoop);
        hasPickedShootingSpot = false;
        shootingSpot = new Vector3f (0, 0, 0);
        hasShot = true;
        hasBall = false;
    } // end shoot
    
    public boolean getHasBall ()
    {
        return hasBall;
    } // end playerHasBall
    
    public void setHasBall (boolean hB)
    {
        hasBall = hB;
    } // end setHasBall
    
    public boolean getIsShooting ()
    {
        return isShooting;
    } // end playerHasBall
    
    public void setIsShooting (boolean iS)
    {
        isShooting = iS;
    } // end setHasBall
    
    public int getScore ()
    {
        return opponentScore;
    } // end getOpponentScore
    
    public void setScore (int oS)
    {
        opponentScore = oS;
    } // end setOpponentScore
    
    public boolean getHasShot ()
    {
        return hasShot;
    } // end getOpponentScore
    
    public void setHasShot (boolean hS)
    {
        hasShot = hS;
    } // end setOpponentScore
    
    public Node getModel ()
    {
        return opponentModel;
    } // end getOpponentModel
    
    public RigidBodyControl getControl ()
    {
        return opponentControl;
    } // end getOpponentControl
    
    public AnimChannel getBlockChannel ()
    {
        return blockChannel;
    } // end getBlockChannel
    
    public AnimChannel getWalkChannel ()
    {
        return walkChannel;
    } // end getWalkChannel
    
    public Vector3f getShootingSpot ()
    {
        return shootingSpot;
    } // end getShootingSpot
    
    // These do nothing
    public void onAnimCycleDone (AnimControl control, AnimChannel channel, String animName) {}
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {}
} // end Opponent