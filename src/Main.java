import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        EmailScraper.scrapeEmails("https://lcm.touro.edu/");

        EmailScraper.createThreads(5);
        String userName = "lander", password = "dovberish";
        String url = "lcm.cqzhmas5ky4m.us-east-1.rds.amazonaws.com:1433";
        DatabaseConnect db = new DatabaseConnect();
        db.addEmailsToDatabase(EmailScraper.emails, String.format("jdbc:jtds:sqlserver://%s//gal", url),
                userName, password);
    }
}
