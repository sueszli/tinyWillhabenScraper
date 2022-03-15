# tinyWillhabenScraper
This little fella is the fastest [willhaben](https://www.willhaben.at/iad) scraper out there 🔥.
<br><br>

## The initial problem
Willhaben is an online marketplace, similar to craigslist.

Scraping data off of Willhaben is a difficult task, since after performing a simple `GET` request (in any client of your choice), one can usually only access 5 entries to read from.
This is most likely an attempt to make scraping off of this website more difficult.
<br><br>

## The solution
I could bypass this problem by manipulating the initial URL that im fetching from to limit the fetched results to only 5 items (although it is not possible through the GUI).

**An Example:** <br>
Let's say, I'm looking for nice watches to buy. <br>
Once I pick the right place in the market, I'll be forwarded to: https://www.willhaben.at/iad/kaufen-und-verkaufen/marktplatz/uhren-schmuck-2409 <br>
Then I can limit the number of items displayed on each page to just 5 by adding the following query to the URL: `?page=1&rows=5`<br>
This way a single `GET` request to the URL https://www.willhaben.at/iad/kaufen-und-verkaufen/marktplatz/uhren-schmuck-2409?page=1&rows=5 will be enough to read all the data I require
and I can effectively bypass the given obstruction.
