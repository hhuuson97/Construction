package com.mygdx.main.desktop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

class Car {

    private Model model;
    GameObject gameObject;

    Vector3[] partCood;
    Vector3[] partSize;
    float height;

    CraneJib craneJib;
    CraneCabin craneCabin;

    Car(Vector3 _cood, Array<ModelInstance> instances, btSoftRigidDynamicsWorld dynamicsWorld, ArrayMap<String, GameObject.Constructor> constructors) {

        Vector3 size = new Vector3(5, 15, 5);
        Vector3 cood = _cood;
        Vector3 cabinSize = new Vector3(5, 5, 5);
        height = 1.2f;
        Vector3 cabinCood = new Vector3(0, _cood.y + size.y / 2, height / 2);

        //glass material
        Material material = new Material(
                new BlendingAttribute(0.4f),
                new FloatAttribute(FloatAttribute.AlphaTest, 0f));

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        MeshPartBuilder meshpart;
        //"Counter Jib" init
        modelBuilder.node().id = "car";
        meshpart = modelBuilder.part("car", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(new Color(0xAA00AAAA))));
        meshpart.setVertexTransform(new Matrix4().translate(0, -cabinSize.y / 2, height / 2));
        meshpart.box(size.x, size.y, size.z);

        //"Crane cabin" init
        float glassSize = cabinSize.y * 0.4f;
        float thickness = cabinSize.y / 5;
        partCood = new Vector3[9];
        partSize = new Vector3[9];
        //glass
        //below
        partSize[0] = new Vector3(cabinSize.x, glassSize, thickness);
        partCood[0] = new Vector3(cabinCood.x, cabinCood.y + cabinSize.y / 2 - glassSize / 2, cabinCood.z - cabinSize.z / 2 + thickness / 2);
        //left
        partSize[1] = new Vector3(thickness, glassSize - thickness, cabinSize.z - thickness * 2);
        partCood[1] = new Vector3(cabinCood.x - cabinSize.x / 2 + thickness / 2, cabinCood.y + cabinSize.y / 2 - glassSize / 2 - thickness / 2, cabinCood.z);
        //front
        partSize[2] = new Vector3(cabinSize.x, thickness, cabinSize.z - thickness * 2);
        partCood[2] = new Vector3(cabinCood.x, cabinCood.y + cabinSize.y / 2 - thickness / 2, cabinCood.z);
        //right
        partSize[3] = new Vector3(thickness, glassSize - thickness, cabinSize.z - thickness * 2);
        partCood[3] = new Vector3(cabinCood.x + cabinSize.x / 2 - thickness / 2, cabinCood.y + cabinSize.y / 2 - glassSize / 2 - thickness / 2, cabinCood.z);
        //cabin
        //below
        partSize[4] = new Vector3(cabinSize.x, cabinSize.y - glassSize, thickness);
        partCood[4] = new Vector3(cabinCood.x, cabinCood.y - glassSize / 2, cabinCood.z - cabinSize.z / 2 + thickness / 2);
        //left
        partSize[5] = new Vector3(thickness, cabinSize.y - glassSize - thickness, cabinSize.z - thickness * 2);
        partCood[5] = new Vector3(cabinCood.x - cabinSize.x / 2 + thickness / 2, cabinCood.y - glassSize / 2 + thickness / 2, cabinCood.z);
        //right
        partSize[6] = new Vector3(thickness, cabinSize.y - glassSize - thickness, cabinSize.z - thickness * 2);
        partCood[6] = new Vector3(cabinCood.x + cabinSize.x / 2 - thickness / 2, cabinCood.y - glassSize / 2 + thickness / 2, cabinCood.z);
        //back
        partSize[7] = new Vector3(cabinSize.x, thickness, cabinSize.z - thickness * 2);
        partCood[7] = new Vector3(cabinCood.x, cabinCood.y - cabinSize.y / 2 + thickness / 2, cabinCood.z);
        //above
        partSize[8] = new Vector3(cabinSize.x, cabinSize.y, thickness);
        partCood[8] = new Vector3(cabinCood.x, cabinCood.y, cabinCood.z + cabinSize.z / 2 - thickness / 2);

        //make "cabin"
        for (int i = 0; i < 9; i++) {
            if (i > 0 && i < 4) {
                modelBuilder.node().id = "car";
                meshpart = modelBuilder.part("car", GL20.GL_TRIANGLES,
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                        material);
                meshpart.setVertexTransform(new Matrix4().translate(partCood[i]));
                meshpart.box(partSize[i].x, partSize[i].y, partSize[i].z);
            } else {
                modelBuilder.node().id = "car";
                meshpart = modelBuilder.part("car",
                        GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                        new Material(ColorAttribute.createDiffuse(new Color(0xAAAA))));
                meshpart.setVertexTransform(new Matrix4().translate(partCood[i]));
                meshpart.box(partSize[i].x, partSize[i].y, partSize[i].z);
            }
        }

        model = modelBuilder.end();

        modelBuilder.begin();
        modelBuilder.node("car", model);
        model = modelBuilder.end();

        //set physic
        GameObject.Constructor constructor = new GameObject.Constructor(model, "car",
                new btBoxShape(new Vector3(size.x / 2, size.y / 2 + cabinSize.y / 2, size.z / 2 + height / 2)), 200f);
        constructors.put("car", constructor);
        gameObject = constructor.construct(cood, true, instances, dynamicsWorld);

        craneCabin = new CraneCabin(new Vector3(0, 0, size.z / 2 + height / 2 + 2.5f), new Vector3(5, 5, 5),
                new Vector3(0, 0, 0), gameObject, instances, dynamicsWorld, constructors);
        craneJib = new CraneJib(new Vector3(0, 15, 0), new Vector3(0, 0, craneCabin.size.z / 2 + 1),
                new Vector3(2, 30, 2), new Vector3(0, 0, 0), craneCabin.gameObject, instances, dynamicsWorld, constructors);
    }

    void rotateZ(float angle) {
        craneCabin.rotate(angle);
    }

    void rotateX(float angle) {
        craneJib.rotateX(angle);
    }

    void render() {
        craneJib.render();
        craneCabin.render();
    }

}
