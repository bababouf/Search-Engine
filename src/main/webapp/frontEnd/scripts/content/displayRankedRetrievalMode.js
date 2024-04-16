import { displayBackButton } from "./appendBackButtonToMain.js";

export const displayRankedRetrievalMode = () => {
    const instructions = document.createElement('div');
    instructions.innerHTML = `
        <h2>Instructions</h2>
        <p> Ranked queries do not have a specific form as they treat each query as a "bag of words". As a brief introduction, consider
        a scenario in which a query entered was simply "dogs". In this simple example, it's clear the user would want documents
        about dogs, and therefore it might be reasonable to rank documents that contain variations of "dog" highly. This 
        solution breaks down, however, when a user enters "the dogs"; it's easy to envision a scenario where a document contains "the" 
        many times, with no appearances of "dog" variations.   </p>
        <p> As a result of the above, a document's ranking is based off a combination of the query term's weight, as well as the 
        weight for that term in each of the documents. This program allows for the choice of four different ranking algorithms, each
        building off of the above basic ranking algorithm. Below are the different ranking algorithms that can be selected. </p>
    `;
    
    instructions.classList.add('overview');
    const mainElement = document.querySelector('main');
    mainElement.insertBefore(instructions, mainElement.firstChild);

    const instructionsDiv = document.createElement('div');
    instructionsDiv.classList.add('card-container');

    instructionsDiv.innerHTML = `
    <div class="card">
        <h3> Default </h3>
        <p> Returns documents containing each of the query terms being AND'd. </p>
        <p> Example: dogs cats birds </p>
        <button class="ranked-button"> Go! </button>
  
    </div>
    <div class="card">
        <h3> TFIDF </h3>
        <p>TFIDF, known as term frequency-inverse document frequency, will blah blah</p>
        <button class="ranked-button"> Go! </button>
    </div>
    <div class="card">
        <h3> Okapi BM25 </h3>
        <p> Terms can be AND'd together, and these groups can then be OR'd. </p> 
        <button class="ranked-button"> Go! </button>
    </div>
    <div class="card">
        <h3> Wacky </h3>
        <p> Returns documents that contain the queried phrase. </p>
        <button class="ranked-button"> Go! </button>
    </div>
    `;

    mainElement.appendChild(instructionsDiv);
    displayBackButton();
}