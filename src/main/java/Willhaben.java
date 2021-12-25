import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.CSVHandler;
import util.DateHandler;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static util.ColoredPrint.*;

public class Willhaben {

    private static final String LINKS_FILENAME = "willhabenLinks" + DateHandler.getCurrentDate();
    private static final String PRODUCTS_FILENAME = "willhabenProducts" + DateHandler.getCurrentDate();

    private static final String ROOT_ADDRESS =
            "https://www.willhaben.at/iad/immobilien/eigentumswohnung/eigentumswohnung-angebote?" +
                    "areaId=117223&areaId=117224&areaId=117225&areaId=117226&areaId=117227&areaId=117228&areaId=117229&areaId=117230&areaId=117231&areaId=117234&areaId=117235&areaId=117236&areaId=117237&areaId=117238&areaId=117239" +
                    "&sort=1" +
                    "&page=1" +
                    "&rows=5" + // -> only 5 products are rendered on first request
                    "&PRICE_FROM=300000&PRICE_TO=400000";

    private static final int PAUSE_LINKS = 500; //in ms
    private static final int PAUSE_PRODUCTS = 1000; //in ms





    public static void main(String[] args) throws InterruptedException {
        printGreen("\uD83E\uDDF9 Starting Willhaben scrape");
        System.out.println("Root: " + ROOT_ADDRESS + "\n\n");

        // scrape links
        printGreen("\uD83D\uDD17 Starting to scrape links");
        scrapeLinks(ROOT_ADDRESS);
        printGreen("Found " + CSVHandler.getSize(LINKS_FILENAME) + " links in total.\n\n");

        // scape products
        printGreen("\uD83C\uDFD8 Starting to scrape products");
        int startIndex = 1;
        printGreen("Starting to read from index " + startIndex + "\n\n");
        scrapeProducts(startIndex);

        // done
        printGreen("DONE! - Read " + CSVHandler.getSize(PRODUCTS_FILENAME) + " products from the total of " + CSVHandler.getSize(LINKS_FILENAME) + " links.");
    }





    private static void scrapeLinks(String url) throws InterruptedException {

        Document doc = null;

        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36")
                    .header("Accept-Language", "en-US")
                    .get();
        } catch (IOException | IllegalArgumentException e) {
            printRed("Invalid URL to scrape link from!");
            System.out.println(e);
            return;
        }

        // fetch
        Elements as = doc.select("a[href^=/iad/immobilien/d/eigentumswohnung]");
        if (as.size() == 0) {
            printYellow("Reached empty URL.");
            return;
        }
        for (Element a : as) {
            String link = a.absUrl("href");
            CSVHandler.appendCSV(LINKS_FILENAME, link);
        }

        // get link for next page (alternatively -> just change 'page' query in URL)
        Elements nextPageElem = doc.select("a[aria-label^=Weiter]");
        if (nextPageElem.size() == 0) {
            printYellow("Reached last page.");
            return;
        }
        String nextPageLink = nextPageElem.get(0).absUrl("href");

        // pause -> necessary so server doesn't blacklist you
        Thread.sleep(PAUSE_LINKS);

