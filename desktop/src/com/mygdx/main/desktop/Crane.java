package com.mygdx.main.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btGImpactMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray;
import com.badlogic.gdx.physics.bullet.dynamics.btDefaultVehicleRaycaster;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRaycastVehicle;
import com.badlogic.gdx.physics.bullet.dynamics.btVehicleRaycaster;
import com.badlogic.gdx.physics.bullet.dynamics.btWheelInfo;
import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

class Crane {

    GameObject[] wheels;
    Car car;

    btVehicleRaycaster raycaster;
    btRaycastVehicle vehicle;
    btRaycastVehicle.btVehicleTuning tuning;

    float maxForce = 250f;
    float currentForce = 0f;
    float acceleration = 200f; // force/second
    float maxAngle = 30f;
    float currentAngle = 0f;
    float steerSpeed = 45f; // angle/second

    Crane(Vector3 _cood, Array<ModelInstance> instances, btSoftRigidDynamicsWorld dynamicsWorld, ArrayMap<String, GameObject.Constructor> constructors) {

        //parts create
        car = new Car(new Vector3(_cood.x, _cood.y, _cood.z + 3.1f), instances, dynamicsWorld, constructors);
        car.gameObject.body.setActivationState(Collision.DISABLE_DEACTIVATION);

        //vehicle Install
        raycaster = new btDefaultVehicleRaycaster(dynamicsWorld);
        tuning = new btRaycastVehicle.btVehicleTuning();
        vehicle = new btRaycastVehicle(tuning, car.gameObject.body, raycaster);
        dynamicsWorld.addVehicle(vehicle);

        //wheel make
        Vector3 point = new Vector3();
        Vector3 direction = new Vector3(0, 0, -1);
        Vector3 axis = new Vector3(1, 0, 0);
        BoundingBox bounds = new BoundingBox();
        AssetManager assets = new AssetManager();
        assets.load("wheel.g3db", Model.class);
        assets.finishLoading();
        Model wheelModel = assets.get("wheel.g3db", Model.class);
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.node("wheel", wheelModel);
        wheelModel = modelBuilder.end();
        btTriangleIndexVertexArray wheelVertexArray;
        wheelVertexArray = new btTriangleIndexVertexArray(wheelModel.meshParts);
        btGImpactMeshShape wheelShape = new btGImpactMeshShape(wheelVertexArray);
        wheelShape.setLocalScaling(new Vector3(1f, 1f, 1f));
        wheelShape.setMargin(0f);
        //wheelShape.updateBound();
        GameObject.Constructor constructor = new GameObject.Constructor(wheelModel, "wheel",
                wheelShape, 0f);
        constructors.put("wheel", constructor);
        wheels = new GameObject[4];
        for (int i = 0; i < 4; i++) {
            wheels[i] = constructor.construct(new Vector3(0, 0, 0), false, instances, dynamicsWorld);
            //dynamicsWorld.removeRigidBody(wheels[i].body);
        }

        //set wheel physics
        btWheelInfo wheelInfo;
        Vector3 chassisHalfExtents = car.gameObject.model.calculateBoundingBox(bounds).getDimensions(new Vector3()).scl(0.5f);
        Vector3 wheelHalfExtents = wheelModel.calculateBoundingBox(bounds).getDimensions(new Vector3()).scl(0.5f);
        wheelInfo = vehicle.addWheel(point.set(chassisHalfExtents).scl(0.9f, 0.7f, -0.8f), direction, axis,
                wheelHalfExtents.y * 0.3f, wheelHalfExtents.y, tuning, true);
        wheelInfo = vehicle.addWheel(point.set(chassisHalfExtents).scl(-0.9f, 0.7f, -0.8f), direction, axis,
                wheelHalfExtents.y * 0.3f, wheelHalfExtents.y, tuning, true);
        wheelInfo = vehicle.addWheel(point.set(chassisHalfExtents).scl(0.9f, -0.5f, -0.8f), direction, axis,
                wheelHalfExtents.y * 0.3f, wheelHalfExtents.y, tuning, false);
        wheelInfo = vehicle.addWheel(point.set(chassisHalfExtents).scl(-0.9f, -0.5f, -0.8f), direction, axis,
                wheelHalfExtents.y * 0.3f, wheelHalfExtents.y, tuning, false);
    }

    void rotateZ(float ang) {
        car.rotateZ(ang);
    }

    void rotateX(float ang) {
        car.rotateX(ang);
    }

    void run(boolean left, boolean up, boolean right, boolean down, float delta) {
        float angle = currentAngle;
        if (right) {
            angle = MathUtils.clamp(angle - steerSpeed * delta, -maxAngle, 0);
        } else if (left) {
            angle = MathUtils.clamp(angle + steerSpeed * delta, 0, maxAngle);
        } else angle = 0;
        if (angle != currentAngle) {
            currentAngle = angle;
            vehicle.setSteeringValue(angle * MathUtils.degreesToRadians, 0);
            vehicle.setSteeringValue(angle * MathUtils.degreesToRadians, 1);
        }

        float force = currentForce;
        if (up) {
            if (force < 0f) force = 0f;
            force = MathUtils.clamp(force + acceleration * delta, 0f, maxForce);
        } else if (down) {
            if (force > 0f) force = 0f;
            force = MathUtils.clamp(force - acceleration * delta, -maxForce, 0f);
        } else
            force = 0f;
        if (force != currentForce) {
            currentForce = force;
            vehicle.applyEngineForce(force, 2);
            vehicle.applyEngineForce(force, 3);
        }
        for (int i = 0; i < wheels.length; i++) {
            vehicle.updateWheelTransform(i, true);
            vehicle.getWheelInfo(i).getWorldTransform().getOpenGLMatrix(wheels[i].transform.val);
        }
    }

    void render() {
        car.render();
    }

}
