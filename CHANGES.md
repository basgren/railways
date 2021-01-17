Railways - Change Notes
=======================

### 0.8.17 (2021-01-17)

* Fix crash on loading routes in Rails 6 application
* Make search field wider
* Enhance parsing of with redirect
* Rework usages of deprecated API


### 0.8.16 (2019-10-21)

* Fix exception in IDE version 2019.3


### 0.8.15 (2019-07-14)

* Add support of IDE version 2019.2


### 0.8.14 (2019-07-13)

* Add support of IDE version 2019.1


### 0.8.13 (2019-01-18)

* Enhance route parsing: allow route path starting with '('


### 0.8.12 (2018-11-23)

* Add support of IDE version 2018.3.1 (version 2018.3 is not supported due to issue in RubyMine)
* Update rake routes parser to avoid capturing warnings in output


### 0.8.11

* Add support of IDE version 2018.2


### 0.8.10

* Add support of IDE version 2018.1


### 0.8.9

* Add ability to filter routes using wildcard (*)


### 0.8.8

* Add support of IDE version 2017.3 (@hurricup)


### 0.8.7

* Fix plugin crash in RubyMine 2017.1


### 0.8.6

* Add plugin compatibility with RubyMine 2016.2 and (liekly) with following releases of
  Ruby plugin for IDEA

### 0.8.5 (2016-03-07)

* Fix compatibility with previous version of IDEA and RubyMine


### 0.8.4 (2016-01-30)

* Add ability to copy route path with http method
* Add ability to copy data from multiple selected rows
* Make right-click behavior more intuitive for the routes table


### 0.8.3 (2015-05-16)

* Optimize performance of route action availability check.
* Add option to disable live check of route action implementation.
* Fix NPE when non-rails project is opened.


### 0.8.2 (2015-03-09)

* New feature - autorefresh route list when routes.rb is changed (enable it in 
  the plugin settings).
* Add toggle button to hide routes from mounted engines.
* Minor UI fixes.


### 0.8.1 (2014-12-22)

* Highlight route action in list depending on its availability.
* Add setting to specify Rails environment for which routes are collected.
* Parse redirect routes.
* Fix parsing - routes with unknown action format weren't added to list.


### 0.8.0 (2014-12-06)

* Add settings dialog to customize rake task that retrieves routes.
* Live update route action status and icon (it's disabled in Power Save mode).
* Implement navigation to route action implementation from engines, parent controllers and included modules.
* Navigate to selected route action when Enter is pressed in route list.
* Navigate to controller class when route action cannot be found.
* Fix displaying of engine routes.
* Add hotkey for route list refresh.
* Change Routes tool window orientation depending on panel where it's docked to.
* Persist selected module in Routes tool window between IDE restarts.
* Minor fixes and UI improvements


### 0.7.1 (2014-03-16)

* Fix plugin crash when IDE is run using Java 1.6


### v0.7.0 (2014-02-27)
        
* Added support for projects with multiple modules.
* Added module display in Go To Route popup.
* Fixed routes parsing for Rails 4 projects.
* Syntax highlighting for route paths in Route tool window.
* Minor UI enhancements.
* Plugin now released in two versions - for RubyMine and IntelliJ IDEA due to
  issues with dependencies.


### v0.6.1 (2013-12-05)

* Plugin now compatible with RubyMine 6.0.
* Added support for PATCH method (Rails 4).


### v0.6.0 (2013-02-10)

* Added navigation popup available by pressing <strong>Ctrl+Shift+G</strong> in
  Ruby On Rails project.
* Fixed exception thrown on some versions of JRE.
* Fixed Railways panel disappearing when several Ruby On Rails projects are
  opened.
* Fixed parsing errors.


### v0.5.3 (2012-11-19)

* Fixed: any non-critical warnings in 'rake routes' output was treated as errors
  and error panel was shown after routes are updated.
* Routes are updated just after project is opened, not after Routes panel is
  activated as it was before.
* Minor fixes.


### v0.5.2 (2012-11-09)

* Added caching of route list.
* Routes are automatically updated (or loaded from cache) on IDE startup.
* Route name can be copied via context menu or keyboard shortcuts.


### v0.5.1 (2012-10-27)

* Added compatibility with RubyMine 5.
* Scrollbar is changed to IDE's native.
* Fixed bug with formatting controller name in CamelCase.


### v0.5.0b (2012-10-22)

* Initial release.