        // call for next page
        System.out.print(".");
        scrapeLinks(nextPageLink);
    }





    private static void scrapeProducts(int skip) {
        int current = skip;
        int total = CSVHandler.getSize(LINKS_FILENAME);

        try (CSVReader csvr = new CSVReader(new FileReader(CSVHandler.FILE_PATH + LINKS_FILENAME + ".csv"))) {

            // skip necessary rows
            for (int i = 1; i < skip; i++) {
                csvr.readNextSilently();
            }

            // read line
            String[] nextLine;
            while ((nextLine = csvr.readNext()) != null) {
                String link = nextLine[0];

                // scrape
                String pos = "[" + current++ + "/" + total + "]";
                printGreen("NR - " + pos);
                ArrayList<String> data = scrapeProduct(link);
                // data.add(0, pos);
                CSVHandler.appendCSV(PRODUCTS_FILENAME, data.toArray(String[]::new));
                System.out.println("\n");

                // pause
                Thread.sleep(PAUSE_PRODUCTS);
            }

        } catch (IOException | CsvValidationException | InterruptedException ignore) {
        }
    }

    private static ArrayList<String> scrapeProduct(String url)  {
        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36")
                    .header("Accept-Language", "en-US")
                    .get();
        } catch (IOException | IllegalArgumentException e) {
            printRed("Invalid URL to scrape product from!");
            System.out.println(e);
            ArrayList<String> out = new ArrayList<>();
            out.add("Invalid url: " + url);
            return out;
        }

        ArrayList<String> attributes = new ArrayList<>();

        // url
        System.out.println("URL: " + url);
        attributes.add(url);

        // district
        String d = url.substring(69, 69 + 4);
        System.out.println("District: " + d);
        attributes.add(d);

        // header
        Elements hs = doc.select("h1");
        if (hs.size() != 0) {
            System.out.println("Header: " + hs.get(0).text());
            attributes.add(hs.get(0).text());
        } else {
            printYellow("Header not found.");
            attributes.add("NULL");
        }

        // address
        Elements ds = doc.select("div[data-testid^=object-location-address]");
        if (ds.size() != 0) {
            System.out.println("Address: " + ds.get(0).text());
            attributes.add(ds.get(0).text());
        } else {
            printYellow("Address not found.");
            attributes.add("NULL");
        }

        // living space in m²
        Elements ts0 = doc.select("div[data-testid^=ad-detail-teaser-attribute-0] > span");
        if (ts0.size() != 0) {
            System.out.println("Living space: " + ts0.get(0).text());
            attributes.add(ts0.get(0).text());
        } else {
            printYellow("Living space not found.");
            attributes.add("NULL");
        }

        // number of rooms
        Elements ts1 = doc.select("div[data-testid^=ad-detail-teaser-attribute-1] > span");
        if (ts1.size() != 0) {
            System.out.println("Number of rooms: " + ts1.get(0).text());
            attributes.add(ts1.get(0).text());
        } else {
            printYellow("Number of rooms not found.");
            attributes.add("NULL");
        }

        // type
        Elements ts2 = doc.select("div[data-testid^=ad-detail-teaser-attribute-2] > span");
        if (ts2.size() != 0) {
            System.out.println("Type: " + ts2.get(1).text());
            attributes.add(ts2.get(1).text());
        } else {
            printYellow("Type not found.");
            attributes.add("NULL");
        }

        // old vs. new
        Elements ovn = doc.select("div[data-testid^=attribute-value]");
        if (ovn.size() != 0) {
            String val = ovn.get(1).text();
            if (!val.equals("Neubau") && !val.equals("Altbau")) {
                printYellow("Old vs. new not found.");
                attributes.add("NULL");
            } else {
                System.out.println("Old vs. new: " + val);
                attributes.add(ovn.get(1).text());
            }
        } else {
            printYellow("Old vs. new not found.");
            attributes.add("NULL");
        }

        // last change
        Elements cs = doc.select("span[data-testid^=ad-detail-ad-edit-date-top]");
        if (cs.size() != 0) {
            System.out.println("Last change: " + cs.get(0).text().substring(18));
            attributes.add(cs.get(0).text().substring(18));
        } else {
            printYellow("Last change not found.");
            attributes.add("NULL");
        }

        // price
        Elements ps = doc.select("span[data-testid^=contact-box-price-box-price-value-0]");
        if (ps.size() != 0) {
            System.out.println("Price: " + ps.get(0).text());
            attributes.add(ps.get(0).text());
        } else {
            printYellow("Price not found.");
            attributes.add("NULL");
        }

        // monthly costs
        Elements mc = doc.select("span[data-testid^=price-information-formatted-attribute-value-1]");
        if (mc.size() != 0) {
            System.out.println("Monthly costs: " + mc.get(0).text());
            attributes.add(mc.get(0).text());
        } else {
            printYellow("Monthly costs not found.");
            attributes.add("NULL");
        }

        // broker commission
        Elements mp = doc.select("span[data-testid^=price-information-freetext-attribute-value-0]");
        if (mp.size() != 0) {
            System.out.println("Broker commission: " + mp.get(0).text());
            attributes.add(mp.get(0).text());
        } else {
            printYellow("Broker commission not found.");
            attributes.add("NULL");
        }

        // contact: company
        Elements comp = doc.select("span[data-testid^=top-contact-box-seller-name]");
        if (comp.size() != 0) {
            System.out.println("Comapny name: " + comp.get(0).text());
            attributes.add(comp.get(0).text());
        } else {
            printYellow("Company name not found.");
            attributes.add("NULL");
        }

        // contact: phone number
        // click on button -> not possible with JSoup

        return attributes;
    }
}
