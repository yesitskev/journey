package com.github.kevelbreh.journey;

import android.os.Bundle;
import com.bluelinelabs.conductor.Controller;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ControllerRoute {

  private final Pattern pattern;
  private final ControllerCreator creator;
  private final String[] params;

  public ControllerRoute(String pattern, String[] params, ControllerCreator creator) {
    this.pattern = Pattern.compile(pattern);
    this.params = params;
    this.creator = creator;
  }

  Controller newController(Bundle args) {
    return creator.create(args);
  }

  Pattern pattern() {
    return pattern;
  }

  Bundle getKeyValuesFromMatcher(Matcher matcher) {
    Bundle bundle = new Bundle();
    matcher.reset();
    if (matcher.find()) {
      for (int i = 0; i < matcher.groupCount(); i++) {
        bundle.putString(params[i], matcher.group(i + 1));
      }
    }
    return bundle;
  }
}
