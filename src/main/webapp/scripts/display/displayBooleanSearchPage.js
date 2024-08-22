import {
    attachBackToHomeListener,
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

    // Create and append to main the instructions and loading spinner
    const mainElement = document.querySelector('main');
    const instructions = createBooleanInstructions();
    const loadingSpinner = createLoadingSpinner();
    mainElement.insertBefore(instructions, mainElement.firstChild);
    mainElement.appendChild(loadingSpinner);

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

    // Create and append instructions, query format cards, search bar, and back to home button
    const mainElement = document.querySelector('main');
    const booleanSearchDiv = createBooleanInstructions();
    const queryFormats = createAcceptableQueryFormats();
    const searchBar = createSearchBar();
    booleanSearchDiv.appendChild(queryFormats);
    booleanSearchDiv.appendChild(searchBar);
    const backToHomeButton = createBackToHomeButton();
    booleanSearchDiv.appendChild(backToHomeButton);
    mainElement.appendChild(booleanSearchDiv);

    // Attach listeners to back button and query submit button
    attachBackToHomeListener();
    attachQuerySubmitListener();

}

// Creates the HTML for the boolean retrieval instructions
const createBooleanInstructions = () =>
{
    const booleanSearchDiv = document.createElement('div');
    booleanSearchDiv.classList.add('site__boolean-container');

    booleanSearchDiv.innerHTML = `
        <h2 class="site__h2">Instructions</h2>
        <p class = "site__boolean-instructions"> The program accepts boolean queries that are in disjunctive normal form (DNF), also described as an OR of ANDS. 
        In addition, phrase queries can be entered by enclosing the query in double quotes. Below examples of acceptable form are shown.</p>
    `;

    return booleanSearchDiv;
}


// Creates HTML for the accepted query formats
const createAcceptableQueryFormats = () =>
{
    const queryFormats = document.createElement('div');
    queryFormats.classList.add('card-container');

    queryFormats.innerHTML = `
    <div class="card">
        <h3 class="site__h3"> AND Query </h3>
        <p> Returns documents containing each of the query terms being AND'd. </p>
        <p> Example: dogs cats birds </p>
  
    </div>
    <div class="card">
        <h3 class="site__h3"> OR Query </h3>
        <p> Returns documents containing at least one of the terms being OR'd. </p>
        <p> Example: dogs + cats + birds </p>
         
    </div>
    <div class="card">
        <h3 class="site__h3"> OR of ANDs </h3>
        <p> One or more terms can be AND'd together, and two or more of these groups can then be OR'd. </p>
        <p> Example: dogs cats + birds walruses
         
    </div>
    <div class="card">
        <h3 class="site__h3"> Phrase Query </h3>
        <p> Returns documents that contain the queried phrase. </p>
        <p> Example: "fires in yosemite" </p>
        
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