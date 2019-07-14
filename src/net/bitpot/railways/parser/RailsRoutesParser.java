package net.bitpot.railways.parser;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import net.bitpot.railways.models.RailsEngine;
import net.bitpot.railways.models.RequestMethods;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.models.routes.EngineRoute;
import net.bitpot.railways.models.routes.RedirectRoute;
import net.bitpot.railways.models.routes.SimpleRoute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class parses text and retrieves RouteNode
 */
public class RailsRoutesParser extends AbstractRoutesParser {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getInstance(RailsRoutesParser.class.getName());

    // Errors
    public static final int NO_ERRORS = 0;
    public static final int ERROR_GENERAL = -1;
    public static final int ERROR_RAKE_TASK_NOT_FOUND = -2;


    private static final Pattern ROUTE_LINE_PATTERN = Pattern.compile("^([a-z0-9_]+)?\\s*([A-Z|]+)?\\s+([/(]\\S*?)\\s+(.+?)$");
    private static final Pattern ACTION_PATTERN = Pattern.compile(":action\\s*=>\\s*['\"](.+?)['\"]");
    private static final Pattern CONTROLLER_PATTERN = Pattern.compile(":controller\\s*=>\\s*['\"](.+?)['\"]");
    private static final Pattern REQUIREMENTS_PATTERN = Pattern.compile("(\\{.+?}\\s*$)");
    private static final Pattern REDIRECT_PATTERN = Pattern.compile("redirect\\(\\d+(?:,\\s*(.+?))?\\)");

    private static final String EXCEPTION_REGEX = "(?s)rake aborted!\\s*(.+?)Tasks:";

    // Will capture both {:to => Test::Server} and Test::Server.
    private static final Pattern RACK_CONTROLLER_PATTERN = Pattern.compile("([A-Z_][A-Za-z0-9_:/]+)");

    private static final Pattern HEADER_LINE = Pattern.compile("^\\s*Prefix\\s+Verb");
    private static final Pattern ENGINE_ROUTES_HEADER_LINE = Pattern.compile("^Routes for ([a-zA-Z0-9:_]+):");

    private String stacktrace;

    //private final Project project;
    private Module myModule;
    private int errorCode;

    private List<RailsEngine> mountedEngines;
    private RouteList routes;

    private int insertPos;

    @Nullable
    private RailsEngine currentEngine;


    public RailsRoutesParser() {
        this(null);
    }


    public RailsRoutesParser(@Nullable Module module) {
        myModule = module;
        clear();
        clearErrors();
    }


    private void clearErrors() {
        stacktrace = "";
        errorCode = NO_ERRORS;
    }


    public RouteList parse(String stdOut, @Nullable String stdErr) {
        parseErrors(stdErr);

        return parse(new ByteArrayInputStream(stdOut.getBytes()));
    }


