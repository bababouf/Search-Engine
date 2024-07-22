import {appendBackToHomeButton} from "../utils/appendBackToHomeButton.js";
import {createSearchBar} from "./createSearchBar.js";
import {verifyQueryDispatch} from "../utils/verifyQueryDispatch.js";
import {removeMainElements} from "../utils/removeMainElements.js";
import {downloadCorpusAtServer} from "../utils/downloadCorpusAtServer.js";

/*
This file contains methods for creating and displaying the "boolean search" page. The HTML to display the page is
created, and an event listener is attached to the "submit query" button. The HTML for the page consists of instructions,
cards containing acceptable query formats (with examples), a search bar (with a submit button), and the "back to home"
button
 */
export const displayBooleanSearchPage = (buttonId) => {
    downloadCorpusAtServer();
    removeMainElements();
    const mainElement = document.querySelector('main');
    const instructions = createBooleanInstructions();
    const queryFormats = createAcceptableQueryFormats();
    const searchBar = createSearchBar();

    mainElement.insertBefore(instructions, mainElement.firstChild);
    mainElement.appendChild(queryFormats);
    mainElement.appendChild(searchBar);
    appendBackToHomeButton();

    attachQuerySubmitListener(buttonId);

}

// Creates the HTML for the boolean retrieval instructions
const createBooleanInstructions = () => {
    const instructions = document.createElement('div');
    instructions.classList.add('boolean-search__overview');
    instructions.classList.add('margin-horizontal-10rem');
    instructions.innerHTML = `
        <h2>Instructions</h2>
        <p> The program accepts boolean queries that are in disjunctive normal form (DNF), also described as an OR of ANDS. 
        In addition, phrase queries can be entered by enclosing the query in double quotes. Below examples of acceptable form are shown.</p>
    `;

    return instructions;
}

// Creates HTML for the accepted query formats
const createAcceptableQueryFormats = () => {
    const queryFormats = document.createElement('div');
    queryFormats.classList.add('card-container');

    queryFormats.innerHTML = `
    <div class="card">
        <h3> AND Query </h3>
        <p> Returns documents containing each of the query terms being AND'd. </p>
        <p> Example: dogs cats birds </p>
  
    </div>
    <div class="card">
        <h3> OR Query </h3>
        <p> Returns documents containing at least one of the terms being OR'd. </p>
        <p> Example: dogs + cats + birds </p>
         
    </div>
    <div class="card">
        <h3> OR of ANDs </h3>
        <p> One or more terms can be AND'd together, and two or more of these groups can then be OR'd. </p>
        <p> Example: dogs cats + birds walruses
         
    </div>
    <div class="card">
        <h3> Phrase Query </h3>
        <p> Returns documents that contain the queried phrase. </p>
        <p> Example: "fires in yosemite" </p>
        
    </div>
    `;

    return queryFormats;
}

// Creates listener for submitting a query
const attachQuerySubmitListener = (buttonId) => {
    const form = document.querySelector('#search-form');
    form.addEventListener('submit', (event) => {
        event.preventDefault();
        verifyQueryDispatch(buttonId);
    });
}