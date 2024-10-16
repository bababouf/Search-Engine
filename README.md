# _Search Genie_
![](https://i.gyazo.com/69e159c6d36ceb9455631a0359058b15.png)
## _Overview_

Search Genie is a web application that allows a user to submit queries on .TXT or .JSON directories, as well as directories compiled from the web scraping functionality. For testing purposes, a pre-loaded directory has been built that can be queried. The application offers two modes for querying: a ranked retrieval mode (offering four ranking schemes) and a boolean retrieval mode (AND, OR, or a combination of the two). 

## _Technology Stack_
The web application has been fully deployed using Microsoft Azure's App Service and utilizes Github Actions for continuous integration and deployment. This allows for all changes that are pushed to this repository to be updated in the production application efficiently without having to manually redeploy the application. 

![](https://gyazo.com/52a3d4e99b68325981d2b64a16d94634.png)

### Back-end 
Written in Java, the backend contains all of the files that are used for indexing directories, processing queries, communication with the browser, communication with Azure Blob Storage, and CRUD actions involving the PostgresDB. 

Communication with the browser is carried out using Java's Jakarta EE Jersey Server implementation, which uses servlets mapped to specific endpoints that handle each HTTP request. This application uses 7 separate servlets to handle the different functionality that the web applications offers, which include servlets to handle deleting and uploading directories, logging in a user through google authentication API, retrieving profile information, server configuration (for handling a user-selected directory to query), and a separate servlet for processing boolean and ranked queries. 

Microsoft's Azure Blob Storage is used to store the necessary files that are created during the indexing process. A PostgresDB is used to efficiently lookup terms during querying time. 

### Front-end 
Written in vanilla Javascript, CSS, and HTML the front-end offers a simple but responsive user interface for the user. Media queries are used to enhance the viewing experience for users on varying devices. 

## _Indexing Process_
This section will discuss the steps involved in the indexing process, a process which every uploaded directory will undergo to acheive the lowest possible latency between a user's query and the results displayed. 

During indexing, each document in the directory that is uploaded will be traversed term by term. Each term is stemmed, which allows for terms like run, running, and ran to all map to the same base term. These terms are first stored in a hashmap which maps unique terms to their corresponding posting lists. A posting is a structure that contains a document ID and a list of positions within that document where the term appears. In this way, the hashmap knows exactly which terms appear in which documents and where they appear. 

As documents are being traversed, additional meta data is collected that is necessary for implementing the ranked retrieval modes. Meta data for each document includes the number of tokens (terms) and the number of bytes. This allows for normalization of documents so that longer documents do not always overshadow shorter documents in ranked retrieval results. A third meta data file contains the average tokens value which gives the average number of documents across the directory. 

Once the hashmap is created, along with the three meta data files, each is serialized into binary and stored inside an Azure Blob Storage container (for which the container name is built using a hash of the user's unique google ID and the directory name that was uploaded). This allows for a unique identifier for each user uploaded directory. Once all of this is done, the last step in the indexing process creates a PostgresDB table for the directory. This table consists of two columns, one which contains every term in the serialized hashmap, and the other column contains the specific byte position where that terms posting list information can be found. This allows for efficient lookup of the hashmap information during querying time. 

## _Boolean Retrieval_
The boolean retrieval mode used in this application uses a set of boolean logic operators to form queries. A user can form queries using a combination of AND / OR operators. When entering a query, the application treats terms separated by spaces as terms to be AND'd together, and terms separated by "+" as terms to be OR'd. 

The results set for terms AND'd together is built by taking the intersection of their posting lists, whereas the results set for terms OR'd together is built by taking the union of their posting lists. Since a combination of AND / OR can be used in a single query, a specific method is used to parse boolean queries. The routine for this method is as follows: 

This method is used to parse a boolean query into its individual components. The routine is as follows: 

1. Loop through the raw query, putting each literal (single term) into a list.
2. When a "+" is encountered (signifying the previous literals should be OR'd with the next literals after the "+"), build an AND query with each of the literals found.
3. This process is repeated for each segment of the query separated by "+" signs.
4. Finally, build an OR query that consists of all the the built AND subqueries.

## _Ranked Retrieval_
Contrary to the boolean retrieval mode, ranked retrieval does not require a specific form for a query and instead treats each query as a "bag of words" (the style seen in internet browsers). Below I will briefly discuss some issues that are present in basic ranked retrieval. 

In developing a basic ranking algorithm to rank documents based on a search query, it might initially appear that the fequency of the term in the query is most important. Simply rank documents based on the combined frequency of the query words that appear in the document. The downfall of a simple algorithm like this presents itself when common words appear in the query. Since there is no specific form for ranked queries, the user can enter whatever they wish, and as a result common words might appear often. As an example, a user query of "fires in yosemite" using only term frequency will give equal weight to each of the terms. As a result, documents that heavily contain "in" might rank higher than those that contain the more relevant words "fire" and "yosemite". 

As a remedy to the above situation, weights are given to each of the terms in the query based on their inverse document frequency. In this way, common terms that appear in the majority of documents across the corpus will have a higher inverse document frequency, which decreases the weight of that term in the query. 

Although term frequency combined with inverse document frequency solves several problems, it still does not account for varying document lengths. Assume a situation where two documents are present within a vast corpus; one document gives 3 paragraphs talking about giraffes, and another document contains the entire book that the paragraphs are contained in. In this situation, if the user was searching for "giraffes" both documents would be ranked the same, even though one document is clearly far more focused on the subject. In this way, the length of documents must be taken into account. 

Below I show each of the formulas for the four ranking schemes that the application offers. Each formula uniquely determines weights for the query terms (WQT), the documents (WDT), and takes into account the length of the document (LD). 

Each ranking scheme uses the following logic for determining which documents will be in the results set: 

For each term t in the query,
   1. Calculate WQT
   2. For each document d in term t's posting list:
      
      i. Aquire an accumulator value AD.
      
      ii. Calculate WDT
      
      iii. Increase AD by WQT * WDT
      
   4. Once all documents have been traversed, for each nonzero AD value, divide AD by LD.
   5. Return the top 10 documents with the highest AD values

Below are the formulas and brief explanations for calculating WQT, WDT, and LD for each of the ranking schemes. 

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