    @Override
    public RouteList parse(InputStream stream) {
        try {
            clear();

            DataInputStream ds = new DataInputStream(stream);
            BufferedReader br = new BufferedReader(new InputStreamReader(ds));

            String strLine;
            List<Route> routeList;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                if (parseSpecialLine(strLine))
                    continue;

                routeList = parseLine(strLine);
                if (!routeList.isEmpty()) {
                    addRoutes(routeList);

                    addRakeEngineIfPresent(routeList);
                }

            }

            return routes;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void addRoutes(List<Route> routeList) {
        if (insertPos < 0)
            routes.addAll(routeList);
        else {
            routes.addAll(insertPos, routeList);
            insertPos += routeList.size();
        }
    }


    private void clear() {
        routes = new RouteList();
        insertPos = -1;
        currentEngine = null;
        mountedEngines = new ArrayList<>();
    }


    /**
     * Adds rake engine to the list of parsed engines.
     * @param routeList Route list parsed from a line.
     */
    private void addRakeEngineIfPresent(@NotNull List<Route> routeList) {
        if (routeList.size() != 1)
            return;

        Route route = routeList.get(0);
        if (route instanceof EngineRoute) {
            mountedEngines.add(new RailsEngine(
                    route.getQualifiedActionTitle(),
                    route.getPath(),
                    route.getRouteName()));
        }
    }


    /**
     * Parses special lines, such as Header, or line with information about
     * routes engine. Returns true if it matches special line pattern and was
     * successfully parsed, false otherwise.
     *
     * @param line Line from rake routes.
     * @return true if line is a special line and was parsed successfully.
     */
    public boolean parseSpecialLine(String line) {
        Matcher matcher = HEADER_LINE.matcher(line);
        if (matcher.find())
            return true;

        matcher = ENGINE_ROUTES_HEADER_LINE.matcher(line);
        if (matcher.find()) {
            // All following routes belong to parsed route engine. We should
            // find engine mount route and add them after it.
            String engineName = getGroup(matcher, 1);
            int index = findEngineRouteIndex(engineName);

            if (index >= 0) {
                insertPos = index + 1;
                currentEngine = findEngine(engineName);
            }

            return true;
        }

        return false;
    }


    @Nullable
    private RailsEngine findEngine(String engineName) {
        for(RailsEngine engine: mountedEngines)
            if (engine.getRubyClassName().equals(engineName))
                return engine;

        return null;
    }


    private int findEngineRouteIndex(String engineName) {
        for(int i = 0; i < routes.size(); i++)
            if (routes.get(i).getQualifiedActionTitle().equals(engineName))
                return i;

        return -1;
    }


    /**
     * Parses standard line from the output of rake 'routes' task. If this line contains route information,
     * new Route will be created and its fields set with appropriate parsed values.
     *
     * @param line Line from 'rake routes' output
     * @return List of Route objects. When parsing failed, the list will be empty.
     */
    @NotNull
    public List<Route> parseLine(String line) {
        List<Route> result = new ArrayList<>();

        // 1. Break line into 3 groups - [name]+[verb], path, conditions(action, controller)
        Matcher groups = ROUTE_LINE_PATTERN.matcher(line.trim());

        if (!groups.matches())
            return result;

        String routeController = "", routeAction = "";
        String routeName = getGroup(groups, 1);
        String routePath = getGroup(groups, 3);
        String conditions = getGroup(groups, 4);
        String[] actionInfo = conditions.split("#", 2);
        String engineClass = "";
        String redirectPath = null; // null - when it's not redirect

        // Process new format of output: 'controller#action'
        if (actionInfo.length == 2) {
            routeController = actionInfo[0];

            // In this case second part can contain additional requirements. Example:
            // "index {:user_agent => /something/}"
            routeAction = extractRouteRequirements(actionInfo[1]);
        } else {

            Matcher redirectMatcher = REDIRECT_PATTERN.matcher(conditions);
            if (redirectMatcher.matches())
                redirectPath = getGroup(redirectMatcher, 1);
            else {
                // Older format - all route requirements are specified in ruby hash:
                // {:controller => 'users', :action => 'index'}
                routeController = captureGroup(CONTROLLER_PATTERN, conditions);
                routeAction = captureGroup(ACTION_PATTERN, conditions);

                // Check reference to mounted engine.
                if (routeController.isEmpty() && routeAction.isEmpty())
                    engineClass = captureGroup(RACK_CONTROLLER_PATTERN, conditions);

                // Else just set action to provided text.
                if (routeAction.isEmpty() && routeController.isEmpty() &&
                        engineClass.isEmpty())
                    routeAction = conditions;
            }
        }


        // We can have several request methods here: "GET|POST"
        String[] requestMethods = getGroup(groups, 2).split("\\|");

        // Also fix path if this route belongs to some engine
        if (currentEngine != null) {
            if (routePath.equals("/"))
                routePath = currentEngine.getRootPath();
            else
                routePath = currentEngine.getRootPath() + routePath;
        }


        for (String requestMethodName : requestMethods) {
            Route route;

            if (!engineClass.isEmpty()) {
                route = new EngineRoute(myModule,
                        RequestMethods.get(requestMethodName), routePath,
                        routeName, engineClass);

            } else if (redirectPath != null) {
                route = new RedirectRoute(myModule,
                        RequestMethods.get(requestMethodName), routePath,
                        routeName, redirectPath);

            } else {
                route = new SimpleRoute(myModule,
                        RequestMethods.get(requestMethodName), routePath,
                        routeName, routeController, routeAction);
            }

            route.setParentEngine(currentEngine);

            result.add(route);
        }

        return result;
    }


    /**
     * Extracts requirements from second part and fills route information.
     *
     * @param actionWithReq Action with possible requirements part
     * @return Route action name without requirements.
     */
    private String extractRouteRequirements(String actionWithReq) {
        String requirements = captureGroup(REQUIREMENTS_PATTERN, actionWithReq);

        // Return action without requirements
        return actionWithReq.substring(0, actionWithReq.length() - requirements.length()).trim();
    }


    @NotNull
    private String getGroup(Matcher matcher, int groupNum) {
        String s = matcher.group(groupNum);
        return (s != null) ? s.trim() : "";
    }


    /**
     * Captures first group in subject
     *
     * @param pattern Regex pattern
     * @param subject Subject string
     * @return Captured group or an empty string.
     */
    private String captureGroup(Pattern pattern, String subject) {
        Matcher m = pattern.matcher(subject);
        if (m.find())
            return m.group(1);

        return "";
    }


    public void parseErrors(@Nullable String stdErr) {
        clearErrors();

        if (stdErr == null)
            return;

        // Remove all rake messages that go to stdErr. Those messages start with "**".
        String cleanStdErr = stdErr.replaceAll("(?m)^\\*\\*.*$", "").trim();
        if (cleanStdErr.equals(""))
            return;

        if (cleanStdErr.contains("Don't know how to"))
            errorCode = ERROR_RAKE_TASK_NOT_FOUND;
        else {
            errorCode = ERROR_GENERAL;
            // Remove unnecessary text if exception was thrown after rake sent several messages to stderr.
            stacktrace = cleanStdErr.replaceAll(EXCEPTION_REGEX, "$1");
        }
    }


    public String getErrorStacktrace() {
        return stacktrace;
    }


    public boolean isErrorReported() {
        return errorCode != NO_ERRORS;
    }


    public int getErrorCode() {
        return errorCode;
    }


    public List<RailsEngine> getMountedEngines() {
        return mountedEngines;
    }
}
