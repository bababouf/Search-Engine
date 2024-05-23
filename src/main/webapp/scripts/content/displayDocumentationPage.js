import {removeMainElements} from "../utils/removeMainElements.js";
import {createSidebar} from "./createSidebar.js";


export const displayDocumentationPage = (event) => {

    removeMainElements();
    const sidebar = createSidebar();
    const introSection = createIntroductionSection();
    const techSection = createTechnologyStackSection();
    const indexingSection = createIndexingSection();
    const queryingSection = createQueryingSection();
    const mainElement = document.querySelector('main');
    const flexContainer = document.createElement('div');
    const docsContainer = document.createElement('div');


    flexContainer.classList.add('flex-container');
    docsContainer.classList.add('documentation');
    flexContainer.appendChild(sidebar);



    docsContainer.appendChild(introSection);
    docsContainer.appendChild(techSection);
    docsContainer.appendChild(indexingSection);
    docsContainer.appendChild(queryingSection);
    flexContainer.appendChild(docsContainer);
    mainElement.appendChild(flexContainer);

}

const createIntroductionSection = () => {
    const introSection = document.createElement('div');
    introSection.classList.add('documentation');
    introSection.innerHTML = `
        <h2 id="documentation__intro-section"> Introduction </h2>
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
        <a href="https://github.com/bababouf/Search-Engine/tree/master">GitHub Repository</a>
           
    `;
    return introSection;
}

const createTechnologyStackSection = () => {
    const techSection = document.createElement('div');
    techSection.classList.add('documentation');
    techSection.innerHTML = `
        <h2 id="documentation__tech-stack-section">Technology Stack</h2>
        <p>
        The front end of the application is built using vanilla JavaScript, CSS, and HTML. A single HTML page (index.html) is 
        employed, with user interactions triggering various scripts to dynamically modify the content without reloading the page.
        The back end follows the Jakarta EE (Java) specification and uses the Jersey Server implementation for its REST API. The 
        most common requests from the browser to the server are POST requests containing the user's query. For these requests, the 
        server interacts with a MySQL database, which has a two-column structure where each row contains a term and its byte position 
        in another binary file. This design enables efficient term lookup and quick query responses. Maven is used as a build automation 
        tool, streamlining development by managing dependencies, compiling source code, and packaging the application. It uses a 
        Project Object Model (POM) file to define the project's structure and configurations, ensuring efficient project management 
        and consistency across development environments.
        </p>
    `;
    return techSection;
}

