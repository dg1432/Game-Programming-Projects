/*
 * David Glover
 * CS 3023 - Intro to Game Programming
 * Spring 2014
 * Project 4 - Space Jelly v.2
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterBoxShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Torus;
import com.jme3.util.TangentBinormalGenerator;

public class Goal
{
    private Geometry goalGeom;
    
    private RigidBodyControl goalControl;
    
    public Goal (int owner, AssetManager assetManager, Node rootNode)
    {
        // 0 -> Player's goal, 1 -> Opponent's goal
        Torus t = new Torus (12, 40, 0.5f, 3.5f);
        goalGeom = new Geometry ("Torus", t);
        Material m = new Material (assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        if (owner == 0) // Player's goal
        {
            m.setColor ("Color", ColorRGBA.Blue);
            goalGeom.setLocalTranslation (55, 25, 0);
        } // end if
        
        else // Opponent's goal
        {
            m.setColor ("Color", ColorRGBA.Red);
            goalGeom.setLocalTranslation (-55, 25, 0);
        } // end else
        
        goalGeom.setMaterial (m);
        
        goalGeom.setShadowMode (RenderQueue.ShadowMode.CastAndReceive);
        TangentBinormalGenerator.generate (goalGeom);
        
        goalControl = new RigidBodyControl (0.5f);
        goalGeom.setName ("Goal");
        
        goalGeom.addControl (goalControl);
        rootNode.attachChild (goalGeom);
        
        // Set up particle effects
        ParticleEmitter emit = new ParticleEmitter ("Emitter", Type.Point, 10000);
        emit.setShape (new EmitterBoxShape (new Vector3f (-2.8f, -2.8f, -2.8f),
                       new Vector3f (2.8f, 2.8f, 2.8f)));
        emit.setLowLife (5);
        emit.setHighLife (15);
        emit.setInitialVelocity (new Vector3f (0, 2, 0));
        emit.setImagesX (15);
        emit.setStartSize (0.15f);
        emit.setEndSize (0.1f);
        
        if (owner == 0) // Player's goal
        {
            emit.setStartColor (ColorRGBA.Cyan);
            emit.setEndColor (ColorRGBA.Blue);
            emit.setLocalTranslation (goalControl.getPhysicsLocation ());
        } // end if
        
        else // Opponent's goal
        {
            emit.setStartColor (ColorRGBA.Yellow);
            emit.setEndColor (ColorRGBA.Red);
            emit.setLocalTranslation (goalControl.getPhysicsLocation ());
        } // end else
        
        emit.setSelectRandomImage (true);
        emit.setParticlesPerSec (40);
        
        Material mat = new Material (assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setBoolean ("PointSprite", true);
        mat.setTexture ("Texture", assetManager.loadTexture ("Effects/Smoke/Smoke.png"));
        emit.setMaterial (mat);

        rootNode.attachChild (emit);
    } // end constructor
    
    public RigidBodyControl getControl ()
    {
        return goalControl;
    } // end getGoalControl
    
    public Geometry getGeom ()
    {
        return goalGeom;
    } // end getGoalGeom
} // end Goal