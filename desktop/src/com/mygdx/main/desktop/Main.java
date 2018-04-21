package com.mygdx.main.desktop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btGImpactCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btManifoldPoint;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.physics.bullet.softbody.btSoftBodyRigidBodyCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

public class Main implements ApplicationListener, InputProcessor {

	final float ROTATE_SPEED = 2;
	final float DROP_SPEED = 5;
	final float MINIMUM_VIEWPORT_SIZE = 160f;
	float center, distance;

	//3d generate
	PerspectiveCamera camera;
	ModelBatch modelBatch;
	SpriteBatch batch;
	static Environment environment;
	static DebugDrawer debugDrawer;

	//3d physics
	static btSoftBodyRigidBodyCollisionConfiguration collisionConfig;
	static btCollisionDispatcher dispatcher;
	static btDbvtBroadphase broadphase;
	static btConstraintSolver constraintSolver;
	static btDynamicsWorld dynamicsWorld;
	static MyContactListener contactListener;
	static int numberWindow = 0;

	//Bitmap Font
	private BitmapFont font;

	//Objects
	static Ground ground;
	static Background background;
	static Crane crane;
	static PlaceTest placeTest;
	static Array<ModelInstance> instances;
	static ArrayMap<String, GameObject.Constructor> constructors;

	//Key event
	//share window control
	public static boolean[] arg = new boolean[11];
	public static boolean touch = false;
	int cabinView = 2;

	static Vector3 ptA = new Vector3(), ptB = new Vector3(), tmp1 = new Vector3(), tmp2 = new Vector3(), tmp = new Vector3();
	static String ans1 = "", ans2 = "";
	static float waiting = 0;

	class MyContactListener extends ContactListener {
		@Override
		public boolean onContactAdded (btManifoldPoint cp, int userValue0, int partId0, int index0, int userValue1, int partId1, int index1) {
			/*if (waiting <= 0) touch = false;
			if (userValue0 == crane.car.craneJib.trolley.rope.rope.getUserValue() &&
                    userValue1 == crane.car.craneJib.trolley.rope.hookBlock.gameObject.body.getUserValue()) {
				touch = true;
				cp.getLocalPointA(ptA);
				cp.getLocalPointB(ptB);
				waiting = 1;
			}
            if (userValue1 == crane.car.craneJib.trolley.rope.rope.getUserValue() &&
                    userValue0 == crane.car.craneJib.trolley.rope.hookBlock.gameObject.body.getUserValue()) {
				touch = true;
				cp.getLocalPointA(ptB);
				cp.getLocalPointB(ptA);
				waiting = 1;
			}
			if (touch || arg[6])
				((ColorAttribute)crane.car.craneJib.trolley.rope.hookBlock.gameObject.materials.get(0).get(ColorAttribute.Diffuse)).color.set(0x0000AAAA);
			else ((ColorAttribute)crane.car.craneJib.trolley.rope.hookBlock.gameObject.materials.get(0).get(ColorAttribute.Diffuse)).color.set(0xAAAA00AA);*/
			return true;
		}

		@Override
		public void onContactProcessed(int userValue0, int userValue1) {
			if (userValue0 == placeTest.gameObject.body.getUserValue() &&
					userValue1 == crane.car.craneJib.trolley.rope.hookBlock.gameObject.body.getUserValue()) {
				placeTest.gameObject.transform.getTranslation(tmp1);
				crane.car.craneJib.trolley.rope.hookBlock.gameObject.transform.getTranslation(tmp2);
				tmp1.z = tmp2.z = 0;
				ans2 = "Vi tri dat: Lech " + (Math.round(tmp1.dst(tmp2) * 100) / 100f) + "m so voi tam";
			}
			if (userValue1 == placeTest.gameObject.body.getUserValue() &&
					userValue0 == crane.car.craneJib.trolley.rope.hookBlock.gameObject.body.getUserValue()) {
				placeTest.gameObject.transform.getTranslation(tmp1);
				crane.car.craneJib.trolley.rope.hookBlock.gameObject.transform.getTranslation(tmp2);
				tmp1.z = tmp2.z = 0;
				ans2 = "Vi tri dat: Lech " + (Math.round(tmp1.dst(tmp2) * 100) / 100f) + "m so voi tam";
			}
		}
	}

