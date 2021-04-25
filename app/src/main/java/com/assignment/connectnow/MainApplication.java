package com.assignment.connectnow;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.annotation.Annotation;
import java.util.Set;


public class MainApplication extends Application {

    private GlobalSettings globalSettings;
    private static FirebaseDatabase firebaseDatabase;


    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        initExamples();
    }

    private void initExamples() {
        try {
            Set<String> packageName = ClassUtils.getFileNameByPackageName(this, "io.agora.api.example.examples");
            for (String name : packageName) {
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N && name.contains("io.agora.api.example.examples.advanced.ARCore")) {
                    continue;
                }
                Class<?> aClass = Class.forName(name);
                Annotation[] declaredAnnotations = aClass.getAnnotations();
                for (Annotation annotation : declaredAnnotations) {
                    if (annotation instanceof Example) {
                        Example example = (Example) annotation;
                        Examples.addItem(example);
                    }
                }
            }
            Examples.sortItem();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public FirebaseDatabase getFirebaseDatabase() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
        }
        return firebaseDatabase;
    }

    public GlobalSettings getGlobalSettings() {
        if(globalSettings == null){
            globalSettings = new GlobalSettings();
        }
        return globalSettings;
    }
}