# _Search Engine_
![](https://i.gyazo.com/69e159c6d36ceb9455631a0359058b15.png)
## _Overview_

Written in Java, this search engine program has two main functionalities: building an in-memory index from a corpus of documents, and querying that index. The index allows for fast execution of queries, in exchange for additional time
to do the indexing. If the documents the index is built on are static, the index only needs to be built once.

## _How To Run_  

1. Clone the repository to local filesystem: **git clone https://github.com/bababouf/Search-Engine.git**
2. Navigate to the root of the cloned repository
3. Use Maven command to build project: **mvn clean install**
4. Run the driver class: (Open project in an IDE and run the driver class below)


Driver Class: **src/SearchEngineFoundation/drivers/DiskPositionalIndexer.java**  

Don't have Maven? Download it here: **https://maven.apache.org/download.cgi**  
Linux and MacOS should choose the *tar.gz archive*, and windows should choose *zip archive*.  
For installation instructions, follow: **https://maven.apache.org/install.html**

## _Building the Index_
Running the *DiskPositionalIndex* driver will first prompt the user to select to either build or query an index.  

  
![](https://i.gyazo.com/82d1d6efbede43f9aaf5866699fc791e.png)  

Selecting choice 1 will prompt the user to enter the directory for which the index will be built from. Currently the program only knows how to deal with .JSON and .TXT documents; the directory specified needs to be all of one or the other.
The corpus of documents that is in this respository (truncated to 1000 documents) is located at *all-nps-sites-extracted*. 

![](https://i.gyazo.com/87698dfae883724b711a521902de1a6c.png)  

The relative path for this directory can be entered when prompted, as shown above. However, if another corpus is constructed (.JSON or .TXT files) it can be placed in the root of this project and will be indexed in the same manner.
### _Indexing Details_
The indexing process can be broken up into three main stages:

**_1: Creating the in-memory positional index (PositionalInvertedIndexer.indexCorpus())_**  

The most important part of the indexing process is creating the in-memory index. The in-memory positional index that is created is a hashmap of terms to their corresponding *posting* lists. For every unique term found in the corpus,
a posting list will contain the documentID, as well as positions (integers starting from 0 at the beginning of the document) where that term is found. Some terms will contain long posting lists, as they show up in many documents, whereas others may contain very short posting lists, as they only appear in few documents. The *indexCorpus* method found in the PositionalInvertedIndexer class is responsible for creating this in-memory positional index. Creating this index takes care of all the needs for boolean queries, but in order to achieve functionality for ranked queries, additional information must be taken from the documents during indexing time. To accomplish this, a binary file within the corpus directory is populated with this information (i.e *all-nps-sites-extracted/index/docWeights.bin*)  

**_2: Creating the on-disk index (DiskIndexWriter.writeIndex())_**  

Once the in-memory index is created, it is then moved to disk (i.e *all-nps-sites-extracted/index/postings.bin*). Each term is stored in the following manner:  
  
documentFrequency -> docID_First -> position1 -> position2 -> ... -> positionZ -> docID_Next -> position1 -> position2 -> ... -> positionZ  

DocumentFrequency contains the number of documents the term appears in. This is followed by the first documentID, and the integer positions where the term is found in that specific document. Position 0 would correspond to the first term in a document.  This pattern continues for the rest of the documents that the term is found in.  

**_3: Creating SQLite database to store byte positions of terms (SQLiteDB class)_**  

The last stage of the indexing process is to store the bytePositions within the on-disk index file (*postings.bin*) where each term starts. To do this efficiently, a local SQLite database consisting of two columns, one for the *string term* and one for the *long bytePosition* is used. In this way, when a term is found in a query, the bytePosition where it is found in the on-disk index can be found efficiently. 




## _Querying the Index_
As stated above, running the *DiskPositionalIndex* driver will first prompt the user to select to either build or query an index.  

  
![](https://i.gyazo.com/82d1d6efbede43f9aaf5866699fc791e.png)  

Selecting choice 2 will allow the user to query an index. The user will be prompted to select a mode for querying:  


![](https://i.gyazo.com/8988043619d45852e693e4de8342fa7e.png)  

And enter a valid corpus directory:  

![](https://i.gyazo.com/87698dfae883724b711a521902de1a6c.png)  

Once a valid directory is entered, the user will either be prompted to enter a query (if boolean query mode was selected) or to select a ranking scheme (if ranked query mode was selected).  


### _Boolean Queries_
The program is able to process boolean queries that are in normal disjunctive form (one of more AND queries joined with ORs). Quotes around the query are used to indicate phrase queries, where the user is looking
for specific phrases that appear in a document. 
Below are some examples of what this looks like:  


Single Term: dogs  

*AND* Query: dogs cats  

*OR* Query: dogs + cats  

*Phrase* Query: "fires in yosemite" (needs quotes around query)  

*Mixed* Query: dogs cats + elephants yaks turkeys

### _Basic Ranked Retrieval_  

In ranked retrieval, the documents of a corpus are ranked based on their suspected relevance to a given query. A basic ranking scheme may look to term frequency as an indicator for relevant documents. For example if a user entered "dogs", intuitively, 
it might make sense to rank the documents where dog appears frequently higher than those in which it doesn't. However, it soon becomes apparent that not all terms in the query are equal. If the query was instead "the dogs", a document with frequent use of "the" (and no mention of dogs) might rank higher than a document actually talking about dogs. Thus, for each of the schemes below, weights are given for both the terms in the query and the terms in each document.  

In addition to these weights, the length of the document must be accounted for. Without an attempt to normalize document lengths, longer documents would almost always rank higher than shorter documents.


1. Default
   
   ![](https://i.gyazo.com/eb608bfd40a7f0f1879603e38d58698d.png)
   
   
3. TF-IDF
   
   ![](https://i.gyazo.com/f569b3ec39a67f492e2b1bb4541e82bf.png)  
4. Okapi BM25

   ![](https://i.gyazo.com/6fbc53ea9cb1ee932c012e239e50f55b.png)
   
5. Waky
   
   ![](https://i.gyazo.com/1b5cba7ac18f70b414f53987528c9131.png)  
   
### _Querying Details_  
Depending on the mode selection that the user chooses, data will flow through one of two paths. Below the most important methods for each of the flows are named and described.

**_Flow 1: Parsing and Processing Boolean Queries_**  
The data flow for boolean queries first starts with processing the query, which is done using the BooleanQueryParser.parseQuery() method. This method will always return a list of QueryComponents. A QueryComponent is an interface
class that several other classes implement; for example, if a phrase is entered by the user, the parseQuery() method will return a list comprised of one PhraseLiteral component. However, this list returned by parseQuery() can include several
different QueryComponents (ANDQuery, ORQuery, Literal). Depending on the components of the query, one of two functions will be called; getPostingsWithPositions() is only neededfor phrase queries, while all other components do not need positions 
and simply use getPostings(). The final list returned will hold the appropriate postings that were merged. Below are the main classes responsible for handling/processing boolean queries:

**_Main Classes:_**  
&emsp;**BooleanQueryParser.ParseQuery()**  
&emsp;**QueryComponent.getPostings()**  
&emsp;**QueryComponent.getPostingsWithPositions()**  
&emsp;**DiskPositionalIndexer.printResults()**  


**_Flow 2: Parsing and Processing Ranked Queries_**  
The data flow for ranked queries begins with the RankedQuery.parseQuery() method. The parseQuery() method does not deal with QueryComponents in the same way the boolean parser does. Instead, it creates a TermLiteral for each of the space-separated terms in the query. 
Regardless of the ranking scheme used, each of the terms in the query will be given a weight. In addition, when getPostings() is called on each of the literals, documents where the term is found are given a weight. The weight for each term and each document that 
term is found in are multiplied together, and this result is known as the accumulator value. If a document contains multiple terms found in the query, the accumulator value will continue to grow. For example, assume the query is "fires in yosemite". Each of the literals ("fires", "in", "yosemite") will be given a weight. Again, each ranking scheme will handle this differently, but for the most part, terms that show up in the majority of documents (a, the, be, I) will be given a lower weight than terms which are infrequent. 
The program will proceed to go term by term in the query and find the documents where it appears. If "fires" appears in document 1 (docID = 1), the weight for that term in the document (given by the specific ranking scheme) and the weight for that term in the query will be multiplied, creating the accumulator value for that document. Additional query terms found in document 1 will grow this value (give it a higher ranking). Once all query terms and documents are traversed, the documents with the highest accumulator values will be returned.  

  
As mentioned above, there are four different ranking schemes, each with their own unique way of calculating the weight of a term in the query, and the weight of a term in a document. In addition, several of the schemes differ in how the length of the document is taken into account. The user is able to select the ranking scheme that will be used, and depending on this selection, the proper calculate method must be called. To do this, RankingStrategy is an interface class that contains the two methods calculate() and calculateAccumulatorValue(). Calculate(), as explained, will calculate the weights for the query term and the term in each document. CalculateAccumulatorValue() will account for the length of the document, and return the highest ranking documents. Each of the ranking schemes are derived from the RankingStrategy class, and have their own calculate() and calculateAccumulatorValue() methods. A RankedDispatch class is used to call the calculate method from the proper derived class. 
  
**_Main Classes:_**  
&emsp;**RankedQueryParser.parseQuery()**  
&emsp;**RankingStrategy.calculate()**  
&emsp;**RankedDispatch.calculate()**  
&emsp;**DiskPositionalIndexer.printTop10Ranked()**  


### _Testing Details_  
The testing module (**src/SearchEngineFoundation/tests**) contains unit tests for the most important methods invovled in the project. The BuildIndexTest class contains tests that ensure the index is properly built, and the QueryIndexTest contains tests 
to ensure boolean and ranked queries are properly calculated and the result is as expected.  

**_src/SearchEngineFoundation/tests/BuildIndexTest.java_**  

This class contains tests that ensure the path entered is properly read in, and that the system mode rejects input that assumes the wrong format. The most important test, however, is the buildIndexAndTestDocumentWeights() test. This ensure that for each of the 5 test
documents, the weights that are being calculated are as expected. For this test, weights for each of the documents were manually calculated and checked against the weights returned by the method being tested.  

**_src/SearchEngineFoundation/tests/QueryIndexTest.java_**  

This class contains unit tests for both boolean and ranked queries. For boolean queries, unit tests have been created for all different types of queries that the user might enter: single term queries, ANDQuery, ORQuery, a mix of AND/OR, and a test for terms not found in the corpus. For each of the ranking schemes, unit tests have been created for single terms, multiple terms, and common terms.  

