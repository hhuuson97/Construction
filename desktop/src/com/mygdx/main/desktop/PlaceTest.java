package com.mygdx.main.desktop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

class PlaceTest {

    private Model model;
    GameObject gameObject;

    Vector3 size;
    Vector3 cood;

    PlaceTest(Vector3 _cood, Array<ModelInstance> instances, btDynamicsWorld dynamicsWorld, ArrayMap<String, GameObject.Constructor> constructors) {

        size = new Vector3(10, 10, 2f);
        cood = _cood;

        ModelBuilder modelBuilder = new ModelBuilder();

        modelBuilder.begin();
        //"area" init, size 500 x 500 x 1
        modelBuilder.node().id = "area";
        modelBuilder.part("area", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(new Color(0xAAAAAAFF))))
                .box(size.x, size.y, size.z);
        model = modelBuilder.end();

        //set physic
        GameObject.Constructor constructor = new GameObject.Constructor(model, "area",
                new btBoxShape(new Vector3(size.x / 2, size.y / 2, size.z / 2)), 0f);
        constructors.put("area", constructor);
        gameObject = constructor.construct(cood, false, instances, dynamicsWorld);
    }

}
