package com.mygdx.main.desktop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

class Ground {

    private Model model;
    GameObject gameObject;

    Vector3 size;
    Vector3 cood;

    Ground(Array<ModelInstance> instances, btDynamicsWorld dynamicsWorld, ArrayMap<String, GameObject.Constructor> constructors) {

        size = new Vector3(500, 500, 1);
        cood = new Vector3(0, 0, -0.5f);

        ModelBuilder modelBuilder = new ModelBuilder();

        modelBuilder.begin();
        //"ground" init, size 500 x 500 x 1
        modelBuilder.node().id = "ground";
        modelBuilder.part("ground", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(new Color(0x548540FF))))
                .box(size.x, size.y, size.z);
        model = modelBuilder.end();

        //set physic
        GameObject.Constructor constructor = new GameObject.Constructor(model, "ground",
                new btBoxShape(new Vector3(size.x / 2, size.y / 2, size.z / 2)), 0f);
        constructors.put("ground", constructor);
        gameObject = constructor.construct(cood, false, instances, dynamicsWorld);
        gameObject.body.setRollingFriction(0.5f);
    }

}
