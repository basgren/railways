Railways
========

Railways is a plugin for RubyMine that provides convenient way to navigate
between route actions.

Features:

* Separate Routes panel with list of all routes of Ruby On Rails project
* Route filter for quick filtering by route path, controller/action or route name
* "Goto route action" popup available by pressing **Ctrl + Shift + G** in
  Ruby On Rails projects


## Development Environment

1. Fork Railways repository and clone it to your local machine.

2. Open the project in IntelliJ IDEA

3. Set up a JDK if it's not set (File > Project Structure > SDKs > Add New JDK).
   It's highly recommended to use JDK 1.6 to make plugin compatible with wider
   range of systems.

4. Set up IntelliJ plugin SDK. You should have RubyMine 7 installed.
   Go to File > Project Structure > SDKs > Add new IntelliJ IDEA Plugin SDK,
   name it "RubyMine 7 SDK" and select path to your RubyMine 7 installation.
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
