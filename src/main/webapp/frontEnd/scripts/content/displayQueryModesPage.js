import {removeMainElements} from "../utils/removeMainElements.js";
import {createBackButton} from "./createBackButton.js";
import {displayBooleanSearchPage} from "./displayBooleanSearchPage.js";
import {displayRankedSearchPage} from "./displayRankedSearchPage.js";


export const displayQueryModesPage = () => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const modesDiv = createQueryModes();
    const backButton = createBackButton();

    mainElement.appendChild(modesDiv);
    mainElement.appendChild(backButton);
    attachModeButtonListeners();
}

const createQueryModes = () => {
    const modesDiv = document.createElement('div');
    modesDiv.classList.add('card-container');

    modesDiv.innerHTML = `
    <div class="card">
        <h3> Boolean Retrieval Mode </h3>
        <p> Configures the program to handle boolean queries. </p>
        <button class = "mode-button" id="boolean-button"> Go! </button>  
    </div>
    <div class="card">
        <h3> Ranked Retrieval Mode </h3>
        <p> Configures the program to handle ranked requests. </p>
        <button class = "mode-button" id="ranked-button"> Go! </button>  
    </div>
    `;

    return modesDiv;
}

const attachModeButtonListeners = () => {
    const modeButtons = document.querySelectorAll(".mode-button");
    modeButtons.forEach(button => {
        button.addEventListener("click", queryModeDispatch);
    });
}

const queryModeDispatch = (event) => {

    const buttonId = event.currentTarget.id;

    if (buttonId === 'boolean-button') {
        displayBooleanSearchPage(buttonId);
    } else if (event.currentTarget.id === 'ranked-button') {
        displayRankedSearchPage(buttonId);
    }

}
