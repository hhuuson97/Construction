package com.mygdx.main.desktop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Wrapper extends Main {

    public static ApplicationListener createChildWindowClass(Class clazz) {
        try {
            return (ApplicationListener) clazz.newInstance();
        } catch(Throwable t) {
            throw new GdxRuntimeException("Couldn't instantiate app listener", t);
        }
    }

    @Override
    public void create() {
        super.create();

        Lwjgl3Application app = (Lwjgl3Application) Gdx.app;
        Lwjgl3WindowConfiguration config = new Lwjgl3WindowConfiguration();
        config.setTitle("Main Window");
		ApplicationListener listener = createChildWindowClass(Main.class);
		app.newWindow(listener, config);
    }
}