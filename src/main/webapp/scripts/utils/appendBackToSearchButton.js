import {displayRankedSearchPage} from "../content/displayRankedSearchPage.js";
import {displayBooleanSearchPage} from "../content/displayBooleanSearchPage.js";

/*
This file contains methods for creating the "back to search" button. This button is used to take the user from the results page
back to either the "boolean search" or the "ranked search" page
*/
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