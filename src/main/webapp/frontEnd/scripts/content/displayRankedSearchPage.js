import {createBackButton} from "./createBackButton.js";
import {hideMainElements} from "../utils/hideMainElements.js";
import {createSearchBar} from "./createSearchBar.js";
import {verifyQueryDispatch} from "../utils/verifyQueryDispatch.js";


let rankedMode = null;
export const displayRankedSearchPage = (buttonId) => {
    const mainElement = document.querySelector('main');
    const instructions = displayRankedInstructions();
    mainElement.insertBefore(instructions, mainElement.firstChild);

    const nextButton = document.querySelector('#next-button');

    nextButton.addEventListener('click', () => {

        hideMainElements();
        const rankedModes = displayRankedModes();
        const searchBar = createSearchBar();
        const backButton = createBackButton();
        mainElement.appendChild(rankedModes);
        mainElement.appendChild(searchBar);
        mainElement.appendChild(backButton);
        createModeSelectListener();
        createQuerySubmitListener(buttonId);

    });

}

const displayRankedInstructions = () => {
    const instructions = document.createElement('div');
    instructions.classList.add('overview');
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
        <div class="card-container">
            <button id="next-button">Next</button>
        </div>
    `;

    return instructions;
}

const displayRankedModes = () => {

    const rankedModes = document.createElement('div');
    rankedModes.classList.add('card-container');

    rankedModes.innerHTML = `
    <div class="card">
        <h3> Default </h3>
        <p> Returns documents containing each of the query terms being AND'd. </p>
        <p> Example: dogs cats birds </p>
        <button id ="default-ranked" class="ranked-button"> Select </button>
  
    </div>
    <div class="card">
        <h3> TFIDF </h3>
        <p>TFIDF, known as term frequency-inverse document frequency, will blah blah</p>
        <button id ="tfidf-ranked" class="ranked-button"> Select </button>
    </div>
    <div class="card">
        <h3> Okapi BM25 </h3>
        <p> Terms can be AND'd together, and these groups can then be OR'd. </p> 
        <button id ="okapi-ranked" class="ranked-button"> Select </button>
    </div>
    <div class="card">
        <h3> Wacky </h3>
        <p> Returns documents that contain the queried phrase. </p>
        <button id ="wacky-ranked" class="ranked-button"> Select </button>
    </div>
    `;

    return rankedModes;
}

const createQuerySubmitListener = (buttonId) => {
    const form = document.querySelector('#search-form');
    form.addEventListener('submit', (event) => {
        event.preventDefault();
        if(rankedMode === null)
        {
            const errorMsg = document.createElement('p');
            errorMsg.textContent = 'Please select a ranked mode before submitting a query. ';
            errorMsg.style.color = '#880808';
            const searchbar = document.querySelector('.search-div');
            const box = document.querySelector('.box');
            searchbar.insertBefore(errorMsg, box);
        }
        else
        {
            verifyQueryDispatch(buttonId, rankedMode);
        }

    });
}

const createModeSelectListener = () => {
    const rankModeButtons = document.querySelectorAll('.ranked-button');
    rankModeButtons.forEach(button => {
        button.addEventListener('click', (event) => {
            document.querySelector?.(".active")?.classList?.remove('active');
            event.currentTarget.classList.add('active');
            console.log(event.currentTarget.id);
            rankedMode = event.currentTarget.id;
        })
    })
}