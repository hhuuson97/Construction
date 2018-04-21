package com.mygdx.main.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

class Trolley {

    static final int MOVE_UP = 1;
    static final int MOVE_DOWN = -1;

    private Model model;
    GameObject gameObject, carGameObject;

    Vector3 size;
    Vector3 cood, orgCood;
    Vector3 ang;
    Vector3 tmp = new Vector3(), tmp2 = new Vector3();
    Quaternion quaternion = new Quaternion();

    Rope rope;

    btDynamicsWorld dynamicsWorld;
    Array<ModelInstance> instances;

    Trolley(Vector3 _cood, Vector3 _org, Vector3 _size, Vector3 _ang, GameObject _gobj, Array<ModelInstance> instances, btSoftRigidDynamicsWorld dynamicsWorld, ArrayMap<String, GameObject.Constructor> constructors) {
        cood = _cood;
        orgCood = _org;
        size = _size;
        ang = _ang;
        carGameObject = _gobj;
        cood.rotate(Vector3.X, ang.x);
        cood.rotate(Vector3.Y, ang.y);
        cood.rotate(Vector3.Z, ang.z);
        this.dynamicsWorld = dynamicsWorld;
        this.instances = instances;

        ModelBuilder modelBuilder = new ModelBuilder();

        modelBuilder.begin();
        //"Trolley" init
        modelBuilder.node().id = "trolley";
        modelBuilder.part("trolley", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(new Color(0xAAAA00AA))))
                .box(size.x, size.y, size.z);
        model = modelBuilder.end();

        //set physic
        GameObject.Constructor constructor = new GameObject.Constructor(model, "trolley",
                new btBoxShape(new Vector3(size.x / 2, size.y / 2, size.z / 2)), 0f);
        constructors.put("trolley", constructor);
        carGameObject.transform.getTranslation(tmp);
        gameObject = constructor.construct(tmp.add(cood).add(orgCood), true, instances, dynamicsWorld);

        gameObject.transform.getTranslation(tmp);
        rope = new Rope(10, tmp, instances, dynamicsWorld, constructors);
        float max = rope.rope.getNode(0).getX().getZ();
        for (int i = 1; i < rope.vertCount; i++)
            if (max < rope.rope.getNode(i).getX().getZ()) max = rope.rope.getNode(i).getX().getZ();
        for (int i = 0; i < rope.vertCount; i++)
            if (max == rope.rope.getNode(i).getX().getZ()) rope.rope.appendAnchor(i, gameObject.body);

        float min = rope.rope.getNode(0).getX().getZ();
        for (int i = 1; i < rope.vertCount; i++)
            if (min > rope.rope.getNode(i).getX().getZ()) min = rope.rope.getNode(i).getX().getZ();
        int d = 0;
        for (int i = 0; i < rope.vertCount; i++)
            if (min == rope.rope.getNode(i).getX().getZ()) rope.rope.appendAnchor(i, rope.hookBlock.gameObject.body);
    }

    void moveRope(int direction, float amount) {
        switch (direction) {
            case MOVE_UP:
                rope.curLength -= amount / rope.size.z;
                rope.curLength = Math.max(1, rope.curLength);
                break;
            case MOVE_DOWN:
                rope.curLength += amount / rope.size.z;
                rope.curLength = Math.min(160, rope.curLength);
                break;
        }
        if (rope.curLength > 1 && rope.curLength < 160) rope.reset();
    }

    //static object as a part need follow body object
    //need calculate transform
    void render() {
        carGameObject.transform.getTranslation(tmp);
        carGameObject.transform.getRotation(quaternion);
        tmp2.set(cood).mul(quaternion);
        gameObject.transform.set(quaternion);
        gameObject.transform.rotate(Vector3.Z, ang.z);
        gameObject.transform.rotate(Vector3.X, ang.x);
        gameObject.transform.trn(tmp.add(tmp2).add(orgCood));
        gameObject.body.proceedToTransform(gameObject.transform);

        //fix length ~ curLength
        rope.render();
    }

    void rotateX(float angle) {
        cood.rotate(Vector3.X, angle);
    }

}
