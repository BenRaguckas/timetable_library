import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TimetableProcessor {
    //  Stage tracker
    public final int    STAGE_START = 0,
                        STAGE_COOKIES = 1,
                        STAGE_STUDENT_SET = 2,
                        STAGE_TIMETABLE = 3;
    private int current_stage = STAGE_START;
    //  URL parts
    private String url_base, url_entry_point, url_home, url_timetable;
    //  Map for POST method
    private Map<String, String> post_content = null;
    //  Cookies :)
    private Map<String, String> session_cookies = null;
    //  Keys to get initial cookies
    private final String[] KEYS_FOR_COOKIES = {
            "__VIEWSTATE",
            "__VIEWSTATEGENERATOR",
            "__EVENTVALIDATION",
            "bGuestLogin",
    };
    //  Keys to get Student Set - by Name
    private final String[] KEYS_STUDENT_SET = {
            "__EVENTTARGET",
            "__VIEWSTATE",
            "__VIEWSTATEGENERATOR",
            "__EVENTVALIDATION",
            "tLinkType"
    };



    //  Constructor
    public TimetableProcessor(String url_base, String url_entry_point) {
        //  Set URL parameters
        this.url_base = url_base;
        this.url_entry_point = url_entry_point;
    }

    //  Will build post_content map with given url and key_list
    private void buildPostContent(String url_target, String[] key_list) throws TimetableProcessorExceptionHandler {
        //  Build new HashMap with nulls
        post_content = new HashMap<>();
        for (String key : key_list) {
            post_content.put(key, null);
        }
        //  Create client
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        //  Create request
        HttpRequest request;
        if (session_cookies == null)
            request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url_base + url_target))
                .build();
        else
            request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url_base + url_target))
                    .headers("Cookie", Utilities.cookiesForHeaders(session_cookies))
                    .build();
        //  Send request for response
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new TimetableProcessorExceptionHandler(e.getMessage());
        }
        //  Use scanner to parse the page
        Scanner scanner = new Scanner(response.body());
        //  Loop through each line and check for key
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            for (String key : key_list)
                //  Surround key with quotes
                if (line.contains("\"" + key + "\""))
                    post_content.put(key, Utilities.getValue(line));
        }
        if (current_stage == STAGE_COOKIES)
            post_content.put("__EVENTTARGET", "LinkBtn_StudentSetByName");
        // Check if all keys were set
        for (String key : key_list)
            if (post_content.get(key) == null)
                throw new TimetableProcessorExceptionHandler("Not all keys_base were set.");
    }

    private HttpResponse<String> getPostResponse(String url_target, String[] keys) throws TimetableProcessorExceptionHandler{
        buildPostContent(url_target, keys);
        //  Create client
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        //  Build body from HashMap
        BodyPublisher requestBody = BodyPublishers.ofString(Utilities.buildRequestBody(post_content));
        //  Build and send request
        HttpRequest request;
        if (session_cookies == null)
            request = HttpRequest.newBuilder()
                    .POST(requestBody)
                    .uri(URI.create(url_base + url_target))
                    .headers("Content-Type", "application/x-www-form-urlencoded"
                    ).build();
        else
            request = HttpRequest.newBuilder()
                    .POST(requestBody)
                    .uri(URI.create(url_base + url_target))
                    .headers("Cookie", Utilities.cookiesForHeaders(session_cookies),
                        "Content-Type", "application/x-www-form-urlencoded"
                    ).build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new TimetableProcessorExceptionHandler(e.getMessage());
        }
        return response;
    }


    private void instantiateCookies() throws TimetableProcessorExceptionHandler {
        //  Failsafe check (should not be necessary)
        if (current_stage == STAGE_START) {
            HttpResponse<String> response = getPostResponse(url_entry_point, KEYS_FOR_COOKIES);
            //  Get information from headers
            url_home = response.headers().map().get("location").toString();
            url_home = url_home.substring(1, url_home.length() - 1);
            String unparsed_cookies = response.headers().map().get("set-cookie").toString();
            unparsed_cookies = unparsed_cookies.substring(1, unparsed_cookies.length() - 1);
            session_cookies = new HashMap<>();
            session_cookies = Utilities.parseCookies(unparsed_cookies);
            //  Advance current stage to STAGE_COOKIES
            current_stage = STAGE_COOKIES;
        } else {
            throw new TimetableProcessorExceptionHandler("Internal staging error has occurred.");
        }
    }


    private void instantiateStudentSet() throws TimetableProcessorExceptionHandler {
        if (current_stage == STAGE_COOKIES) {
            HttpResponse<String> response = getPostResponse(url_home, KEYS_STUDENT_SET);
            System.out.println(response.body());
        } else {
            throw new TimetableProcessorExceptionHandler("Internal Staging error has occurred.");
        }
    }



    //  FOR TESTING
    public void debug() {
    }

    public void debug_post_data() throws TimetableProcessorExceptionHandler {
        instantiateCookies();
        instantiateStudentSet();
        System.out.println("===   POST_CONTENT   ===");
        for(Map.Entry<String, String> item : post_content.entrySet()) {
            System.out.println(item.getKey() + "=" + item.getValue());
        }
    }


}

class TimetableProcessorExceptionHandler extends Exception {
    private String message;
    public TimetableProcessorExceptionHandler(String message) { this.message = message; }
    public String getMessage() { return this.message; }
}

class Utilities {
    //  Isolates a value=" something " property in a given string and returns something
    public static String getValue(String input_line) {
        int start = input_line.indexOf("value=\"") + 7;
        int end = input_line.indexOf(34, start);
        String output = input_line.substring(start, end);
        //  Bootleg encoding for few specific values (hopefully no more than this)
        //  Check for / + = and replace
        if (output.contains("/"))
            output = output.replaceAll("/", "%2F");
        if (output.contains("+"))
            output = output.replaceAll("\\+", "%2B");
        if (output.contains("="))
            output = output.replaceAll("=", "%3D");
        return output;
    }

    //  Sticks together a request body
    public static String buildRequestBody(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> item : map.entrySet()) {
            if (sb.length() != 0)
                sb.append("&");
            sb.append(item.getKey()).append("=").append(item.getValue());
        }
        return sb.toString();
    }

    public static Map<String, String> parseCookies(String unparsed_cookies) {
        Map<String, String> cookies = new HashMap<>();
        String[] chunk = unparsed_cookies.split(",");
        for (String item : chunk) {
            int equals = item.indexOf("=");
            String key = item.substring(0, equals);
            String value = item.substring(equals + 1, item.indexOf(";"));
            cookies.put(key, value);
        }
        return cookies;
    }

    //  Loop through the map and arrange into String array
    public static String cookiesForHeaders(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> item : map.entrySet()) {
            if (sb.length() != 0) {
                sb.append(";");
            }
            sb.append(item.getKey()).append("=").append(item.getValue());
        }
        return sb.toString();
    }
}
