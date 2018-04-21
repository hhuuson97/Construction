package com.mygdx.main.desktop;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

class GameObject extends ModelInstance implements Disposable {

    public final btRigidBody body;
    public final MyMotionState motionState;

    public GameObject(Model model, String node, btRigidBody.btRigidBodyConstructionInfo constructionInfo, Vector3 cood) {
        super(model, node);
        transform.trn(cood);
        motionState = new MyMotionState();
        motionState.transform = transform;
        body = new btRigidBody(constructionInfo);
        body.setMotionState(motionState);
    }

    public GameObject(Model model, btRigidBody.btRigidBodyConstructionInfo constructionInfo, Vector3 cood) {
        super(model);
        transform.trn(cood);
        motionState = new MyMotionState();
        motionState.transform = transform;
        body = new btRigidBody(constructionInfo);
        body.setMotionState(motionState);
    }

    @Override
    public void dispose() {
        body.dispose();
        motionState.dispose();
    }

    static class Constructor implements Disposable {

        final short GROUND_FLAG = 1 << 8;
        final short OBJECT_FLAG = 1 << 9;

        public final Model model;
        public final String node;
        public final btCollisionShape shape;
        public final btRigidBody.btRigidBodyConstructionInfo constructionInfo;
        private Vector3 localInertia = new Vector3();

        public Constructor(Model model, String node, btCollisionShape shape, float mass) {
            this.model = model;
            this.node = node;
            this.shape = shape;
            if (mass > 0f)
                shape.calculateLocalInertia(mass, localInertia);
            else
                localInertia.set(0, 0, 0);
            this.constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
        }

        public GameObject construct(Vector3 cood, boolean objectFlag, Array<ModelInstance> instances, btDynamicsWorld dynamicsWorld) {
            GameObject object;
            if (node != null) object = new GameObject(model, node, constructionInfo, cood);
            else object = new GameObject(model, constructionInfo, cood);
            object.body.proceedToTransform(object.transform);
            object.body.setUserValue(instances.size);
            if (objectFlag) {
                object.body.setCollisionFlags(object.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
                object.body.setContactCallbackFlag(OBJECT_FLAG);
                object.body.setContactCallbackFlag(GROUND_FLAG);
            }
            else {
                object.body.setCollisionFlags(object.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
                object.body.setContactCallbackFlag(GROUND_FLAG);
                object.body.setContactCallbackFilter(0);
                object.body.setActivationState(Collision.DISABLE_DEACTIVATION);
            }
            instances.add(object);
            ((btDiscreteDynamicsWorld)dynamicsWorld).addRigidBody(object.body);
            return object;
        }

        @Override
        public void dispose() {
            shape.dispose();
            constructionInfo.dispose();
        }
    }
}