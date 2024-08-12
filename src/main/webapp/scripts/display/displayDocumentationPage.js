import {removeMainElements} from "../utils/removeMainElements.js";
import {createSidebar} from "../components/createSidebar.js";

// Creates the documentation page
export const displayDocumentationPage = (event) => {

    removeMainElements();
    const sidebar = createSidebar();
    const introSection = createIntroductionSection();
    const techSection = createTechnologyStackSection();
    const indexingSection = createIndexingSection();
    const booleanSection = createBooleanSection();
    const rankedSection = createRankedSection();
    const mainElement = document.querySelector('main');
    const flexContainer = document.createElement('div');
    const docsContainer = document.createElement('div');

    flexContainer.classList.add('flex-container');
    docsContainer.classList.add('documentation');
    flexContainer.appendChild(sidebar);

    docsContainer.appendChild(introSection);
    docsContainer.appendChild(techSection);
    docsContainer.appendChild(indexingSection);
    docsContainer.appendChild(booleanSection);
    docsContainer.appendChild(rankedSection);
    flexContainer.appendChild(docsContainer);
    mainElement.appendChild(flexContainer);

}

// Creates the HTML for the introduction section
const createIntroductionSection = () => {
    const introSection = document.createElement('div');
    introSection.classList.add('documentation');
    introSection.innerHTML = `
        <h2 id="documentation__intro-section" class="section"> Introduction </h2>
        <p> 
        This web application functions as a search engine and indexer, enabling users to search a default set of documents or 
        upload and query their own collections. The default collection includes 30,000 webpages scraped from NPS.org (National 
        Park Service pages). If users upload their own collection, it must consist entirely of either .TXT or .JSON files.
        </p>
        <p>
        After selecting the corpus, the user can choose between two query modes: boolean retrieval or ranked retrieval. 
        Boolean queries must be in disjunctive normal form, accepting standard AND/OR queries as well as ORs of ANDs. Ranked 
        retrieval allows for "bag of words" queries, similar to typical web browser searches. This mode accepts any query and 
        returns the top ten most relevant documents. Additionally, users can select from several ranking schemes in ranked 
        retrieval: default, TF-IDF, Okapi, and Wacky, each offering a unique approach to ranking documents and query terms.
        </p>
        <a href="https://github.com/bababouf/Search-Engine/tree/main">Link to GitHub Repository</a>
           
    `;
    return introSection;
}

// Creates HTML for the technology section
const createTechnologyStackSection = () => {
    const techSection = document.createElement('div');
    techSection.classList.add('documentation');
    techSection.innerHTML = `
        <h2 id="documentation__tech-stack-section" class="section">Application Architecture</h2>
        <p>
        The front end of the application is built using vanilla JavaScript, CSS, and HTML. A single HTML page (index.html) is 
        employed, with user interactions triggering various scripts to dynamically modify the content without reloading the page.
        The back-end follows the Jakarta EE (Java) specification and uses the Jersey Server implementation for its REST API. 
        </p>
        <img class="arch-diagram" src="../../../../../target/Search-Engine-1.0-SNAPSHOT/images/arch-diagram.png" height="430" width="800" >
        <p>
        The REST API handles processing queries and communicates with a MYSQL database to make responses as efficient as possible. 
        Maven is used as a build automation tool, streamlining development by managing dependencies, compiling source code, and 
        packaging the application.
        </p>
        <p>
        The web application is deployed using Microsoft's Azure App Service. This is a platform as a service (PaaS) which provides complete 
        managed hosting of the application (meaning Microsoft's hardware is being used as the server). 
        </p>
        <br>
        
    `;
    return techSection;
}

