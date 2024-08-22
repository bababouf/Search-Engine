import {displayRankedSearchPage} from "../display/displayRankedSearchPage.js";
import {displayBooleanSearchPage} from "../display/displayBooleanSearchPage.js";


// Creates HTML for the "back-to-search" button
export const createBackToSearchButton = () =>
{
    const backButtonContainer = document.createElement('div');
    backButtonContainer.classList.add('back-button-container');
    backButtonContainer.innerHTML = `
    <button class ="back-to-search-button"> Try another Query </button>
    `;

    return backButtonContainer;

}

// Attaches eventListener for "back-to-search" button
export const attachBackToSearchListener = (mainElement, backButton, endpoint) =>
{
    backButton.addEventListener('click', event =>
    {
        if (endpoint === '/rankedsearch')
        {
            displayRankedSearchPage();
        }
        else
        {
            displayBooleanSearchPage();
        }

    });
}