package com.mygdx.main.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3;
import com.badlogic.gdx.physics.bullet.softbody.btSoftBody;
import com.badlogic.gdx.physics.bullet.softbody.btSoftBodyWorldInfo;
import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ShortBuffer;

class Rope {

    Vector3 size = new Vector3(2.5f, 2.5f, 2.5f);
    Vector3 constraint = new Vector3();
    HookBlock hookBlock;
    btSoftBody rope;
    ModelInstance modelInstance;
    btSoftBodyWorldInfo worldInfo;
    Model model;

    int maxLength;
    float curLength, length;

    ShortBuffer indexMap;
    int positionOffset;
    int normalOffset;
    MeshPart meshPart;
    int vertCount;

    Rope(int length, Vector3 _cood, Array<ModelInstance> instances, btSoftRigidDynamicsWorld dynamicsWorld, ArrayMap<String, GameObject.Constructor> constructors) {

        maxLength = 100;
        curLength = this.length = length;

        worldInfo = new btSoftBodyWorldInfo();
        worldInfo.setBroadphase(dynamicsWorld.getBroadphase());
        worldInfo.setDispatcher(dynamicsWorld.getDispatcher());
        worldInfo.setGravity(new btVector3(0, 0, -10));
        worldInfo.getSparsesdf().Initialize();

        ObjLoader objLoader = new ObjLoader();
        model = objLoader.loadModel(Gdx.files.internal("rope.obj"));
        modelInstance = new ModelInstance(model);
        instances.add(modelInstance);

        meshPart = modelInstance.nodes.get(0).parts.get(0).meshPart;
        meshPart.mesh.scale(1, 1, length);
        indexMap = BufferUtils.newShortBuffer(meshPart.size);
        positionOffset = meshPart.mesh.getVertexAttribute(VertexAttributes.Usage.Position).offset;
        normalOffset = meshPart.mesh.getVertexAttribute(VertexAttributes.Usage.Normal).offset;

        rope = new btSoftBody(worldInfo, meshPart.mesh.getVerticesBuffer(), meshPart.mesh.getVertexSize(), positionOffset,
                normalOffset, meshPart.mesh.getIndicesBuffer(), meshPart.offset, meshPart.size, indexMap, 0);
        rope.setUserValue(instances.size);
        rope.setTotalMass(100);
        dynamicsWorld.addSoftBody(rope);
        float tmp = rope.getBounds().getZ();
        modelInstance.transform.trn(new Vector3(_cood.x, _cood.y, _cood.z - 1 + tmp / 2));
        rope.transform(modelInstance.transform);
        vertCount = rope.getNodeCount();

        hookBlock = new HookBlock(new Vector3(_cood.x, _cood.y, _cood.z - 1 + tmp - 2),
                instances, dynamicsWorld, constructors);
    }

    void reset() {
        length = length * (curLength / length);
    }

    void setConstraint(Vector3 pt) {
        constraint.set(pt);
    }

    void connect() {
    }

    void disconnect() {
        //rope.cutLink()
    }

    void render() {
        MeshPart meshPart = modelInstance.nodes.get(0).parts.get(0).meshPart;
        rope.getVertices(meshPart.mesh.getVerticesBuffer(), meshPart.mesh.getVertexSize(), positionOffset, normalOffset,
                meshPart.mesh.getIndicesBuffer(), meshPart.offset, meshPart.size, indexMap, 0);
        rope.getWorldTransform(modelInstance.transform);
    }

}