package com.mygdx.main.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

class Background {

    private Model model;
    ModelInstance modelInstance;

    Vector3 cood;

    Background(Array<ModelInstance> instances) {
        cood = new Vector3(0, 0, 0);

        ObjLoader objLoader = new ObjLoader();
        model = objLoader.loadModel(Gdx.files.internal("spacesphere.obj"));
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.node("panorama", model);
        model = modelBuilder.end();
        modelInstance = new ModelInstance(model);
        modelInstance.transform.translate(cood);
        instances.add(modelInstance);
    }

}
