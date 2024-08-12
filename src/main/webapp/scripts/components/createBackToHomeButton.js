import {getInitialContent} from "../utils/profileContentManager.js";
import {attachProfileListeners} from "../display/displayProfilePage.js";

// Creates the HTML for the "back to home" button
export const createBackToHomeButton = () => {
    const backButtonContainer = document.createElement('div');
    backButtonContainer.classList.add('back-button-container');
    backButtonContainer.innerHTML = `
    <button class ="back-to-home-button"> Back to Home </button>
    `;

    return backButtonContainer;
}

// Attaches an event listener to the "back to home" button
export const attachBackToHomeListener = (mainElement, backButton) => {
    backButton.addEventListener('click', event => {

        mainElement.innerHTML = getInitialContent();
        attachProfileListeners();

    });
}