	Main() {
		numberWindow++;
		if (numberWindow == 1) cabinView = 1;
		else cabinView = 0;
	}
	
	@Override
	public void create () {
		//set Bullet lib
		Bullet.init();

		//Screen init
		if (modelBatch == null) modelBatch = new ModelBatch();
		if (batch == null) batch = new SpriteBatch();

		//Bitmap Font
		if (font == null) font = new BitmapFont();

		//Camera init
		if (camera == null) {
			camera = new PerspectiveCamera();
			camera.position.set(0, 0, 10);
			camera.lookAt(0, 0, 0);
		}

		//Desktop view3D
		Gdx.input.setInputProcessor(this);
		Gdx.input.setCatchBackKey(true);

		//Setup for first time
		//Light init
		if (environment == null) {
			environment = new Environment();
			environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.7f, 0.7f, 0.7f, 1.f));
			environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -.4f, -.4f, -.4f));
		}

		//3d simulation physics
		if (collisionConfig == null) collisionConfig = new btSoftBodyRigidBodyCollisionConfiguration();
		if (dispatcher == null) dispatcher = new btCollisionDispatcher(collisionConfig);
		if (broadphase == null)
			broadphase = new btDbvtBroadphase();
		if (constraintSolver == null) constraintSolver = new btSequentialImpulseConstraintSolver();
		if (dynamicsWorld == null) {
			dynamicsWorld = new btSoftRigidDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
			dynamicsWorld.setGravity(new Vector3(0f, 0f, -10f)); //gravity
		}
		if (contactListener == null) contactListener = new MyContactListener();

		//debug mode
		if (debugDrawer == null) {
			debugDrawer = new DebugDrawer();
			dynamicsWorld.setDebugDrawer(debugDrawer);
			debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);
		}

		if (instances == null) instances = new Array<ModelInstance>();
		if (constructors == null) constructors = new ArrayMap<String, GameObject.Constructor>();

		//Obj builder
		if (background == null) background = new Background(instances);
		if (ground == null) ground = new Ground(instances, dynamicsWorld, constructors);
		if (crane == null) {
			crane = new Crane(new Vector3(0, 0, 0), instances, (btSoftRigidDynamicsWorld)dynamicsWorld, constructors);
			btGImpactCollisionAlgorithm.registerAlgorithm(dispatcher);
		}
		if (placeTest == null) placeTest = new PlaceTest(new Vector3(0, 52.095497f, -0.5f), instances, dynamicsWorld, constructors);
	}

	public void resize(int width, int height) {
		//Camera init
		float Height = MINIMUM_VIEWPORT_SIZE;
		if (height < width)
			Height *= (float)height / (float)width;
		float FovRadians = MathUtils.degreesToRadians * camera.fieldOfView;
		distance = Height / (float)Math.tan(FovRadians);
		center = distance * (float)Math.tan(FovRadians / 2);
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.far = 300;
		camera.near = 0.2f;
		camera.up.set(0, 1, 0);
		camera.position.set(crane.car.craneJib.cood.x, crane.car.craneJib.cood.y, 150);
		camera.lookAt(crane.car.craneJib.cood);
	}

	@Override
	public void render () {
		//real-time processing
		final float delta = Math.min(1/30f, Gdx.graphics.getDeltaTime());
		waiting = Math.max(0, waiting - delta);
		//Clear screen
		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		//control
		if (arg[1]) crane.car.craneJib.trolley.moveRope(Trolley.MOVE_UP, delta * DROP_SPEED);
		else if (arg[0]) crane.car.craneJib.trolley.moveRope(Trolley.MOVE_DOWN, delta * DROP_SPEED);
		if (arg[2]) crane.rotateZ(delta * ROTATE_SPEED);
		else if (arg[3]) crane.rotateZ(-delta * ROTATE_SPEED);
		if (arg[5]) crane.rotateX(delta * ROTATE_SPEED);
		else if (arg[4]) crane.rotateX(-delta * ROTATE_SPEED);

		//3d physics cal
		crane.run(arg[7], arg[8], arg[9], arg[10], delta);
		crane.render();
		dynamicsWorld.stepSimulation(delta, 5, 1f / 60f);

        /*Gdx.app.log("Java Heap", "" + Gdx.app.getJavaHeap());
        Gdx.app.log("Native Heap", "" + Gdx.app.getNativeHeap());*/

		//Desktop view3D
		crane.car.gameObject.transform.getTranslation(tmp);
		background.modelInstance.transform.setTranslation(tmp);
		switch (cabinView) {
			case 0:
				crane.car.craneCabin.gameObject.transform.getTranslation(tmp);
				camera.position.set(tmp);
				camera.up.set(0, 1, 0);
				camera.direction.set(0, 0, -1);
				camera.rotate(crane.car.craneCabin.gameObject.transform);
				crane.car.craneJib.gameObject.transform.getTranslation(tmp);
				camera.lookAt(tmp.x, tmp.y, 6);
				break;
			case 1:
				camera.up.set(0, 1, 0);
				crane.car.gameObject.transform.getTranslation(tmp);
				camera.position.set(tmp.x, tmp.y, 80);
				camera.lookAt(tmp);
				break;
			case 2:
				camera.up.set(0, 1, 0);
				camera.position.set(90, 0, 40);
				camera.lookAt(0, 0, 40);
				camera.rotate(Vector3.X, 90);
				break;
		}
		camera.update();
		Vector3 t = new Vector3();
		crane.car.gameObject.transform.getTranslation(t);

		//3d render
		modelBatch.begin(camera);
		modelBatch.render(instances, environment);
		modelBatch.end();

		//2d render
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		batch.begin();
		font.setColor(Color.FIREBRICK);
		font.draw(batch, ans1, 20, 20);
		font.draw(batch, ans2, 20, 40);
		batch.end();
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

		//debug render
		debugDrawer.begin(camera);
		//dynamicsWorld.debugDrawWorld();
		debugDrawer.end();
	}

	@Override
	public void dispose () {
		numberWindow--;
		if (numberWindow == 0) {
			dynamicsWorld.dispose();
			constraintSolver.dispose();
			broadphase.dispose();
			dispatcher.dispose();
			collisionConfig.dispose();
			contactListener.dispose();
			debugDrawer.dispose();
		}
		modelBatch.dispose();
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

    @Override
    public boolean keyDown(int keycode) {
		switch (keycode) {
			case Input.Keys.DPAD_DOWN:
				arg[0] = true;
				break;
			case Input.Keys.DPAD_UP:
				arg[1] = true;
				break;
			case Input.Keys.DPAD_LEFT:
				arg[2] = true;
				break;
			case Input.Keys.DPAD_RIGHT:
				arg[3] = true;
				break;
			case Input.Keys.C:
				arg[4] = true;
				break;
			case Input.Keys.Z:
				arg[5] = true;
				break;
			case Input.Keys.X:
				if (arg[6]) {
					crane.car.craneJib.trolley.rope.disconnect();
					arg[6] = false;
				}
				else {
					if (touch) {
						tmp1.set(ptB);
						tmp1.z = 0;
						ans1 = "Vi tri moc: Lech " + (Math.round(tmp1.dst(Vector3.Zero) * 100) / 100f) + "m so voi tam";
						crane.car.craneJib.trolley.rope.setConstraint(ptB);
						crane.car.craneJib.trolley.rope.connect();
						arg[6] = true;
					}
				}
				break;
			case Input.Keys.A:
				arg[7] = true;
				break;
			case Input.Keys.W:
				arg[8] = true;
				break;
			case Input.Keys.D:
				arg[9] = true;
				break;
			case Input.Keys.S:
				arg[10] = true;
				break;
		}
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
		switch (keycode) {
			case Input.Keys.DPAD_DOWN:
				arg[0] = false;
				break;
			case Input.Keys.DPAD_UP:
				arg[1] = false;
				break;
			case Input.Keys.DPAD_LEFT:
				arg[2] = false;
				break;
			case Input.Keys.DPAD_RIGHT:
				arg[3] = false;
				break;
			case Input.Keys.C:
				arg[4] = false;
				break;
			case Input.Keys.Z:
				arg[5] = false;
				break;
			case Input.Keys.A:
				arg[7] = false;
				break;
			case Input.Keys.W:
				arg[8] = false;
				break;
			case Input.Keys.D:
				arg[9] = false;
				break;
			case Input.Keys.S:
				arg[10] = false;
				break;
		}
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
