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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

class CraneJib {

    GameObject gameObject, carGameObject;

    Vector3 size;
    Vector3 orgCood, cood;
    Vector3 ang;
    Vector3 tmp = new Vector3(), tmp2 = new Vector3();
    Quaternion quaternion = new Quaternion();
    float maxAngle = 60, minAngle = 0;

    Trolley trolley;

    btDynamicsWorld dynamicsWorld;
    Array<ModelInstance> instances;

    CraneJib(Vector3 _cood, Vector3 _org, Vector3 _size, Vector3 _ang, GameObject _gobj, Array<ModelInstance> instances, btSoftRigidDynamicsWorld dynamicsWorld, ArrayMap<String, GameObject.Constructor> constructors) {
        cood = _cood;
        orgCood = _org;
        size = _size;
        ang = _ang;
        carGameObject = _gobj;
        cood.rotate(Vector3.X, ang.x);
        this.dynamicsWorld = dynamicsWorld;
        this.instances = instances;

        ModelBuilder modelBuilder = new ModelBuilder();

        modelBuilder.begin();
        //"Crane Jib" init
        modelBuilder.node().id = "crane jib";
        modelBuilder.part("crane jib", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(new Color(0xAAAA00AA))))
                .box(size.x, size.y, size.z);
        Model model = modelBuilder.end();

        //set physic
        GameObject.Constructor constructor = new GameObject.Constructor(model, "crane jib",
                new btBoxShape(new Vector3(size.x / 2, size.y / 2, size.z / 2)), 200f);
        constructors.put("crane jib", constructor);
        carGameObject.transform.getTranslation(tmp);
        gameObject = constructor.construct(tmp.add(cood).add(orgCood), false, instances, dynamicsWorld);

        trolley = new Trolley(new Vector3(0, size.y, 0), new Vector3(orgCood.x, orgCood.y, orgCood.z - 2), new Vector3(2, 2, 2), new Vector3(ang), carGameObject, instances, dynamicsWorld, constructors);
    }

    void rotateX(float angle) {
        if (ang.x + angle < minAngle) angle = minAngle - ang.x;
        else if (ang.x + angle > maxAngle) angle = maxAngle - ang.x;
        ang.x += angle;
        cood.rotate(Vector3.X, angle);
        trolley.rotateX(angle);
    }

    //static object as a part need follow body object
    //need calculate transform
    void render() {
        carGameObject.transform.getTranslation(tmp);
        carGameObject.transform.getRotation(quaternion);
        tmp2.set(cood).mul(quaternion);
        gameObject.transform.set(quaternion);
        gameObject.transform.rotate(Vector3.X, ang.x);
        gameObject.transform.trn(tmp.add(tmp2).add(orgCood));
        gameObject.body.proceedToTransform(gameObject.transform);

        trolley.render();
    }

}
