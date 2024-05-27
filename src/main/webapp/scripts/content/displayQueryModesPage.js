import {removeMainElements} from "../utils/removeMainElements.js";
import {appendBackToHomeButton} from "../utils/appendBackToHomeButton.js";
import {displayBooleanSearchPage} from "./displayBooleanSearchPage.js";
import {displayRankedSearchPageInstructions} from "./displayRankedSearchPage.js";

// Displays a "page" containing the query modes (boolean and ranked) and a back button
export const displayQueryModesPage = () => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const modesDiv = createQueryModes();

    mainElement.appendChild(modesDiv);
    appendBackToHomeButton();

    attachModeButtonListeners();
}

// Creates the HTML for the boolean and ranked method selection
const createQueryModes = () => {
    const modesDiv = document.createElement('div');

    modesDiv.innerHTML = `
    <h2>Select Query Mode</h2>
    <div class = "card-container">
    <div class="card">
        <h3> Boolean Retrieval Mode </h3>
        <img src="../../images/venn-diagram-logo.png" height="55" width="75">
        <p> Configures the program to handle boolean queries. </p>
        <button class = "query-modes__mode-button" id="boolean-button"> Go! </button>  
    </div>
    <div class="card">
        <h3> Ranked Retrieval Mode </h3>
        <img src="../../images/mag-glass-logo.png" height="55" width="75">
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

// Depending on which query mode is selected, the proper method will be dispatched to handle the work
const queryModeDispatch = (event) => {

    const buttonId = event.currentTarget.id;

    if (buttonId === 'boolean-button') {
        displayBooleanSearchPage(buttonId);
    } else if (event.currentTarget.id === 'ranked-button') {
        displayRankedSearchPageInstructions(buttonId);
    }

}