const createIndexingSection = () => {
    const indexingSection = document.createElement('div');
    indexingSection.classList.add('documentation');
    indexingSection.innerHTML = `
        <h2 id="documentation__indexing-section">Indexing</h2>
        <h3>Overview</h3>
        <p> 
        Creating an index is crucial for speeding up the search engine's response time. By investing time upfront to build an index for a 
        given corpus, we can significantly reduce the time it takes to retrieve search results for individual queries.
        </p>
        <p>
        During indexing, each document is examined word by word. Every word is transformed to its base form (stemmed) to ensure consistency 
        when indexing variations of the same word. For example, the word run and it's variations (running, ran, runner) would all map to a single
        term. These base terms are then stored in a hashmap, each associated with a list of postings. Each posting contains the document's ID, 
        which uniquely identifies the term within the corpus, along with a list of positions where the term occurs in that document.<br>
        </p>
        <p>
        The index itself is stored in a binary file. When writing terms to disk, a specific sequence is followed: document frequency, document ID, 
        number of positions, and the positions themselves. To streamline term lookup during queries, we store the byte position of each term in a 
        MySQL table, which consists of two columns: the term and its corresponding position. 
        </p>
        <h3>Critical Classes</h3>
        <p>
        This section will give details for the most important classes used in the indexing process, in the order in which they are used. The path to the method, 
        along with the method signature is given. <br>
        As a note, each path starts from the modules directory, which can be found <a href="https://github.com/bababouf/Search-Engine/tree/test-branch/src/main/java/modules">here.</a>
        </p>
        <p>
        <strong><em>1. modules.text.NonBasicTokenProcessor.processToken(String token)</strong></em><br>
        The <em>processToken</em> method is called on every term in each of the corpus documents. This method takes care of the preprocessing, which involves
        the stemming mentioned in the last section, as well as basic transformations of terms such as lowercasing and splitting hyphenated terms into their individual 
        terms. 
        </p>
        <p>
        <strong><em>2. modules.indexing.PositionalInvertedIndex.addTerm(String term, int documentId, int position)</strong></em><br>
        The PositionalInvertedIndex class contains the hashmap that will eventually be moved to disk. Each of the stemmed terms is added to this hashmap through
        the <em>addTerm</em> method. Each key in the hashmap corresponds to a stemmed term, and each value is a list of postings. As mentioned above, the posting
        list contains a document's ID, and all the positions (integer values) where that term shows up in a document. 
        </p>
        <strong><em>3. modules.indexing.DiskIndexWriter.calculateAndWriteDocumentWeights(Map< String, Integer > termFrequency, Path absolutePath, int id, int documentTokens, int bytes)</strong></em><br>
        Once the last term in a document is processed and added to the hashmap, the <em>calculateAndWriteDocumentWeights</em> method is called to store important 
        information obtained from each document. These include the length of the document (used to normalize very short documents/very long documents), the number 
        of tokens, number of ASCII bytes, and the average term frequency of terms in the document.
        </p>
        <p>
        <strong><em>4. modules.indexing.DiskIndexWriter.writeAverageTokensForCorpus(String pathToDocWeights, double averageTokens)</strong></em><br>
        After all documents have been processed, the <em>writeAverageTokensForCorpus</em> is called to write the average number of tokens to the end of the 
        docWeights.bin file. 
        </p>
    `;
    return indexingSection;
}
const createQueryingSection = () => {
    const queryingSection = document.createElement('div');
    queryingSection.classList.add('documentation');
    queryingSection.innerHTML = `
        <h2 id="documentation__querying-section">Querying </h2>
        <h3>Overview</h3>
        <p>
        As mentioned in the introduction section, the two accepted modes of query are boolean retrieval and ranked retrieval. The boolean retrieval mode accepts
        basic AND/OR queries, as well as ORs of ANDs. The ranked retrieval mode offers four different ranking schemes, each using a slightly different algorithm to 
        rank the documents. This section will give a detailed look into how each of the modes work, and present the most important Java classes used. 
        </p>
        <h3>Boolean Retrieval</h3>
        <p>
        Boolean retrieval is a classic model in information retrieval that uses Boolean logic to match documents to a user's query. This approach employs the AND and 
        OR operators. Terms separated by a space are combined using AND, while terms separated by a "+" are combined using OR. A query can have multiple AND conditions 
        combined with OR. For example, "dog cat + snake rat + walrus goose" retrieves documents containing either "dog" and "cat," or "snake" and "rat," or "walrus" and 
        "goose." Additionally, Boolean retrieval accepts phrase queries, which are enclosed in double quotes. Documents matching a phrase query must contain the exact phrase 
        within the text.
        </p>
        <p>
        Boolean searches will provide ALL the documents that match a given query, and there is no ranking of documents. If a user queries for "dogs", every document 
        containing the stemmed root of dogs will be returned, and in no particular order. 
        </p>
        <h3>Critical Classes</h3>
        <p>
        When a Boolean query first enters the system, it needs to be parsed into its individual components. A QueryComponent is an interface representing a part of a 
        larger query, whether it is a literal string or a combination of other components. For instance, the query "dogs cats" would be parsed to return an ANDQuery 
        component, which is a concrete class that implements the QueryComponent interface. Other concrete classes include OrQuery, PhraseLiteral, and TermLiteral. The boolean 
        parser will be explained below in more detail. 
        </p>
        <p>
        <strong><em>1. modules.queries.BooleanQueryParser.parseQuery(String query)</strong></em><br>
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
        <strong><em>2. modules.queries.QueryComponent.getPostings(DiskPositionalIndex index)</strong></em><br>
        Each of the concrete classes will implement a getPostings method, which takes as a parameter the index. 
        </p>
        <h3>Ranked Retrieval</h3>
        <h3>Critical Classes</h3>
    `;
    return queryingSection;

}