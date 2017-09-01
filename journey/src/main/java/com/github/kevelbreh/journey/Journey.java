package com.github.kevelbreh.journey;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

public final class Journey {

  public static String EXTRA_INTENT = "Journey.EXTRA_INTENT";

  private List<TransactionInterceptor> transactionInterceptors = new LinkedList<>();

  private final Router router;
  private List<ControllerRoute> routes;

  public Journey(Router router, RoutesProvider factory) {
    this.router = router;
    this.routes = factory.provideRoutes();
  }

  public void routeUsing(@NonNull Uri uri) {
    routeUsing(new Intent(Intent.ACTION_VIEW, uri));
  }

  public void routeUsing(@NonNull Intent intent) {
    onEachControllerRoute(intent);
  }

  public void addTransactionInterceptor(TransactionInterceptor interceptor) {
    transactionInterceptors.add(interceptor);
  }

  public void removeTransactionInterceptor(TransactionInterceptor interceptor) {
    transactionInterceptors.remove(interceptor);
  }

  private void onEachControllerRoute(Intent intent) {
    final Uri uri = intent.getData();
    if (uri == null) {
      return;
    }
    StringBuilder builder = new StringBuilder();
    builder.append(uri.getPath() != null ? uri.getPath() : "/");
    if (uri.getQuery() != null) {
      builder.append("?");
      builder.append(uri.getQuery());
    }

    final String target = builder.toString();
    for (ControllerRoute route : routes) {
      Matcher matcher = route.pattern().matcher(target);
      if (!matcher.matches()) {
        continue;
      }

      intent.putExtras(route.getKeyValuesFromMatcher(matcher));
      Bundle bundle = new Bundle();
      bundle.putParcelable(EXTRA_INTENT, intent);
      handleMatchedRoute(route, bundle);
    }
  }

  private void handleMatchedRoute(ControllerRoute route, Bundle bundle) {
    Controller controller = route.newController(bundle);
    RouterTransaction transaction = RouterTransaction.with(controller);
    for (TransactionInterceptor interceptor : transactionInterceptors) {
      interceptor.intercept(transaction);
    }
    router.pushController(RouterTransaction.with(controller));
  }
}
