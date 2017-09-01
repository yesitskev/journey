package com.github.kevelbreh.routerroute.controllers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.github.kevelbreh.routerroute.IntentRenderingController;
import com.github.kevelbreh.journey.Route;

@Route("^/users$")
public class UserListController extends IntentRenderingController {
  public UserListController(@Nullable Bundle args) {
    super(args);
  }
}
