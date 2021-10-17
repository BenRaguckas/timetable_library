public class Tester {
    public static void main(String[] args) throws TimetableProcessorExceptionHandler {
        TimetableProcessor tp = new TimetableProcessor("https://timetable.ait.ie", "/2122/login.aspx");

        tp.debug();
        tp.debug_post_data();
    }
}
