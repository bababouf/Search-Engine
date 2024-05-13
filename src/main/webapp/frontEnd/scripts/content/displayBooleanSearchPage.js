import {createBackButton} from "./createBackButton.js";
import {createSearchBar} from "./createSearchBar.js";
import {verifyQueryDispatch} from "../utils/verifyQueryDispatch.js";
import {removeMainElements} from "../utils/removeMainElements.js";

// Creates a "page" containing instructions for boolean retrieval, acceptable query formats, a search bar (with a submit button), and a back button
export const displayBooleanSearchPage = (buttonId) => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const instructions = createBooleanInstructions();
    const queryFormats = createAcceptableQueryFormats();
    const searchBar = createSearchBar();
    const backButton = createBackButton();

    mainElement.insertBefore(instructions, mainElement.firstChild);
    mainElement.appendChild(searchBar);
    mainElement.appendChild(queryFormats);
    mainElement.appendChild(backButton);

    attachQuerySubmitListener(buttonId);

}

// Creates the HTML for the boolean retrieval instructions
const createBooleanInstructions = () => {
    const instructions = document.createElement('div');
    instructions.classList.add('overview');

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
        <p> Terms can be AND'd together, and these groups can then be OR'd. </p>
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