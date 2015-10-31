/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 4 - Space Jelly v.2
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;

public class Ball
{
    private Geometry ballGeom;
    
    private RigidBodyControl ballControl;
    
    public Ball (AssetManager assetManager, Node rootNode, BulletAppState bulletAppState,
                 Player player, Opponent opponent, Camera cam, RigidBodyControl goalControl)
    {
        // Create and texture the ball
        Sphere s = new Sphere (16, 16, 1f);
        ballGeom = new Geometry ("Ball", s);
        s.setTextureMode (Sphere.TextureMode.Projected);
        Material m = new Material (assetManager, "Common/MatDefs/Light/Lighting.j3md");
        m.setTexture ("DiffuseMap", assetManager.loadTexture("Materials/Basketball.jpg"));
        ballGeom.setMaterial (m);
        ballGeom.setName ("Basketball");
        
        // Shadow stuff
        ballGeom.setShadowMode (RenderQueue.ShadowMode.CastAndReceive);
        TangentBinormalGenerator.generate (ballGeom);
        
        rootNode.attachChild (ballGeom);
        ballControl = new RigidBodyControl (0.5f);
        
        // Direction for opponent to shoot ball
        Vector3f tmp = new Vector3f ((goalControl.getPhysicsLocation ().getX () -
                                          opponent.getControl ().getPhysicsLocation ().x / FastMath.PI) - 1,
                                     (goalControl.getPhysicsLocation ().getY () -
                                          opponent.getControl ().getPhysicsLocation ().y / FastMath.PI) - 1,
                                     (goalControl.getPhysicsLocation ().getZ () -
                                          opponent.getControl ().getPhysicsLocation ().z / FastMath.PI));
        
        // Spawn the ball in front of the player if they are about to shoot it
        if (player.getHasBall ())
            ballGeom.setLocalTranslation (cam.getLocation ().addLocal (cam.getDirection ().mult (7)));
        
        // Initial location for ball when opponent is shooting
        else if (opponent.getIsShooting ())
            ballGeom.setLocalTranslation (opponent.getControl ().getPhysicsLocation ().x - 15, 14,
                                          opponent.getControl ().getPhysicsLocation ().z);
        
        ballGeom.addControl (ballControl);
        bulletAppState.getPhysicsSpace ().add (ballControl);
        
        // Shoot the ball in the direction the player is looking if they have it
        if (player.getHasBall ())
            ballControl.setLinearVelocity (cam.getDirection ().multLocal (35));
        
        // Opponent shoots the ball in the direction specified above
        else if (opponent.getIsShooting ())
            ballControl.setLinearVelocity (tmp.multLocal (1.5f));
        
        ballControl.setRestitution (0.75f);
    } // end constructor
    
    public Geometry getGeom ()
    {
        return ballGeom;
    } // end getBallGeom
    
    public RigidBodyControl getControl ()
    {
        return ballControl;
    } // end getBallControl
} // end Ball