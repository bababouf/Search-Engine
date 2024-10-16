<h1 align = "center"> Search Genie </h1>
<p align="center">
  <img width="150" height="150" src="https://gyazo.com/d772b4d0dcea61f5d37abbad23918e6f.png">
</p>

## _Overview_

Search Genie is a web application that allows users to upload and query their own .TXT or .JSON directories, as well as directories generated via web scraping. For testing, a pre-loaded directory is also available. The application supports two query modes: ranked retrieval with four ranking schemes, and boolean retrieval using AND, OR, or combinations of both.

## _Technology Stack_
The web application is fully deployed using Microsoft Azure's App Service and integrates with GitHub Actions for seamless continuous integration and deployment. This ensures that any changes pushed to the repository are automatically reflected in the production environment without requiring manual redeployment.

The diagram below shows the main components of the application.


![](https://gyazo.com/52a3d4e99b68325981d2b64a16d94634.png)

### Back-end 
The backend, written in Java, handles indexing directories, processing queries, managing Azure Blob Storage, and performing CRUD operations on PostgresDB. It also facilitates communication with the browser through Java's Jakarta EE Jersey Server implementation. This setup uses seven servlets mapped to specific endpoints, each responsible for different tasks, such as directory uploads/deletions, user authentication via Google API, profile retrieval, configuration of user-selected directories, and handling both boolean and ranked queries.

Azure Blob Storage stores all files created during the indexing process, while PostgresDB enables efficient term lookups during queries.

### Front-end 
Written in vanilla Javascript, CSS, and HTML the front-end offers a simple but responsive user interface for the user. Media queries are used to enhance the viewing experience for users on varying devices. 

## _Indexing Process_
This section will discuss the steps involved in the indexing process, a process which every uploaded directory will undergo to acheive the lowest possible latency between a user's query and the results displayed. 

![](https://gyazo.com/079f35d36d770e8c7791c8d709806424.png)


During indexing, each document within the uploaded directory is processed term by term. Terms are stemmed to reduce variations like "run," "running," and "ran" to their root form, ensuring consistent mapping. These terms are stored in a hashmap, where each unique term is linked to a posting list—a structure containing the document ID and a list of positions where the term appears within the document. This allows the hashmap to efficiently track which terms appear in which documents and their exact locations.

In addition to term indexing, essential metadata is collected for ranked retrieval modes, including the number of tokens (terms) and the byte size of each document. This metadata enables document normalization, preventing longer documents from disproportionately dominating ranked retrieval results. An additional file stores the average token count across all documents.

Once indexing is complete, the hashmap and metadata files are serialized into binary and stored in Azure Blob Storage. Each user's uploaded directory is uniquely identified by a hash of their Google ID and directory name. The final step creates a PostgresDB table for the directory, containing two columns: one for the term and another for the byte position of the term’s posting list in the serialized hashmap. This ensures efficient lookups during querying.

## _Boolean Retrieval_
The boolean retrieval mode in this application allows users to form queries using AND and OR operators. By default, terms separated by spaces are treated as if they are ANDed together, while terms separated by a "+" are treated as ORed.

When terms are ANDed together, the application finds the intersection of their posting lists. For terms ORed together, it computes the union of their posting lists. The query parsing routine processes combined AND/OR queries as follows:

- Loop through the query and collect each term (literal) into a list.
- When encountering a "+", process the preceding terms as an AND query.
- Repeat this for every segment separated by "+" signs.
- Finally, process the entire query by combining all AND results into a single OR query.

## _Ranked Retrieval_
Unlike boolean retrieval, ranked retrieval treats each query as a "bag of words," similar to how search engines operate. This means there is no strict query structure required, but this can lead to issues when using a basic ranking algorithm based solely on term frequency.

A simplistic approach would rank documents based on how frequently the query terms appear in them. However, this can result in irrelevant rankings when common words (like "in" or "the") are present in the query. For example, in the query "fires in Yosemite," ranking only by term frequency might give too much weight to "in" rather than focusing on the more relevant terms "fires" and "Yosemite."

To address this, each term is weighted using inverse document frequency (IDF), which reduces the importance of common words. Despite this, document length still poses a challenge. For example, a short article and an entire book on the same topic might be ranked equally, even though one is more focused on the subject. To handle this, document length normalization is used, ensuring that shorter, more focused documents are not overshadowed by longer ones.

The application uses four ranking schemes, each with unique formulas for determining term weights (WQT), document weights (WDT), and adjusting for document length (LD). The general ranking logic is as follows:

1. For each query term, calculate WQT.
2. For each document containing that term:
      - Retrieve an accumulator value (AD).
      - Calculate WDT.
      - Update AD by multiplying WQT and WDT.
3. After processing all terms, divide each nonzero AD by LD.
4. Return the top 10 documents with the highest AD values.
Below are the formulas used for WQT, WDT, and LD in each ranking scheme.

## Ranking Scheme Formulas

   
   ![](https://i.gyazo.com/eb608bfd40a7f0f1879603e38d58698d.png)
   
   Wqt:
   - N/dft represents the inverse document frequency for that term (the ratio of documents in the corpus that contain that term)
   - 1 is added to the inverse document frequency to prevent dividing by zero (if the term is found in no documents), and a term being given zero weight (term appears in all documents)
   - The log function is used to scale down the effect of document frequency. Without this, rare terms would dominate and common terms would have very minimal impact.
   
   Wdt:
   - TFtd is the number of times a term appears in the document
   - The log function, again, scales down the effect of term frequency.
     
   LD :
   
  - Docweights is calculated as the square root of the summation of TFTD terms squared
    
   

   
   ![](https://i.gyazo.com/f569b3ec39a67f492e2b1bb4541e82bf.png)

   Wqt:
   - Exactly as the default calculation, except 1 is not added to the inverse document frequency. Terms that appear in all documents are given 0 weight. 
   
   Wdt:
   - Simply uses TFtd as the weight for a term in the document (number of times a term appears in a document).
     
   LD :
   
  - Same as default calculation: docweights is calculated as the square root of the summation of TFTD terms squared.


   ![](https://i.gyazo.com/6fbc53ea9cb1ee932c012e239e50f55b.png)

   - Developed in the 1980s, used throughout 1990s; designed for short catalog records and abstracts of fairly consistent length
   Wqt, Wdt:
   - A derivative of the above formulas that uses scientifically devised constants to modify the aformentioned default and TF-IDF formulas
     
   LD :
   
  - Set to a constant value of 1. Again, this formula was designed for a retrieval system on documents with relatively similar lengths


   
   ![](https://i.gyazo.com/1b5cba7ac18f70b414f53987528c9131.png) 

   Wqt:
   - The maximum value is taken between 0 or the natural log of the number of documents a term appears in
   
   Wdt:
   - Calculated as 1 plus the natural log of the TFtd over 1 plus the natural log of the average TFtd (average number of terms in each document)
     
   LD :
   
  - Calculated as the square root of the byte size of the document






## _Web Scraper_

A web scraper is a tool designed to automate the process of extracting data from websites. It simulates human interaction by sending requests to web servers, retrieving the HTML of the pages, and parsing that content to extract specific data such as text, links, or images. In this application, users can provide a homepage URL and set a depth parameter that determines how extensively the site will be crawled. The scraper will then navigate through the pages found within that depth, extracting and saving their content as .JSON files. Once completed, the final directory containing these files will be indexed, following the process described in the indexing section above.

Before discussing the methods employed in this application's web scraper, it’s crucial to address the legal and ethical considerations surrounding web scraping. One key aspect of this is the robots.txt file, which websites use to communicate with web crawlers about which parts of the site can be accessed. While a robots.txt file is not legally binding, it reflects the website owner's preferences and intentions. Respecting these directives is considered a best practice in ethical web scraping, as failing to do so could lead to unauthorized access claims.

Consequently, this application prioritizes the directives specified in a website's robots.txt file. Some websites explicitly restrict scraping, whether for specific sections or the entire site. This application adheres to these preferences, ensuring compliance with the guidelines outlined in the robots.txt file.

## Scraping Strategies and Methods

### Concurrency Management
ExecutorService: The crawler uses a fixed thread pool (with MAX_THREADS set to 5), which allows it to process multiple pages simultaneously while preventing system overload. The activeThreads counter ensures that the program can track and manage how many threads are running at a given time, which prevents oversaturation of resources.
Concurrent Collections: Both the visitedUrls and pageContents are concurrent data structures. visitedUrls is a synchronized set to track which URLs have been crawled, preventing redundant work. pageContents is a ConcurrentLinkedQueue to safely collect crawled page data from multiple threads.

### Politeness and Backoff Strategy
Exponential Backoff: The crawler implements a backoff mechanism using the currentDelay and BACKOFF_FACTOR to progressively increase delays between requests in case of certain status codes like 403 Forbidden, which may indicate the site is blocking requests. This helps prevent overwhelming the server with repeated requests.
Random Delays: Each request introduces a random delay (calculateRandomDelay) between 1 to 5 seconds to mimic human-like browsing behavior, reducing the chance of being detected or blocked by anti-bot mechanisms.
Respecting robots.txt: The crawler integrates a RobotsTxtParser, which ensures that it only crawls URLs allowed by the site's robots.txt rules, adhering to ethical web scraping practices.

### Proxy Rotation and Smart Proxy Use
Proxy Rotation: The crawler employs a SmartProxyRotator to rotate through different proxy IP addresses, ensuring that requests come from different sources, helping evade IP bans or rate limits set by the target websites.
Proxy Authentication: The crawler also supports authenticated proxies by setting up BasicCredentialsProvider for the proxies, helping in cases where smart proxies require login credentials to access their services.

### Retry and Fault Tolerance
Retry Logic: In case of network failures or unsuccessful responses, the crawler retries the request up to MAX_RETRIES (set to 10). It waits for a RETRY_DELAY of 5 seconds between retries, preventing the crawler from failing due to temporary network issues or server hiccups.
Graceful Thread Management: Even if a crawl attempt fails, the thread handling that task will decrement the activeThreads counter, ensuring that the overall execution flow is unaffected by failures in individual tasks.

### SSL and Connection Management
Custom SSL Configuration: The crawler builds an HttpClient with custom SSL settings, including setting up an SSLContext that trusts all certificates. This ensures that the crawler can access sites using various SSL configurations without issues.
Timeout Settings: To avoid indefinitely hanging on unresponsive servers, the crawler sets connection and socket timeouts (CONNECTION_TIMEOUT and READ_TIMEOUT) to 5 seconds each, ensuring it can quickly move on if a server doesn't respond promptly.

### Randomized User-Agent Strings
User-Agent Rotation: To avoid detection as a bot, the crawler randomizes its User-Agent string from a predefined set of common User-Agent headers that mimic different browsers and devices (e.g., Windows, Mac, iPhone). This reduces the likelihood of being blocked by websites filtering out crawlers based on User-Agent patterns.
Custom Request Headers: Besides the User-Agent, other headers like Accept-Language and Accept-Encoding are randomized to resemble a variety of browser configurations and help bypass anti-bot protections.

### Content Decompression and Handling
Support for Various Encodings: The crawler can handle compressed responses with encodings like Gzip, Deflate, and Brotli. It checks the Content-Encoding header of the response and decompresses the data accordingly. This allows the crawler to process pages that serve compressed content more efficiently.
Content Parsing: The HTML content is parsed using Jsoup, which extracts the page's text, title, and links for further processing. This allows the crawler to capture structured data from each page and identify further links to crawl.

### Domain-Specific Crawling
Base Domain Restriction: The crawler is restricted to crawling within a specific domain by checking if each link belongs to the same domain as the baseUrl. This helps focus the crawl on a single website and avoid crawling unrelated domains.

### Handling of HTML Links
Link Extraction and Recursive Crawling: After processing each page's content, the crawler uses Jsoup to extract all the hyperlinks (<a> tags). If these links have not already been visited and are within the allowed domain, they are added to the crawl queue for further exploration, allowing for depth-limited recursive crawling.