// Creates HTML for the indexing section
const createIndexingSection = () => {
    const indexingSection = document.createElement('div');
    indexingSection.classList.add('documentation');
    indexingSection.innerHTML = `
        <h2 id="documentation__indexing-section" class="section">Indexing</h2>
        <p> 
        Creating an index is crucial for speeding up the search engine's response time. By investing time upfront to build an index for a 
        given corpus, the time it takes to retrieve search results for individual queries is significantly decreased. 
        </p>
        <h4>Indexing Flowchart</h4>
        <img src="../../../../../target/Search-Engine-1.0-SNAPSHOT/images/indexing_flowchart.png" width = "1200">
        <p>
        During indexing, each document is examined word by word. Every word is transformed to its base form (stemmed) to ensure consistency 
        when indexing variations of the same word. These base terms are then stored in a hashmap, each associated with a list of postings. 
        Each posting contains the document's ID, which uniquely identifies the term within the corpus, along with a list of positions where the term occurs in that document.<br>
        </p>
        <p>
        The index itself is stored in a binary file. When writing terms to disk, a specific sequence is followed: document frequency, document ID, 
        number of positions, and the positions themselves. To streamline term lookup during queries, we store the byte position of each term in a 
        MySQL table, which consists of two columns: the term and its corresponding position. 
        </p>
        <h3>Critical Classes</h3>
        <p>
        This section will provide the method signature and an explanation for the important classes shown in the indexing flowchart above.  <br>
        Each method signature is clickable, and will open up the code for a given method in a new tab.  
        </p>
        <p>
        <strong><em><a href="https://github.com/bababouf/Search-Engine/blob/test-branch/src/main/java/modules/text/NonBasicTokenProcessor.java#L20" target="_blank"> 1. modules.text.NonBasicTokenProcessor.processToken(token)</a></em></strong><br>
        This method is invoked for each token (an individual term) in every document. Each term undergoes stemming and is converted to lowercase. If the term is hyphenated, 
        it is split into its individual components, and each component is then stemmed and converted to lowercase. The processed term is then returned from the method.
        </p>
        <p>
        <strong><em><a href="https://github.com/bababouf/Search-Engine/blob/test-branch/src/main/java/modules/indexing/PositionalInvertedIndex.java#L30" target="_blank"> 2. modules.indexing.PositionalInvertedIndex.addTerm(term, documentID, position)</a></em></strong><br>
        The <em>addTerm</em> method will add a single term from a single documentID one position at a time. A term can appear in many documents, and it can appear many times 
        in a single document. This method is invoked for every processed token that was returned in the previous <em>processToken</em> method.
        </p>
        <p>
        <strong><em><a href="https://github.com/bababouf/Search-Engine/blob/test-branch/src/main/java/modules/indexing/DiskIndexWriter.java#L134" target="_blank"> 3. modules.indexing.DiskIndexWriter.writeDocumentWeights(termFrequencyMap, absolutePathToIndex, ID, documentTokens, bytes)</a></em></strong><br>
        Once the last term in a document is processed and added to the hashmap, this method stores important information obtained from each document. These include 
        the length of the document (used to normalize very short documents/very long documents), the number of tokens, number of ASCII bytes, and the average term 
        frequency of terms in the document. This information is important for calculating document rankings in the ranked retrieval mode. 
        </p>
        <p>
        <strong><em><a href="https://github.com/bababouf/Search-Engine/blob/test-branch/src/main/java/modules/indexing/DiskIndexWriter.java#L176" target="_blank"> 4. modules.indexing.DiskIndexWriter.writeAverageTokensForCorpus(String pathToDocWeights, double averageTokens)</a></em></strong><br>
        After all documents have been processed, the <em>writeAverageTokensForCorpus</em> is called to write the average number of tokens to the end of the 
        docWeights.bin file. 
        </p>
    `;
    return indexingSection;
}

