package net.bitpot.railways.models;

import net.bitpot.railways.models.requestMethods.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * @author Basil Gren
 */
public class RequestMethods {
    public static final RequestMethod GET = new GetRequestMethod();
    public static final RequestMethod POST = new PostRequestMethod();
    public static final RequestMethod PUT = new PutRequestMethod();
    public static final RequestMethod PATCH = new PatchRequestMethod();
    public static final RequestMethod DELETE = new DeleteRequestMethod();
    public static final RequestMethod ANY = new AnyRequestMethod();


    private static List<RequestMethod> requestMethods = createRequestMethods();


    private static List<RequestMethod> createRequestMethods() {
        Vector<RequestMethod> methods = new Vector<>();
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


    public static Collection<RequestMethod> getAllRequestMethods() {
        return requestMethods;
    }
}
