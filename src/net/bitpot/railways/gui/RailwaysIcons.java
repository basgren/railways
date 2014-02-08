package net.bitpot.railways.gui;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * Class contains all plugin icons.
 */
public class RailwaysIcons
{
    private static final String PLUGIN_ICONS_PATH = "/net/bitpot/railways/icons/";

    private static Icon pluginIcon(String name)
    {
        return IconLoader.getIcon(PLUGIN_ICONS_PATH + name);
    }


    public static final Icon HTTP_METHOD_ANY    = pluginIcon("method_any.png");
    public static final Icon HTTP_METHOD_GET    = pluginIcon("method_get.png");
    public static final Icon HTTP_METHOD_POST   = pluginIcon("method_post.png");
    public static final Icon HTTP_METHOD_PUT    = pluginIcon("method_put.png");
    public static final Icon HTTP_METHOD_DELETE = pluginIcon("method_delete.png");
    public static final Icon RACK_APPLICATION   = pluginIcon("rack_application.png");
    public static final Icon RUBY_ON_RAILS      = pluginIcon("ruby_on_rails.png");

    public static final Icon UPDATE = IconLoader.getIcon("/actions/sync.png");
    public static final Icon SUSPEND = IconLoader.getIcon("/actions/suspend.png");
}