// Creates HTML for the boolean section
const createBooleanSection = () => {
    const booleanSection = document.createElement('div');
    booleanSection.classList.add('documentation');
    booleanSection.innerHTML = `
        <h2 id="documentation__boolean-section" class="section"> Boolean Retrieval </h2>
        <p>
        Boolean retrieval is a classic model in information retrieval that uses Boolean logic to match documents to a user's query. This approach employs the AND and 
        OR operators. Terms separated by a space are combined using AND, while terms separated by a "+" are combined using OR. A query can have multiple AND conditions 
        combined with OR. For example, "dog cat + snake rat + walrus goose" retrieves documents containing either "dog" and "cat," or "snake" and "rat," or "walrus" and 
        "goose." Additionally, Boolean retrieval accepts phrase queries, which are enclosed in double quotes. Documents matching a phrase query must contain the exact phrase 
        within the text.
        </p>
        <img class="arch-diagram" src="../../../../../target/Search-Engine-1.0-SNAPSHOT/images/boolean-diagram.png" height="400" width="500">
        <p>
        Since boolean queries can be fairly complex and can consist of many terms, the first step is to break down a query into it's individual components. A query that only 
        contains one term is simple, and would only contain a single component representing that term. However, more complex queries may involve AND'ing together certain terms, 
        and OR'ing others. 
        </p>
        <p>
        A QueryComponent is an interface representing a part of a larger query, whether it is a literal string or a combination of other components. Concrete classes (the classes 
        that implement the QueryComponent interface) include the following classes: ORQuery, ANDQuery, PhraseLiteral, and TermLiteral. The example on the left shows how a query might 
        be decomposed; each subquery (group of terms split up by the "+") are AND'd together, and then an ORQuery is created to obtain the final results list. 
        </p>
        <h3>Critical Classes</h3>
        <p>
        <strong><em><a href="https://github.com/bababouf/Search-Engine/blob/test-branch/src/main/java/modules/queries/BooleanQueryParser.java#L42" target="_blank"> 1. modules.queries.BooleanQueryParser.parseQuery(query)</a></em></strong><br>
        The <em>parseQuery</em>  method accepts a raw query as a string parameter. The query is scanned to identify segments separated by "+" signs, which represent terms to be 
        OR'd. Each segment is processed as a subquery. 
        </p>
        <p>
        For each subquery segment, multiple literals (terms) are combined into an AndQuery component. If a subquery contains only one literal, a TermLiteral component is created. 
        This component is then added to a list of components. 
        </p>
        <p> 
        After processing all subqueries, a list of components is created. If the list contains only one component, the method returns that single QueryComponent. If the list contains 
        multiple components, the method returns an OrQuery containing those components.
        </p>
        <p>
        <strong><em><a href="https://github.com/bababouf/Search-Engine/blob/test-branch/src/main/java/modules/queries/QueryComponent.java#L20" target="_blank"> 2. modules.queries.QueryComponent.getPostings(index)</a></em></strong><br>
        Each of the concrete classes will implement a getPostings method, which takes as a parameter the index. Depending on which class is implementing the method, it can look widely 
        different. For example, the getPostings method for the ANDQuery class will take the intersection of two or more terms. In contrast, the ORQuery class will implement a method that 
        takes the union of two or more terms. The final list of documents is then returned from these methods.
        </p>
        
    `;
    return booleanSection;

}

// Creates HTML for the ranked section
const createRankedSection = () => {
    const rankedSection = document.createElement('div');
    rankedSection.classList.add('documentation');
    rankedSection.innerHTML = `
        <h2 id="documentation__ranked-section" class="section"> Ranked Retrieval </h2>
        <p>
        As mentioned above, one of several different ranking schemes can be selected for the ranked retrieval mode. Each ranking scheme varies in how weights are calculated for 
        the terms in documents, as well as the terms in the query. Similar to how the QueryComponent interface class was used in indexing, the RankingStrategy interface class 
        is implemented by each of the ranking scheme classes (the concrete classes). 
        </p>
        <img src="../../../../../target/Search-Engine-1.0-SNAPSHOT/images/ranking-strategy-diagram.png" height="400" width="1000">
        <p>
        The two pieces of information passed to the servlet that handles ranked queries is the user's query and the ranking scheme that the user selected. A dispatch class is used 
        to call the appropriate calculate method for the selected ranking scheme. This dispatch class is called RankedDispatch. 
        
        </p>
        <h3>Critical Classes</h3>
        <p>
        <strong><em><a href="https://github.com/bababouf/Search-Engine/blob/test-branch/src/main/java/modules/rankingSchemes/RankedDispatch.java#L25" target="_blank"> 1. modules.rankingSchemes.RankedDispatch(index, corpus)</a></em></strong><br>
        The first step in processing a ranked query is to configure the system so that it knows which corpus is being queried (since the user could have uploaded their own, 
        or chosen to query the default NPS corpus). An object of the RankedDispatch class is instantiated by simply calling the constructor and passing it the index and corpus. 
        </p>
        <p>
        <strong><em><a href="https://github.com/bababouf/Search-Engine/blob/test-branch/src/main/java/modules/rankingSchemes/RankedDispatch.java#L45" target="_blank"> 2. modules.rankingSchemes.RankedDispatch.calculate(rankingScheme, query)</a></em></strong><br>
        Once the RankedDispatch class is instantiated, the calculate method can be called, passing to it an instance of the ranking scheme class being used, as well as the query 
        string. Before calculations can be done, several preprocessing steps are done to lowercase and split the query by whitespace into its individual terms. The 
        RankedQueryParser.parseQuery method takes care of this. After parsing, a list of QueryComponents is returned
        </p>
        <p>
        <strong><em><a href="https://github.com/bababouf/Search-Engine/blob/test-branch/src/main/java/modules/rankingSchemes/RankedDispatch.java#L45" target="_blank"> 2. modules.rankingSchemes.RankedDispatch.calculateAccumulatorValue(rankingScheme, query)</a></em></strong><br>
        </p>
        
        
    `;
    return rankedSection;
}