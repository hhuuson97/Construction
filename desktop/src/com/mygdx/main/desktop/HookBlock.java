package com.mygdx.main.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btGImpactMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

class HookBlock {

    GameObject gameObject;

    Vector3 cood;
    float weight;

    HookBlock(Vector3 _cood, Array<ModelInstance> instances, btDynamicsWorld dynamicsWorld, ArrayMap<String, GameObject.Constructor> constructors) {
        cood = _cood;
        weight = 200f;

        //"Hook Block" init
        ObjLoader objLoader = new ObjLoader();
        Model hookModel = objLoader.loadModel(Gdx.files.internal("hook.obj"));
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.node("hook", hookModel);
        hookModel = modelBuilder.end();
        btTriangleIndexVertexArray hookVertexArray;
        hookVertexArray = new btTriangleIndexVertexArray(hookModel.meshParts);
        btGImpactMeshShape hookShape = new btGImpactMeshShape(hookVertexArray);
        hookShape.setLocalScaling(new Vector3(1f, 1f, 1f));
        hookShape.setMargin(0f);
        hookShape.updateBound();

        //set physic
        GameObject.Constructor constructor = new GameObject.Constructor(hookModel, "hook", hookShape, weight);
        constructors.put("hook", constructor);
        gameObject = constructor.construct(cood, true, instances, dynamicsWorld);
    }

}
