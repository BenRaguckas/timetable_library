import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Map;


public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
//        HttpClient cl1 = HttpClient.newBuilder()
//                .version(HttpClient.Version.HTTP_2)
//                .connectTimeout(Duration.ofSeconds(5))
//                .followRedirects(HttpClient.Redirect.NEVER)
//                .build();
//
//        HttpRequest rq1 = HttpRequest.newBuilder()
//                .GET()
//                .uri(URI.create("https://timetable.ait.ie/2122/login.aspx"))
//                .build();
//
//        HttpResponse<String> response = cl1.send(rq1, BodyHandlers.ofString());
//        // `HttpResponse<T>.body()` returns a `T`
//        System.out.println(response.body());



        HttpClient client = HttpClient.newBuilder()
                // just to show off; HTTP/2 is the default
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        BodyPublisher requestBody = BodyPublishers.ofString("__LASTFOCUS=&__VIEWSTATE=%2FwEPDwUJMzc0MDY4NzA1D2QWAgIED2QWAgIBD2QWDgIBDw8WAh4EVGV4dAUFTG9naW5kZAIFDw8WAh8ABQdTdHVkZW50ZGQCBw8PFgIfAAU6Rm9yIHN0YWZmLCBwbGVhc2UgcHJvdmlkZSB5b3VyIHN0YWZmIHVzZXJuYW1lIGFuZCBwYXNzd29yZGRkAgkPDxYCHwAFCVVzZXIgTmFtZWRkAg0PDxYCHwAFCFBhc3N3b3JkZGQCEQ8PFgIfAGVkZAITDw8WAh8ABQVMb2dpbmRkZLesKN85MQBO3tRAJOnsOYycsMVs&__VIEWSTATEGENERATOR=30F96575&__EVENTTARGET=&__EVENTARGUMENT=&__EVENTVALIDATION=%2FwEWBQK48MvtAgKsl87%2BDQKjq9T8DgLL9PDwDAKa%2FJ78DuLYdH5F2VaGbdtivug4NQccywYs&bGuestLogin=Student&tUserName=&tPassword=");

        HttpRequest request = HttpRequest.newBuilder()
                .POST(requestBody)
                .uri(URI.create("https://timetable.ait.ie/2122/login.aspx"))
                .headers("Content-Type", "application/x-www-form-urlencoded"
//                        "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:93.0) Gecko/20100101 Firefox/93.0",
//                        "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
//                        "Accept-Language", "en-US,en;q=0.5",
//                        "Accept-Encoding", "gzip, deflate, br",
//                        "Content-Type", "application/x-www-form-urlencoded",
//                        "Origin", "https://timetable.ait.ie",
//                        "Upgrade-Insecure-Requests", "1",
//                        "Sec-Fetch-Dest", "document",
//                        "Sec-Fetch-Mode", "navigate",
//                        "Sec-Fetch-Site", "same-origin",
//                        "Pragma", "no-cache",
//                        "Cache-Control", "no-cache",
//                        "TE", "trailers"
                ).build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        System.out.println(response.statusCode());
        System.out.println(response.version());
        HttpHeaders headers = response.headers();
//        System.out.println(headers.toString());
        headers.map().forEach((label, value) -> { System.out.println(label + " : " + value.toString()); });
//        System.out.println(response.body());

    }
}
