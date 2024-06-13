import {getInitialContent} from "./homepageContentManager.js";
import {initializeHomePageEventListeners} from "../setup.js";

/*
This file contains methods for creating the "back to home" button, which simply displays the homepage when clicked.
 */
export const appendBackToHomeButton = () => {
    const backButton = createBackButton();
    const mainElement = document.querySelector('main');
    mainElement.appendChild(backButton);
    attachListener(mainElement, backButton);
}

// Creates the HTML for the "back to home" button
const createBackButton = () => {
    const backButtonContainer = document.createElement('div');
    backButtonContainer.classList.add('back-button-container');
    backButtonContainer.innerHTML = `
    <button class ="back-to-home-button"> Back to Home </button>
    `;

    return backButtonContainer;

}

// Attaches an event listener to the "back to home" button
const attachListener = (mainElement, backButton) => {
    backButton.addEventListener('click', event => {
        mainElement.innerHTML = getInitialContent(); // Sets the main element HTML to that of the initial homepage content
        initializeHomePageEventListeners(); // Reinitializes the homepage event listeners
    });
}

