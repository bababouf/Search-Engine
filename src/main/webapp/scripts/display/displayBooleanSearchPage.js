import {
    attachBackToHomeListener,
    createBackToHomeButton
} from "../components/createBackToHomeButton.js";
import {createSearchBar} from "../components/createSearchBar.js";
import {removeMainElements} from "../utils/removeMainElements.js";
import {downloadCorpus} from "../utils/configureServlet.js";
import {sendQueryToServlet} from "../utils/contactServlet.js";
import {createLoadingSpinner} from "../components/createLoadingSpinner.js";


let queryMode = '';

// Displays the instructions and loading spinner
export const displayInstructionsAndLoadingSpinner = (buttonId) => {
    removeMainElements();
    queryMode = buttonId;
    const mainElement = document.querySelector('main');
    const instructions = createBooleanInstructions();
    const loadingSpinner = createLoadingSpinner();
    mainElement.insertBefore(instructions, mainElement.firstChild);
    mainElement.appendChild(loadingSpinner);
    downloadCorpus(buttonId);
}
/*
The HTML to display the page is created, and an event listener is attached to the "submit query" button. The HTML for
the page consists of instructions,cards containing acceptable query formats (with examples), a search bar (with a submit
button), and the "back to home" button.
 */
export const displayBooleanSearchPage = () => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const instructions = createBooleanInstructions();
    mainElement.insertBefore(instructions, mainElement.firstChild);
    const queryFormats = createAcceptableQueryFormats();
    const searchBar = createSearchBar();

    mainElement.appendChild(queryFormats);
    mainElement.appendChild(searchBar);
    const backToHomeButton = createBackToHomeButton();
    attachBackToHomeListener(mainElement, backToHomeButton)
    mainElement.appendChild(backToHomeButton);
    attachQuerySubmitListener(queryMode);

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
        let endpoint = '';
        if (buttonId === 'boolean-button')
        {
            endpoint = '/booleansearch';
        }
        else if (buttonId === 'ranked-button')
        {
            endpoint = '/rankedsearch';
        }

        sendQueryToServlet(endpoint, null, buttonId);
    });
}