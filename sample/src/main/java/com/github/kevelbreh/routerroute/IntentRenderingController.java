package com.github.kevelbreh.routerroute;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bluelinelabs.conductor.Controller;
import com.github.kevelbreh.journey.Journey;

public class IntentRenderingController extends Controller {

  public IntentRenderingController(@Nullable Bundle args) {
    super(args);
  }

  @NonNull @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller, container, false);
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    Intent intent = getArgs().getParcelable(Journey.EXTRA_INTENT);
    StringBuilder builder = new StringBuilder();
    builder.append("action=").append(intent.getAction()).append("\n");
    builder.append("data=").append(intent.getData()).append("\n");
    for (String key : intent.getExtras().keySet()) {
      builder.append("extra ")
          .append(key)
          .append("=")
          .append(intent.getStringExtra(key))
          .append("\n");
    }
    builder.append("\n");
    builder.append(getClass().getSimpleName());

    TextView text = view.findViewById(R.id.text);
    if (intent != null) {
      text.setText(builder.toString());
    } else {
      text.setText("null");
    }
  }
}
