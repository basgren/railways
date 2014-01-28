package net.bitpot.railways.models.routes;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Vector;

/**
 * User: tox
 */
public abstract class RequestType
{
    public static final RequestType GET    = new GetRequestType();
    public static final RequestType POST   = new PostRequestType();
    public static final RequestType PUT    = new PutRequestType();
    public static final RequestType PATCH  = new PatchRequestType();
    public static final RequestType DELETE = new DeleteRequestType();
    public static final RequestType ANY    = new AnyRequestType();


    private static Collection<RequestType> routeTypes = createRouteTypesList();


    private static Collection<RequestType> createRouteTypesList()
    {
        Vector<RequestType> types = new Vector<RequestType>();
        types.add(GET);
        types.add(POST);
        types.add(PUT);
        types.add(PATCH);
        types.add(DELETE);
        types.add(ANY);

        return types;
    }

    /**
     * Returns the icon used for showing routes of the type.
     *
     * @return The icon instance, or null if no icon should be shown.
     */
    public abstract Icon getIcon();

    /**
     * Returns the name of the route type. The name must be unique among all route types.
     * @return The route type name.
     */
    @NotNull
    @NonNls
    public abstract String getName();


    public static Collection<RequestType> getAllRequestTypes()
    {
        return routeTypes;
    }
}
