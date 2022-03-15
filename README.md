# tinyWillhabenScraper
This little fella is the fastest [willhaben](https://www.willhaben.at/iad) scraper out there 🔥.

I've optimized this project for reading data from real-estate entries, but you can adapt it to your liking!
<br><br>

## The initial problem
Willhaben is an online marketplace, similar to craigslist.

Scraping data off of Willhaben is a difficult task, since after performing a simple `GET` request (in any client of your choice), one can usually only access 5 entries to read from.
This is most likely an attempt to make scraping off of this website more difficult.
<br><br>

## The solution
I could bypass this obstruction by manipulating the initial URL that im fetching from to limit the fetched results to only 5 items by adding the following query to the URL: `?page=1&rows=5`.

Then I can read all the data I require through just a single request.

<br>

**An Example:**
* Initial URL: https://www.willhaben.at/iad/kaufen-und-verkaufen/marktplatz/uhren-schmuck-2409
* Manipulated URL: https://www.willhaben.at/iad/kaufen-und-verkaufen/marktplatz/uhren-schmuck-2409?page=1&rows=5
<br><br>

## How to use
1. Enter your chosen URL in `src/main/java/Willhaben.java` and run the script.
2. Wait until script kills itself and check out the results as `.csv`-files in `src/main/resources`. 