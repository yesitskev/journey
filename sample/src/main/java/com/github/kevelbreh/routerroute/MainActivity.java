package com.github.kevelbreh.routerroute;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout;
import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.changehandler.AutoTransitionChangeHandler;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.github.kevelbreh.journey.DefaultJourneyProvider;
import com.github.kevelbreh.journey.Journey;

public class MainActivity extends AppCompatActivity {

  private Journey journey;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ChangeHandlerFrameLayout view = findViewById(R.id.content);
    Router router = Conductor.attachRouter(this, view, savedInstanceState);
    journey = new Journey(router, new DefaultJourneyProvider());

    journey.addTransactionInterceptor(
        transaction -> transaction.popChangeHandler(new HorizontalChangeHandler())
            .pushChangeHandler(new AutoTransitionChangeHandler()));

    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("app://curately/help"));
    onNewIntent(intent);
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    journey.routeUsing(intent);
  }
}
