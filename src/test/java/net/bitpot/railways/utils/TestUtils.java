package net.bitpot.railways.utils;

import net.bitpot.railways.models.Route;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;


public class TestUtils {

    /**
     * Get the path to a test data file.
     * @param filename The filename in the data directory (e.g., "parserTest_1.txt")
     * @return The path to the test resource file
     */
    public static String getTestDataPath(String filename) {
        URL resource = TestUtils.class.getClassLoader().getResource("data/" + filename);
        if (resource == null) {
            throw new RuntimeException("Test resource not found: data/" + filename);
        }
        return resource.getPath();
    }

    /**
     * Read the content of a test data file as a string.
     * @param filename The filename in the data directory
     * @return The file contents as a string
     * @throws IOException if the file cannot be read
     */
    public static String readTestData(String filename) throws IOException {
        try (InputStream is = TestUtils.class.getClassLoader().getResourceAsStream("data/" + filename)) {
            if (is == null) {
                throw new IOException("Test resource not found: data/" + filename);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    public static void assertRouteEquals(Route expected, Route actual) {
        assertEquals("Route names are equal",
                expected.getRouteName(), actual.getRouteName());

        assertEquals("Route types are equal",
                expected.getClass(), actual.getClass());

        assertEquals("Request methods are equal",
                expected.getRequestMethod(), actual.getRequestMethod());

        assertEquals("Route paths are equal",
                expected.getPath(), actual.getPath());

        assertEquals("Route actions titles are equal",
                expected.getQualifiedActionTitle(), actual.getQualifiedActionTitle());
    }

}
