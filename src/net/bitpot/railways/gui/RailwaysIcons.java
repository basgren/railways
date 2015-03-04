package net.bitpot.railways.gui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import icons.RubyIcons;

import javax.swing.*;

/**
 * Class contains all plugin icons.
 */
public class RailwaysIcons {
    private static final String PLUGIN_ICONS_PATH = "/net/bitpot/railways/icons/";

    private static Icon pluginIcon(String name) {
        return IconLoader.getIcon(PLUGIN_ICONS_PATH + name);
    }

    public static final Icon HTTP_METHOD_ANY = pluginIcon("method_any.png");
    public static final Icon HTTP_METHOD_GET = pluginIcon("method_get.png");
    public static final Icon HTTP_METHOD_POST = pluginIcon("method_post.png");
    public static final Icon HTTP_METHOD_PUT = pluginIcon("method_put.png");
    public static final Icon HTTP_METHOD_DELETE = pluginIcon("method_delete.png");
    public static final Icon RAKE = RubyIcons.Rake.Rake_runConfiguration;

    // Icons for table items
    public static final Icon NODE_CONTROLLER = RubyIcons.Ruby.Nodes.Controllernode;
    public static final Icon NODE_ERROR = AllIcons.General.Error;
    public static final Icon NODE_METHOD = AllIcons.Nodes.Method;
    public static final Icon NODE_MOUNTED_ENGINE = AllIcons.Nodes.Plugin;
    public static final Icon NODE_REDIRECT = pluginIcon("redirect.png");
    public static final Icon NODE_ROUTE_ACTION = RubyIcons.Rails.ProjectView.Action_method;
    public static final Icon NODE_UNKNOWN = pluginIcon("unknown.png");
    
    

    public static final Icon UPDATE = IconLoader.getIcon("/actions/sync.png");
    public static final Icon SUSPEND = IconLoader.getIcon("/actions/suspend.png");
}
