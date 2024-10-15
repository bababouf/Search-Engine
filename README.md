# _Search Genie_
![](https://i.gyazo.com/69e159c6d36ceb9455631a0359058b15.png)
## _Overview_

Search Genie is a web application that allows a user to submit queries on .TXT or .JSON directories, as well as directories compiled from the web scraping functionality. For testing purposes, a pre-loaded directory has been built that can be queried. The application offers two modes for querying: a ranked retrieval mode (offering four ranking schemes) and a boolean retrieval mode (AND, OR, or a combination of the two). 

## _Technology Stack_
The web application has been fully deployed using Microsoft Azure's App Service and utilizes Github Actions for continuous integration and deployment. This allows for all changes that are pushed to this repository to be updated in the production application efficiently without having to manually redeploy the application. 

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
## _Web Scraper_
