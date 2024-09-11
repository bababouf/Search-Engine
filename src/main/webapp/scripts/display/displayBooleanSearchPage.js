import {
    createBackToHomeButton
} from "../components/createBackToHomeButton.js";
import {createSearchBar} from "../components/createSearchBar.js";
import {removeMainElements} from "../utils/removeMainElements.js";
import {contactServlet, sendQueryToServlet} from "../utils/contactServlet.js";
import {createLoadingSpinner} from "../components/createLoadingSpinner.js";

// Constant variables used in contacting the servlet
const endpoint = '/booleansearch';
const rankedMode = null;

// Displays the instructions and loading spinner
export const displayBooleanInstructions = () =>
{
    // Remove all children from main element
    removeMainElements();

    // Attach instructions content and loading spinner
    const mainElement = document.querySelector('main');
    const instructions = createBooleanInstructions();
    const loadingSpinner = createLoadingSpinner();

    instructions.appendChild(loadingSpinner);
    mainElement.appendChild(instructions);

    // Allows servlet to download necessary files for the directory the user will be querying
    contactServlet(endpoint);
}

/*
The HTML to display the page is created, and an event listener is attached to the "submit query" button. The HTML for
the page consists of instructions,cards containing acceptable query formats (with examples), a search bar (with a submit
button), and the "back to home" button.
 */
export const displayBooleanSearchPage = () =>
{
    // Remove all children from the main element
    removeMainElements();

    // Attach instructions, acceptable query formats, search bar, and back button
    const mainElement = document.querySelector('main');
    const booleanSearchDiv = createBooleanInstructions();
    const queryFormats = createAcceptableQueryFormats();
    const searchBar = createSearchBar();
    const backButton = createBackToHomeButton();

    booleanSearchDiv.appendChild(queryFormats);
    booleanSearchDiv.appendChild(searchBar);
    booleanSearchDiv.appendChild(backButton);
    mainElement.appendChild(booleanSearchDiv);

    attachQuerySubmitListener();

}

// Creates the HTML for the boolean retrieval instructions
const createBooleanInstructions = () =>
{
    const booleanSearchDiv = document.createElement('div');
    booleanSearchDiv.classList.add('boolean-formats__content-container');

    booleanSearchDiv.innerHTML = `
        <p class = "boolean-formats__description"> The program accepts boolean queries that are in disjunctive normal form (DNF), also described as an OR of ANDS. 
        In addition, phrase queries can be entered by enclosing the query in double quotes. Below examples of acceptable form are shown.</p>
    `;

    return booleanSearchDiv;
}


// Creates HTML for the accepted query formats
const createAcceptableQueryFormats = () =>
{
    const queryFormats = document.createElement('div');
    queryFormats.classList.add('boolean-formats');

    queryFormats.innerHTML = `
    
    <h2 class="boolean-formats__title">Acceptable Query Formats</h2>
    <div class="boolean-formats__card-container">
        <div class="card bg-gradient" style="width: 300px">
            <h3 class="card-title">AND Query</h3>
            <p class="boolean-formats__card-description">Returns documents containing each of the query terms being AND'd.</p>
            <p class="boolean-formats__example"><strong>Example:</strong> dogs cats birds</p>
        </div>
        <div class="card bg-gradient" style="width: 300px">
            <h3 class="card-title">OR Query</h3>
            <p class="boolean-formats__card-description">Returns documents containing at least one of the terms being OR'd.</p>
            <p class="boolean-formats__example"><strong>Example:</strong> dogs + cats + birds</p>
        </div>
        <div class="card bg-gradient" style="width: 300px">
            <h3 class="card-title">OR of ANDs</h3>
            <p class="boolean-formats__card-description">One or more terms can be AND'd together, and two or more of these groups can then be OR'd.</p>
            <p class="boolean-formats__example"><strong>Example:</strong> dogs cats + birds walruses</p>
        </div>
        <div class="card bg-gradient" style="width: 300px">
            <h3 class="card-title">Phrase Query</h3>
            <p class="boolean-formats__card-description">Returns documents that contain the queried phrase.</p>
            <p class="boolean-formats__example"><strong>Example:</strong> "fires in yosemite"</p>
        </div>
    </div>
    
    `;

    return queryFormats;
}

// Creates listener for submitting a query
const attachQuerySubmitListener = (buttonId) =>
{
    const form = document.querySelector('#search-form');
    form.addEventListener('submit', (event) =>
    {
        event.preventDefault();
        sendQueryToServlet(endpoint, rankedMode, buttonId);
    });
}