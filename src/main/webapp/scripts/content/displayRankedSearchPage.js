import {appendBackToHomeButton} from "../utils/appendBackToHomeButton.js";
import {removeMainElements} from "../utils/removeMainElements.js";
import {createSearchBar} from "./createSearchBar.js";
import {verifyQueryDispatch} from "../utils/verifyQueryDispatch.js";

let rankedMode = null;
export const displayRankedSearchPageInstructions = (buttonId) => {
    removeMainElements();
    const instructions = createRankedInstructions();
    const mainElement = document.querySelector('main');
    mainElement.insertBefore(instructions, mainElement.firstChild);
    const nextButton = document.querySelector('#next-button');
    nextButton.addEventListener('click', () => {
        displayRankedSearchPage(buttonId);
    });
}

// Creates HTMl for ranked retrieval instructions
const createRankedInstructions = () => {
    const instructions = document.createElement('div');
    instructions.classList.add('overview');
    instructions.classList.add('margin-horizontal-10rem');
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
        <div class="flex-row">
            <button id="next-button">Next</button>
        </div>
    `;

    return instructions;
}

// When the next button is clicked, the ranked modes, search bar, and back button are displayed
export const displayRankedSearchPage = (buttonId) => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const rankedModes = createRankedModes();
    const searchBar = createSearchBar();

    const header = document.createElement('h2');
    header.textContent = 'Select Ranking Scheme';

    mainElement.appendChild(header);
    mainElement.appendChild(rankedModes);
    mainElement.appendChild(searchBar);
    appendBackToHomeButton();
    toggleModeSelection();
    attachQuerySubmitListener(buttonId);
}

// Creates HTML for the various ranked retrieval modes the user can select from
const createRankedModes = () => {

    const rankedModes = document.createElement('div');
    rankedModes.classList.add('card-container');

    rankedModes.innerHTML = `
    <div class="card">
        <h3> Default </h3>
        <button id ="default-ranked" class="ranked-button"> Select </button>
  
    </div>
    <div class="card">
        <h3> TFIDF </h3>
        <button id ="tfidf-ranked" class="ranked-button"> Select </button>
    </div>
    <div class="card">
        <h3> Okapi BM25 </h3>
        <button id ="okapi-ranked" class="ranked-button"> Select </button>
    </div>
    <div class="card">
        <h3> Wacky </h3>
        <button id ="wacky-ranked" class="ranked-button"> Select </button>
    </div>
    `;

    return rankedModes;
}

// Creates a listener on each ranked mode button that will toggle (select) one of the ranked modes at a time
const toggleModeSelection = () => {
    const rankModeButtons = document.querySelectorAll('.ranked-button');
    rankModeButtons.forEach(button => {
        button.addEventListener('click', (event) => {
            document.querySelector?.(".active")?.classList?.remove('active'); // Deselects the previously selected ranked mode (if there was one)
            event.currentTarget.classList.add('active'); // Selects the new mode
            rankedMode = event.currentTarget.id;
            console.log(`selecting ${event.currentTarget.id}`)
        })
    })
}

// Creates listener for submitting a query
const attachQuerySubmitListener = (buttonId) => {
    const form = document.querySelector('#search-form');
    form.addEventListener('submit', (event) => {
        event.preventDefault();

        // Creates error message if no mode was selected
        if (rankedMode === null) {
            createErrorMessage();

        } else {
            console.log(`button id ${buttonId}`)
            verifyQueryDispatch(buttonId, rankedMode);
            rankedMode = null;
        }

    });
}

// Creates the HTML and CSS for the error message that will be displayed if no mode was selected
const createErrorMessage = () => {
    const errorMsg = document.createElement('p');
    errorMsg.textContent = 'Please select a ranked mode before submitting a query. ';
    errorMsg.style.color = '#880808';
    const searchbar = document.querySelector('.search-div');
    const box = document.querySelector('.box');
    searchbar.insertBefore(errorMsg, box);
}

