import {removeMainElements} from "../utils/removeMainElements.js";
import {createSidebar} from "../components/createSidebar.js";

// Creates the documentation page
export const displayDocumentationPage = (event) =>
{

    removeMainElements();
    const sidebar = createSidebar();
    const introSection = createIntroductionSection();
    const techSection = createTechnologyStackSection();
    const indexingSection = createIndexingSection();
    const storageSection = createStorageSection();
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
    docsContainer.appendChild(storageSection);
    docsContainer.appendChild(booleanSection);
    docsContainer.appendChild(rankedSection);
    flexContainer.appendChild(docsContainer);
    mainElement.appendChild(flexContainer);

}

// Creates the HTML for the introduction section
const createIntroductionSection = () =>
{
    const introSection = document.createElement('div');

    introSection.innerHTML = `
        <h3 id="documentation__intro-section" class="section site__h3"> Introduction </h3>
        <p> 
        This web application functions as a search engine and indexer, enabling users to search a default set of documents or 
        upload and query their own collections. The default collection includes 30,000 webpages scraped from NPS.org (National 
        Park Service pages). If users upload their own collection, it must consist entirely of either .TXT or .JSON files. The 
        directory must be <= 50 MB, and a limit of 3 directories per user is set.
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
const createTechnologyStackSection = () =>
{
    const techSection = document.createElement('div');

    techSection.innerHTML = `
        <h3 id="documentation__tech-stack-section" class="section site__h3">Application Architecture</h3>
        <p>
        The front end of the application is built using vanilla JavaScript, CSS, and HTML. Only two HTML pages are used; one
        for the homepage, and one to display the Google Login. All other pages are dynamically created through removing and 
        appending elements to the document's main section. The back-end follows the Jakarta EE (Java) specification and uses 
        the Jersey Server implementation for its REST API. 
        </p>
        <img class="arch-diagram" src="../../images/arch-diagram.png" height="500" width="1000" >
        <p>
        The REST API handles processing queries and communicates with a Postgres database to make responses as efficient as possible. 
        There are two databases used; one contains the default corpus and a single table, and the other contains a separate
        table for each of the uploaded directories (where the table name is the unique identifier built from the hashed user ID and directory
        name). Maven is used as a build automation tool, streamlining development by managing dependencies, compiling source code, and 
        packaging the application. A GitHub Actions workflow has been implemented to enable continuous integration and continuous deployment (CI/CD) 
        for the project. This workflow automatically builds and deploys the application to the Azure App Service whenever changes are pushed to the 
        remote repository. By leveraging this automated process, all updates to the codebase are seamlessly reflected in the Azure-hosted application, 
        ensuring that the live environment stays up-to-date with the latest developments.
        </p>
       
        
    `;
    return techSection;
}

// Creates HTML for the indexing section
const createIndexingSection = () =>
{
    const indexingSection = document.createElement('div');

    indexingSection.innerHTML = `
        <h3 id="documentation__indexing-section" class="section site__h3">Indexing</h3>
        <p> 
        Creating an index is crucial for speeding up the search engine's response time. By investing time upfront to build an index for a 
        given corpus, the time it takes to retrieve search results for individual queries is significantly decreased. 
        </p>
        <h4>Indexing Flowchart</h4>
        <img src="../../images/indexing_flowchart.png" width = "1200">
        <p>
        During the indexing process, each document is analyzed word by word. Every word is stemmed, reducing it to its base 
        form to ensure that variations of the same word are consistently indexed. These base terms are stored in a hashmap, 
        with each term linked to a list of postings. Each posting includes the document's unique ID within the corpus and a 
        list of positions where the term appears in that document.
        </p>
        <p>
        As the index is being constructed, metadata about the directory—such as document weights and the average number of tokens 
        per document—is also collected. Once the index and metadata files are complete, they are serialized and stored in an Azure 
        Blob Storage container. The container name is generated using a hashed user ID and the corpus name to ensure uniqueness. 
        For each uploaded directory, a new container is created to store all these files.
        </p>
       
    `;
    return indexingSection;
}

const createStorageSection = () =>
{
    const indexingSection = document.createElement('div');

    indexingSection.innerHTML = `
        <h3 id="documentation__storage-section" class="section site__h3"> Storage Details </h3>
        <p> 
        As described earlier, after the in-memory hashmap is created, it is serialized along with the metadata files and 
        uploaded to a designated container in Azure Blob Storage. Each container, representing a unique directory, contains 
        four .bin files: postings.bin (the serialized hashmap), and two other .bin files for document weights and average token 
        metadata. Additionally, a .zip file holds the original directory files.
        </p>
        <p>
        Following the upload, a PostgreSQL database is created to facilitate efficient term lookups within the postings.bin file. 
        The database table contains two columns: the term and its corresponding byte position within postings.bin. There are two 
        databases—one for the default directory and one for user-uploaded directories. For each uploaded directory, a new table with 
        a unique identifier is created. When a user selects a directory to query, the corresponding table is accessed, allowing for 
        efficient term lookups and retrieval of associated postings. 
        </p>
     
       
    `;
    return indexingSection;
}

// Creates HTML for the boolean section
const createBooleanSection = () =>
{
    const booleanSection = document.createElement('div');

    booleanSection.innerHTML = `
        <h3 id="documentation__boolean-section" class="section site__h3"> Boolean Retrieval </h3>
        <p>
        Boolean retrieval is a classic model in information retrieval that uses Boolean logic to match documents to a user's query. This approach employs the AND and 
        OR operators. Terms separated by a space are combined using AND, while terms separated by a "+" are combined using OR. A query can have multiple AND conditions 
        combined with OR. For example, "dog cat + snake rat + walrus goose" retrieves documents containing either "dog" and "cat," or "snake" and "rat," or "walrus" and 
        "goose." Additionally, Boolean retrieval accepts phrase queries, which are enclosed in double quotes. Documents matching a phrase query must contain the exact phrase 
        within the text.
        </p>
        <img class="arch-diagram" src="../../images/boolean-diagram.png" height="400" width="500">
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
        
        
    `;
    return booleanSection;

}

// Creates HTML for the ranked section
const createRankedSection = () =>
{
    const rankedSection = document.createElement('div');

    rankedSection.innerHTML = `
        <h3 id="documentation__ranked-section" class="section site__h3"> Ranked Retrieval </h3>
        <p>
        In ranked retrieval, the documents of a corpus are ranked based on their suspected relevance to a given query. A 
        basic ranking scheme may look to term frequency as an indicator for relevant documents. For example if a user entered 
        "dogs", intuitively, it might make sense to rank the documents where dog appears frequently higher than those in which 
        it doesn't. However, it soon becomes apparent that not all terms in the query are equal. If the query was instead "the dogs", 
        a document with frequent use of "the" (and no mention of dogs) might rank higher than a document actually talking about dogs. 
        Thus, for each of the schemes below, weights are given for both the terms in the query and the terms in each document.
        </p>
        <p>
        One of several different ranking schemes can be selected for the ranked retrieval mode. Each ranking scheme varies in how weights are calculated for 
        the terms in documents, as well as the terms in the query. Below shows how these weights, the weight of the term in the query (Wqt), the weight of the
        term in the document (Wdt), and the document normalizing value (Ld). The "Ld" value accounts for varying document lengths.
        </p>
        <p><em> Default </em></p>
        <img class="ranked" src="../../images/default-ranked.png" height="150" width="200">
        <p><em> TF-IDF </em></p>
        <img class="ranked" src="../../images/tfidf-ranked.png" height="150" width="200">
        <p><em> Okapi BM25 </em></p>
        <img class="ranked" src="../../images/okapi-ranked.png" height="150" width="200">
        <p><em> Wacky </em></p>
        <img class="ranked" src="../../images/wacky-ranked.png" height="150" width="200">
        
        
        </p>
        
        
    `;
    return rankedSection;
}