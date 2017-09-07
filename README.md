# Journey

A light weight routing helper for conductor based android applications.

## Usage

#### Defining routes

Annotate the controllers you want registered with Journey. These will be matched based on the a dispatched
URI path using Java regex matcher (so any valid regex is accepted.)  

```java
  @Route("/something")
  public class SomeController extends Controller {
    ...
  }
```

You can can also use placeholders which extract data that will use the placeholder text as it's key.

```java
  @Route("/user/{user_uuid}")
  public class UserController extends Controller {
    public UserController(Bundle args) {
      String userUuid = args.getString("user_uuid");
    }
  }
```

#### Hooking things up
A journey requires a router and a list of routes that can be travelled. If you are using the 
the annotation processor then a journey provider will be compiled for you to use.

Don't forget to handle new incoming intents if you're using a single activity application.

```java
public class MainActivity extends AppCompatActivity {

  ...

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    router = Conductor.attachRouter(this, view, savedInstanceState);
    journey = new Journey(router, new DefaultJourneyProvider());
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    journey.routeUsing(intent);
  }
}

```

#### Intercepting transactions
You can add transaction interceptors to alter controller transactions before they are pushed to the 
router. 

```java
    new TransactionInterceptor() {
      @Override public RouterTransaction intercept(RouterTransaction transaction) {
        return transaction.popChangeHandler(new HorizontalChangeHandler())
            .pushChangeHandler(new AutoTransitionChangeHandler());
      }
    });
```

## Download

Add via Gradle:

```groovy
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}

dependencies {
  implementation 'com.github.kevelbreh.journey:journey:0.1.3'
  annotationProcessor 'com.github.kevelbreh.journey:journey-compiler:0.1.3'
}
```

## License

    Copyright 2017 Kevin Woodland

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
