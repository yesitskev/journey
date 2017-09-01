package com.github.kevelbreh.journey;

import android.os.Bundle;
import com.bluelinelabs.conductor.Controller;

public interface ControllerCreator {
  Controller create(Bundle args);
}
