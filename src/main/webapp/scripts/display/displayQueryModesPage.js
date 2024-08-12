import {removeMainElements} from "../utils/removeMainElements.js";
import {
    attachBackToHomeListener,
    createBackToHomeButton
} from "../components/createBackToHomeButton.js";
import {displayInstructionsAndLoadingSpinner} from "./displayBooleanSearchPage.js";
import {displayRankedSearchPageInstructions} from "./displayRankedSearchPage.js";

/*
This file contains methods for creating and displaying the "query modes" selection page. The HTML to display the
page is created, and event listeners are attached to each of the query mode buttons.
 */
export const displayQueryModesPage = () => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const modesDiv = createQueryModes();

    mainElement.appendChild(modesDiv);

    const backToHomeButton = createBackToHomeButton();
    attachBackToHomeListener(mainElement, backToHomeButton);
    mainElement.appendChild(backToHomeButton);
    //appendBackToHomeButton();
    attachModeButtonListeners();
}

// Creates the HTML for the boolean and ranked method selection
const createQueryModes = () => {
    const modesDiv = document.createElement('div');

    modesDiv.innerHTML = `
    <h2>Select Query Mode</h2>
    <div class = "card-container">
    <div class="card">
        <h3> 
        <img src="../../images/venn-diagram-logo.png" height="40" width="40">
        Boolean Retrieval Mode </h3>
        
        <p> Configures the program to handle boolean queries. </p>
        <button class = "query-modes__mode-button" id="boolean-button"> Go! </button>  
    </div>
    <div class="card">
        <h3>
         <img src="../../images/search-glass.png" height="40" width="40">
         Ranked Retrieval Mode </h3>
       
        <p> Configures the program to handle ranked requests. </p>
        <button class = "query-modes__mode-button" id="ranked-button"> Go! </button>  
    </div>
    </div>
    `;

    return modesDiv;
}

// Creates listeners to each of the method buttons
const attachModeButtonListeners = () => {
    const modeButtons = document.querySelectorAll(".query-modes__mode-button");
    modeButtons.forEach(button => {
        button.addEventListener("click", queryModeDispatch);
    });
}

// Calls the appropriate method for handling each of the "query mode" button clicks
const queryModeDispatch = (event) => {
    const buttonId = event.currentTarget.id;

    if (buttonId === 'boolean-button') {
        displayInstructionsAndLoadingSpinner(buttonId);
    }
    else if (event.currentTarget.id === 'ranked-button')
    {
        displayRankedSearchPageInstructions(buttonId);
    }

}
