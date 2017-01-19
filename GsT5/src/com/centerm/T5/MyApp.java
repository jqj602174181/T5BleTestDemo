package com.centerm.T5;

import android.app.Application;

public class MyApp extends Application {

	public static MyApp app;
	boolean connected;

	@Override
	public void onCreate() {
		super.onCreate();

		app = this;
	}

	public static MyApp getInstance(){
		return app;
	}
}
