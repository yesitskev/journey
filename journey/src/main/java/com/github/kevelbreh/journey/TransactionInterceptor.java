package com.github.kevelbreh.journey;

import com.bluelinelabs.conductor.RouterTransaction;

public interface TransactionInterceptor {
  RouterTransaction intercept(RouterTransaction transaction);
}
