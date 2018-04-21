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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

class CraneCabin {

    private Model model;
    GameObject gameObject, carGameObject;

    Vector3 cood;
    Vector3 size;
    Vector3 ang;
    Vector3 tmp = new Vector3();

    CraneCabin(Vector3 _cood, Vector3 _size, Vector3 _ang, GameObject _gameObject, Array<ModelInstance> instances, btDynamicsWorld dynamicsWorld, ArrayMap<String, GameObject.Constructor> constructors) {
        cood = _cood;
        size = _size;
        ang = _ang;
        carGameObject = _gameObject;

        ModelBuilder modelBuilder = new ModelBuilder();
        //glass material
        Material material = new Material(
                new BlendingAttribute(0.4f),
                new FloatAttribute(FloatAttribute.AlphaTest, 0f));

        modelBuilder.begin();
        //"Crane cabin" init
        float glassSize = size.y * 0.4f;
        float thickness = size.y / 5;
        Vector3[] partCood = new Vector3[9];
        Vector3[] partSize = new Vector3[9];
        MeshPartBuilder meshpart;
        //glass
        //below
        partSize[0] = new Vector3(size.x, glassSize, thickness);
        partCood[0] = new Vector3(0, size.y / 2 - glassSize / 2, -size.z / 2 + thickness / 2);
        //left
        partSize[1] = new Vector3(thickness, glassSize - thickness, size.z - thickness * 2);
        partCood[1] = new Vector3(-size.x / 2 + thickness / 2, size.y / 2 - glassSize / 2 - thickness / 2, 0);
        //front
        partSize[2] = new Vector3(size.x, thickness, size.z - thickness * 2);
        partCood[2] = new Vector3(0, size.y / 2 - thickness / 2, 0);
        //right
        partSize[3] = new Vector3(thickness, glassSize - thickness, size.z - thickness * 2);
        partCood[3] = new Vector3(size.x / 2 - thickness / 2, size.y / 2 -glassSize / 2 - thickness / 2, 0);
        //cabin
        //below
        partSize[4] = new Vector3(size.x, size.y - glassSize, thickness);
        partCood[4] = new Vector3(0, -glassSize / 2, -size.z / 2 + thickness / 2);
        //left
        partSize[5] = new Vector3(thickness, size.y - glassSize - thickness, size.z - thickness * 2);
        partCood[5] = new Vector3(-size.x / 2 + thickness / 2, -glassSize / 2 + thickness / 2, 0);
        //right
        partSize[6] = new Vector3(thickness, size.y - glassSize - thickness, size.z - thickness * 2);
        partCood[6] = new Vector3(size.x / 2 - thickness / 2, -glassSize / 2 + thickness / 2, 0);
        //back
        partSize[7] = new Vector3(size.x, thickness, size.z - thickness * 2);
        partCood[7] = new Vector3(0, -size.y / 2 + thickness / 2, 0);
        //above
        partSize[8] = new Vector3(size.x, size.y, thickness);
        partCood[8] = new Vector3(0, 0, size.z / 2 - thickness / 2);

        for (int i = 0; i < 9; i++) {
            if (i < 4) {
                modelBuilder.node().id = "crane cabin";
                meshpart = modelBuilder.part("crane cabin", GL20.GL_TRIANGLES,
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                        material);
                meshpart.setVertexTransform(new Matrix4().translate(partCood[i]));
                meshpart.box(partSize[i].x, partSize[i].y, partSize[i].z);
            }
            else {
                modelBuilder.node().id = "crane cabin";
                meshpart = modelBuilder.part("crane cabin",
                        GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                        new Material(ColorAttribute.createDiffuse(new Color(0xAAAA))));
                meshpart.setVertexTransform(new Matrix4().translate(partCood[i]));
                meshpart.box(partSize[i].x, partSize[i].y, partSize[i].z);
            }
        }
        model = modelBuilder.end();

        //set physic
        GameObject.Constructor constructor;
        constructor = new GameObject.Constructor(model, "crane cabin",
                new btBoxShape(new Vector3(size.x / 2, size.y / 2, size.z / 2)), 100f);
        constructors.put("crane cabin", constructor);
        carGameObject.transform.getTranslation(tmp);
        gameObject = constructor.construct(tmp.add(cood), false, instances, dynamicsWorld);
    }

    void render() {
        gameObject.transform.set(carGameObject.transform).rotate(Vector3.Z, ang.z).trn(cood);
        gameObject.body.proceedToTransform(gameObject.transform);
    }

    void rotate(float angle) {
        ang.z = (360 + ang.z + angle) % 360;
    }

}
