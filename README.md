Railways
========

Railways is a plugin for RubyMine that provides convenient way to navigate
between route actions.

Features:

* Separate Routes panel with list of all routes of Ruby On Rails project
* Route filter for quick filtering by route path, controller/action or route name
* "Goto route action" popup available by pressing Ctrl + Shift + G in
  Ruby On Rails projects


## Development Environment

1. Fork Railways repository and clone it to your local machine.

2. Open the project in IntelliJ IDEA

3. Set up a JDK if it's not set (File > Project Structure > SDKs > Add New JDK)

4. Set up IntelliJ plugin SDK. You should have RubyMine installed.
   Go to File > Project Structure > SDKs > Add new IntelliJ IDEA Plugin SDK
   and select path to your RubyMine installation. When you are asked to select
   JDK, specify JDK from previous step.

5. Select a project SDK for your project using "File > Project Structure >
   Project > Project SDK". Choose the plugin SDK you have created at the
   previous step.

6. Use "Run > Run 'Railways in RubyMine'" menu to test plugin inside RubyMine.

