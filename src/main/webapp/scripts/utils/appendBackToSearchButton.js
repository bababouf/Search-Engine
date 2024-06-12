
import {displayRankedSearchPage} from "../content/displayRankedSearchPage.js";
import {displayBooleanSearchPage} from "../content/displayBooleanSearchPage.js";

// Appends the "back-to-search" button to the main element
export const appendBackToSearchButton = (buttonId) => {
    const backButton = createBackButton();
    const mainElement = document.querySelector('main');
    mainElement.appendChild(backButton);
    attachListener(mainElement, backButton, buttonId);
}

// Creates HTML for the "back-to-search" button
const createBackButton = () => {

    const backButtonContainer = document.createElement('div');
    backButtonContainer.classList.add('back-button-container');
    backButtonContainer.innerHTML = `
    <button class ="back-to-search-button"> Try another Query </button>
    `;

    return backButtonContainer;

}

// Attaches eventListener for "back-to-search" button
const attachListener = (mainElement, backButton, buttonId) => {
    backButton.addEventListener('click', event => {
        if(buttonId === 'ranked-button')
        {
            displayRankedSearchPage(buttonId);
        }
        else if(buttonId === 'boolean-button')
        {
            displayBooleanSearchPage(buttonId);
        }

    });
}