# JustAddHilt
This is a plugin that adds a popular dependency injection library [Hilt](https://dagger.dev/hilt/) to your Android project.

## Where to download the plugin?
It's not published yet. The link will appear here as soon as I publish it. You can clone and compile the project and launch it from your IDE though.

## How to use the plugin?
Open the Tools menu on your IDE's Menu Bar, then click "Add Hilt to the Project" and wait for the completion notification to pop up. Then commit the changes, if you want to.

## What exactly does the plugin add?
1. If `com.google.dagger:hilt-android-gradle-plugin` is missing in the project-level `build.gradle`, it gets added.
2. The following dependencies 
```groovy
implementation 'com.google.dagger:hilt-android:$hilt-version'
annotationProcessor 'com.google.dagger:hilt-compiler:$hilt-version'
```
are added to all modules with [Android facet](https://www.jetbrains.com/help/idea/android-facet-page.html). If a repository to retrieve these packages from (`google()` or `mavenCentral()`) is missing in the project-level `build.gradle`, it gets added as well.

3. All Android modules (i.e. modules with plugin `com.android.application` enabled in their `build.gradle`) receive `Application` classes annotated with `@HiltAndroidApp`. These classes are registered in the respective manifest. If an `Application` class already exists and is registered, only the annotation `@HiltAndroidApp` is added.

## So there's no option to pick the modules the dependencies will be added to?
No, not yet. In order to implement such picking I had to add at least one GUI window, whereas I wanted this plugin to be as silent as possible. Please be careful if you use this plugin on a project comprising hundreds of modules (why do you need this plugin in this case?..), but it should do its job nonetheless.

## What is the value of `$hilt-version` mentioned above?
Currently it is `2.39.1`. This constant is hardcoded in the plugin's source code. I played around with the idea of retrieving the fresh Hilt version from the [official site](https://dagger.dev/hilt/) or [Github](https://github.com/google/dagger/releases), but this approach increased the plugin's execution time so much that I decided to get rid of the network code. Might return it back later.

Also, in future Hilt releases Google may change the library's addition algorithm, so yeah, for now version hardcoding it is.

## Are test dependencies added?
No. It is easily done with the existing codebase, but I expect that only a small fraction of the plugin's users will be using these dependencies, and I decided not to clutter `build.gradle` files with excess lines. If you need test Hilt dependencies, please add them manually.

## What's going to happen if I try to use this plugin on a non-Android project?
You're expected to see a notification telling you that's impossible, and that's it. Your project files are not supposed to change.

## Does this plugin contain tests?
Unfortunately, no. [The documentation on this topic](https://plugins.jetbrains.com/docs/intellij/testing-plugins.html) is pretty scarce. I spent a lot of time trying to configure test projects the way the test environment recognises them as Android projects, didn't progress much (some close-to-useless unit tests were written though, but they are not present here) and gave up because I was quite low on time.

## "Low on time"? Did you have a deadline?
Something like that, yes. This plugin was created as a test assignment for [JetBrains](https://www.jetbrains.com/). The development took about 8 full workdays.

## In that case, will this plugin be supported in the future?
Time will tell.
