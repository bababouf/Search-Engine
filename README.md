# _Search Genie_
![](https://i.gyazo.com/69e159c6d36ceb9455631a0359058b15.png)
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

1. Default
   
   ![](https://i.gyazo.com/eb608bfd40a7f0f1879603e38d58698d.png)
   
   WQT:
   - N/dft represents the inverse document frequency for that term (the ratio of documents in the corpus that contain that term)
   - 1 is added to the inverse document frequency to prevent dividing by zero (if the term is found in no documents), and a term being given zero weight (term appears in all documents)
   - The log function is used to scale down the effect of document frequency. Without this, rare terms would dominate and common terms would have very minimal impact.
   
   WTD:
   - TFtd is the number of times a term appears in the document
   - The log function, again, scales down the effect of term frequency.
     
   LD :
   
  - Docweights is calculated as the square root of the summation of TFTD terms squared
    
   
3. TF-IDF
   
   ![](https://i.gyazo.com/f569b3ec39a67f492e2b1bb4541e82bf.png)

   WQT:
   - Exactly as the default calculation, except 1 is not added to the inverse document frequency. Terms that appear in all documents are given 0 weight. 
   
   WTD:
   - Simply uses TFtd as the weight for a term in the document (number of times a term appears in a document).
     
   LD :
   
  - Same as default calculation: docweights is calculated as the square root of the summation of TFTD terms squared.
5. Okapi BM25

   ![](https://i.gyazo.com/6fbc53ea9cb1ee932c012e239e50f55b.png)

   - Developed in the 1980s, used throughout 1990s; designed for short catalog records and abstracts of fairly consistent length
   WQT, WDT:
   - A derivative of the above formulas that uses scientifically devised constants to modify the aformentioned default and TF-IDF formulas
     
   LD :
   
  - Set to a constant value of 1. Again, this formula was designed for a retrieval system on documents with relatively similar lengths
   
6. Waky
   
   ![](https://i.gyazo.com/1b5cba7ac18f70b414f53987528c9131.png) 

   WQT:
   - The maximum value is taken between 0 or the natural log of the number of documents a term appears in
   
   WTD:
   - Calculated as 1 plus the natural log of the TFtd over 1 plus the natural log of the average TFtd (average number of terms in each document)
     
   LD :
   
  - Calculated as the square root of the byte size of the document






## _Web Scraper_
