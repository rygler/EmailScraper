import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class EmailScraper {

    private static Document doc;
    private static String domain;
    private static Pattern pattern;
    private static ConcurrentLinkedQueue<String> linkQueue;
    private static Set<String> visitedLinks;
    static Set<String> emails;


    static void scrapeEmails(String url) {
        instantiateFields();
        setDomain(url);
        getLinks(url);
    }

    private static void instantiateFields() {
        linkQueue = new ConcurrentLinkedQueue<>();
        visitedLinks = Collections.synchronizedSet(new HashSet<>());
        emails = Collections.synchronizedSet(new HashSet<>());
        pattern = Pattern.compile("[A-za-z0-9_\\.]+@[A-za-z0-9_\\.]+\\.[comedunetorg]{3}");
    }

    private static void clickLink() {
        String currentURL;
        int counter = 0;
        while (!linkQueue.isEmpty() && emails.size() < 10000) {
            currentURL = linkQueue.remove();
            System.out.println(currentURL);
            getLinks(currentURL);
            getEmails();
            System.out.println(emails.size());
            System.out.println("Links: " + counter++ + "  Emails: " + emails.size());
        }
    }

    private static void getEmails() {
        Matcher matcher = pattern.matcher(doc.toString());
        String email;
        try {
            while (matcher.find()) {
                email = matcher.group();
                emails.add(email.toLowerCase());
            }
        } catch (StringIndexOutOfBoundsException | NullPointerException |IllegalStateException ex) {
            Logger.getLogger(EmailScraper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void getLinks(String url) {
        try {
            doc = Jsoup.connect(url)
                    .timeout(5000)
                    .get();
        } catch (IllegalArgumentException | HttpStatusException | SocketTimeoutException | MalformedURLException | UnknownHostException | SSLHandshakeException | NullPointerException ex) {
            System.out.println("Bad Link: " + url);
            return ;
        } catch (IOException ex) {
            Logger.getLogger(EmailScraper.class.getName()).log(Level.SEVERE, null, ex);
            return ;
        }
        addLinks(doc.select("a[href]"));
    }

    private static void addLinks(Elements links) {
        String hrefAttribute;

        for (Element link : links) {
            hrefAttribute = link.attr("abs:href");

            if (isLinkValid(hrefAttribute)) {
                linkQueue.add(hrefAttribute);
                visitedLinks.add(hrefAttribute);
            }
        }
    }

    private static boolean isLinkValid(String url) {
        return !isLinkVisited(url) && isSameDomain(url) && isValidExtension(url) && isNotOnBlacklist(url);
    }

    private static boolean isNotOnBlacklist(String url) {
        String[] blackList = {"facebook", "vimeo", "youtube", "twitter"};
        for (String site : blackList) {
            if (url.contains(site)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidExtension(String url) {
        return !url.contains(".pdf") && !url.contains(".jpg") && !url.contains(".jpeg") && !url.contains(".mp3") && !url.contains(".MP3") && !url.contains(".png");
    }

    private static boolean isSameDomain(String url) {
        return url.contains(domain);
    }

    private static boolean isLinkVisited(String url) {
        return visitedLinks.contains(url);
    }

    private static void setDomain(String url) {
        domain = url;
    }

    static void createThreads(int numberOfThreads)  {
        Thread[] threads = new Thread[numberOfThreads];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new OpenLinksThread();
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(EmailScraper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static class OpenLinksThread extends Thread {
        @Override
        public void run() {
            clickLink();
        }
    }
}
