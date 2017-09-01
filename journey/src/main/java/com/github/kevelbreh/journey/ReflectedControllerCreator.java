package com.github.kevelbreh.journey;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.bluelinelabs.conductor.Controller;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ReflectedControllerCreator implements ControllerCreator {

  private final Class<? extends Controller> cls;

  public ReflectedControllerCreator(Class<? extends Controller> cls) {
    this.cls = cls;
  }

  @Nullable @Override public Controller create(Bundle args) {
    try {
      Constructor constructor = cls.getConstructor(Bundle.class);
      return (Controller) constructor.newInstance(args);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }
}
