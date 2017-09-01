package com.github.kevelbreh.journey;

import java.util.List;

public interface RoutesProvider {
  List<ControllerRoute> provideRoutes();
}
