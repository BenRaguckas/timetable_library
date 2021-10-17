import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

public class Test2 {
    public static void main(String[] args) throws IOException, InterruptedException {
                HttpClient cl1 = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        HttpRequest rq1 = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://timetable.ait.ie/2122/login.aspx"))
                .build();

        HttpResponse<String> response = cl1.send(rq1, HttpResponse.BodyHandlers.ofString());

        response.headers().map().forEach((label, value) -> { System.out.println(label + " : " + value.toString()); });
        // `HttpResponse<T>.body()` returns a `T`
//        System.out.println(response.body());
        Scanner scanner = new Scanner(response.body());
        String target = "";
        while (scanner.hasNextLine()) {
            String current_line = scanner.nextLine();
            if (current_line.contains("\"__VIEWSTATE\""))
                target = current_line;
        }
        System.out.println(target);
    }
}
