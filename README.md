# Search Engine
![](https://i.gyazo.com/69e159c6d36ceb9455631a0359058b15.png)
## Overview
Written in Java, this search engine program has two main functionalities: building an in-memory index from a corpus of documents, and querying that index. The index allows for fast execution of queries, in exchange for additional time
to do the indexing. If the documents the index is built on are static, the index only needs to be built once.
## Driver Classes
*src/SearchFoundations_Java/edu/csulb/DiskPositionalIndexer.java*  
This driver class builds an on-disk index, and allows for boolean queries, as well as several ranking schemes for ranked queries. 

*src/SearchFoundations_Java/edu/csulb/PositionalInvertedIndexer.java*  
This driver class builds an in-memory index, and allows for boolean queries over it.
## Building the Index
Running the *DiskPositionalIndex* driver will first prompt the user to select to either build or query an index.  

  
![](https://i.gyazo.com/82d1d6efbede43f9aaf5866699fc791e.png)  

Selecting choice 1 will prompt the user to enter the directory for which the index will be built from. Currently the program only knows how to deal with .JSON and .TXT documents; the directory specified needs to be all of one or the other.
The corpus of documents that is in this respository (truncated to 1000 documents) is located at *all-nps-sites-extracted*. 

![](https://i.gyazo.com/87698dfae883724b711a521902de1a6c.png)  

The relative path for this directory can be entered when prompted, as shown above. However, if another corpus is constructed (.JSON or .TXT files) it can be placed in the root of this project and will be indexed in the same manner.
### Indexing Details
This subsection will discuss the essential classes involved with the indexing process, and give insight into what exactly is being done in this process.  

Regardless of which driver class is ran (PositionalInvertedIndexer or DiskPositionalIndexer) an in-memory positional index will be created as the first step of the indexing process. The in-memory positional index that is created is simply a hashmap 
of terms to their corresponding *posting* lists. Thus, for every unique term found in the corpus, a posting list will contain the document, as well as the positions in that document where the term is found. Some terms will contain long posting lists, as they 
show up in many documents, whereas others may contain very short posting lists, as they only appear in few documents. The *indexCorpus* method found in the PositionalInvertedIndexer class
is responsible for creating this in-memory positional index. 
## Querying the Index

### Boolean Queries
### Ranked Queries
1. Default
2. TF-IDF
3. Okapi BM25
4. Waky
