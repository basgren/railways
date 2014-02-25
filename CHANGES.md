Railways - Change Notes
=======================

### v0.7.0
        
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