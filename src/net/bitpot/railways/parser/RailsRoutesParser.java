package net.bitpot.railways.parser;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class parses text and retrieves RouteNode
 */
public class RailsRoutesParser extends AbstractRoutesParser {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getInstance(RailsRoutesParser.class.getName());

    private static final Pattern LINE_PATTERN = Pattern.compile("^\\s*([a-z0-9_]+)?\\s*(POST|GET|PUT|PATCH|DELETE)?\\s+(\\S+?)\\s+(.+?)$");
    private static final Pattern ACTION_PATTERN = Pattern.compile(":action\\s*=>\\s*['\"](.+?)['\"]");
    private static final Pattern CONTROLLER_PATTERN = Pattern.compile(":controller\\s*=>\\s*['\"](.+?)['\"]");
    private static final Pattern REQUIREMENTS_PATTERN = Pattern.compile("(\\{.+?\\}\\s*$)");
    private static final Pattern REQUIREMENT_PATTERN = Pattern.compile(":([a-zA-Z0-9_]\\w*)\\s*=>\\s*(.+?)[,]");

    //private static final Pattern EXCEPTION_REGEX = Pattern.compile("rake aborted!\\s*(.+?)Tasks:", Pattern.DOTALL);
    private static final String EXCEPTION_REGEX = "(?s)rake aborted!\\s*(.+?)Tasks:";

    // Will capture both {:to => Test::Server} and Test::Server.
    private static final Pattern RACK_CONTROLLER_PATTERN = Pattern.compile("([A-Z_][A-Za-z0-9_:/]+)");

    public static final Pattern HEADER_LINE = Pattern.compile("^\\s*Prefix\\s+Verb");

    private String stacktrace;

    private final Project project;


    public RailsRoutesParser() {
        this(null);
    }


    public RailsRoutesParser(@Nullable Project project) {
        this.project = project;
    }


    public void clearErrors() {
        stacktrace = "";
    }


    public RouteList parse(String stdOut, @Nullable String stdErr) {
        parseErrors(stdErr);

        return parse(new ByteArrayInputStream(stdOut.getBytes()));
    }


    @Override
    public RouteList parse(InputStream stream) {
        try {
            RouteList routes = new RouteList();

            DataInputStream ds = new DataInputStream(stream);
            BufferedReader br = new BufferedReader(new InputStreamReader(ds));

            String strLine;
            Route route;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                if (isInvalidRouteLine(strLine))
                    continue;

                route = parseLine(strLine);
                if (route != null)
                    routes.add(route);
            }

            return routes;
        } catch (IOException e) {
            //log.debug("Failed to read line.");
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Tests if the line is invalid, i.e. route cannot be parsed.
     *
     * @param line Line from rake routes.
     * @return false if line is correct.
     */
    public boolean isInvalidRouteLine(String line) {
        Matcher matcher = HEADER_LINE.matcher(line);
        return matcher.find();
    }


    /**
     * Parses standard line from the output of rake 'routes' task. If this line contains route information,
     * new Route will be created and its fields set with appropriate parsed values.
     *
     * @param line Line from 'rake routes' output
     * @return Route object, if line contains route information, null if parsing failed.
     */
    public Route parseLine(String line) {
        // 1. Break line into 3 groups - [name]+[verb], path, conditions(action, controller)
        Matcher groups = LINE_PATTERN.matcher(line);

        if (groups.matches()) {
            Route r = new Route(project);

            r.setRouteName(getGroup(groups, 1));
            r.setPath(getGroup(groups, 3));
            r.setRequestType(getGroup(groups, 2));


            String conditions = getGroup(groups, 4);
            String[] actionInfo = conditions.split("#", 2);

            // Process new format of output: 'controller#action'
            if (actionInfo.length == 2) {
                r.setController(actionInfo[0]);

                // In this case second part can contain additional requirements. Example:
                // "index {:user_agent => /something/}"
                r.setAction(extractRouteRequirements(actionInfo[1], r));
            } else {
                // Older format - all route requirements are in the single hash
                parseRequirements(conditions, r);

                Map<String, String> reqs = r.getRequirements();

                r.setController(captureGroup(CONTROLLER_PATTERN, conditions));
                r.setAction(captureGroup(ACTION_PATTERN, conditions));

                if (r.getController().isEmpty())
                    r.setController(captureGroup(RACK_CONTROLLER_PATTERN, conditions));

                // Remove 'controller' and 'action' conditions from requirements.
                reqs.remove("controller");
                reqs.remove("action");
            }


            if (r.isValid()) {
                r.updateType();
                return r;
            } else {
                // TODO: process somehow this error.
            }
        } else {
            // TODO: string not matched. Should log this error somehow.
        }

        return null;
    }


    /**
     * Extracts requirements from second part and fills route information.
     *
     * @param actionWithReq Action with possible requirements part
     * @param r             Route
     * @return Route action name without requirements.
     */
    private String extractRouteRequirements(String actionWithReq, Route r) {
        String requirements = captureGroup(REQUIREMENTS_PATTERN, actionWithReq);

        // Remove outer braces to make regexp simplier.
        //String clean_reqs = requirements.substring(1, requirements.length() - 1);


        parseRequirements(requirements, r);

        // Return action without requirements
        return actionWithReq.substring(0, actionWithReq.length() - requirements.length()).trim();
    }


    /**
     * Parses requirements string (a ruby hash). This string should end with
     * ',' or '}'. In other words, it should be a fully qualified ruby hash,
     * with opening and closing brackets.
     *
     * @param reqs Requirements string to parse
     * @param r    Route where to put all parsed requirements.
     */
    private void parseRequirements(String reqs, Route r) {
        // Just skip parsing of

        // Maybe, more complex parser should be written, because this one is very basic and
        // does not handle situations when requirements are nested hashes.
        //Matcher m = REQUIREMENT_PATTERN.matcher(reqs);


        //while (m.find())
        //    r.getRequirements().put(m.group(1), m.group(2).trim());
    }


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

        // Remove unnecessary text if exception was thrown after rake sent several messages to stderr.
        stacktrace = cleanStdErr.replaceAll(EXCEPTION_REGEX, "$1");
    }


    public String getErrorStacktrace() {
        return stacktrace;
    }


    public boolean isErrorReported() {
        return !stacktrace.equals("");
    }
}
