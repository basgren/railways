package net.bitpot.railways.models.routes;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * @author Basil Gren
 */
public abstract class RequestMethod {
    public static final RequestMethod GET = new GetRequestMethod();
    public static final RequestMethod POST = new PostRequestMethod();
    public static final RequestMethod PUT = new PutRequestMethod();
    public static final RequestMethod PATCH = new PatchRequestMethod();
    public static final RequestMethod DELETE = new DeleteRequestMethod();
    public static final RequestMethod ANY = new AnyRequestMethod();


    private static List<RequestMethod> requestMethods = createRequestMethods();


    private static List<RequestMethod> createRequestMethods() {
        Vector<RequestMethod> methods = new Vector<RequestMethod>();
        methods.add(GET);
        methods.add(POST);
        methods.add(PUT);
        methods.add(PATCH);
        methods.add(DELETE);
        methods.add(ANY);

        return methods;
    }


    /**
     * Finds request method by name. If no request method is found, AnyRequestMethod is
     * returned.
     *
     * @param name Name of request method.
     * @return RequestMethod object.
     */
    @NotNull
    public static RequestMethod get(String name) {
        for(RequestMethod method: requestMethods)
            if (method.getName().equals(name))
                return method;

        return ANY;
    }


    /**
     * Returns the icon used for showing routes of the type.
     *
     * @return The icon instance, or null if no icon should be shown.
     */
    public abstract Icon getIcon();

    /**
     * Returns the name of the route type. The name must be unique among all route types.
     *
     * @return The route type name.
     */
    @NotNull
    @NonNls
    public abstract String getName();


    public static Collection<RequestMethod> getAllRequestMethods() {
        return requestMethods;
    }
}
