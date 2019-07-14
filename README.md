Railways
========

Railways is a plugin for RubyMine and IntelliJ IDEA Ultimate (with Ruby plugin)
that provides convenient way to navigate between route actions.

Features:

* Displays routes of Ruby On Rails application/engine in a separate "Routes" panel
* Adds quick navigation to action implementation from routes panel or
  "Goto route action" popup available in code editor by pressing **Ctrl + Shift + G**
  (or **Cmd + Shift + G** on Mac)
* Provides quick routes filtering by route path, controller/action or route name

Current release is tested on RubyMine 2016.1, RubyMine 2016.2, IntelliJ IDEA 2016.2.

## Development Environment

1. Fork Railways repository and clone it to your local machine.

2. Open the project in IntelliJ IDEA

3. Set up a JDK if it's not set (File > Project Structure > SDKs > Add New JDK).
   Select JDK 1.8.

4. Set up IntelliJ plugin SDK. You should have RubyMine 2016.2 installed.
   Go to File > Project Structure > SDKs > Add new IntelliJ IDEA Plugin SDK,
   name it "RubyMine 2016.2 SDK" and select path to your RubyMine installation.
   When you are asked to select JDK, specify JDK from previous step.

5. Select a project SDK for your project using "File > Project Structure >
   Project > Project SDK". Choose the plugin SDK you have created at the
   previous step.

6. Use "Run > Run 'Railways'" menu to test plugin inside RubyMine.

7. Use "Run > Run 'All tests'" menu to run all tests for Railways.


## Building the Plugin

1. Copy and rename file `build.properties.example` to `build.properties`.

2. Edit this file and edit properties to match your environment (see comments
   in `build.properties` file).

3. Open Ant tool window in IntelliJ IDEA and run 'all' task. Plugin jars will
   be placed into `release/railways-[version]/` directory. There are two
   versions of plugin: `railways.jar` for RubyMine and `railways-idea.jar` for
   IntelliJ IDEA.
