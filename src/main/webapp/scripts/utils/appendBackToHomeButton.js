import {getInitialContent} from "./homepageContentManager.js";
import {initializeHomePageEventListeners} from "../setup.js";

export const appendBackToHomeButton = () => {
    const backButton = createBackButton();
    const mainElement = document.querySelector('main');
    mainElement.appendChild(backButton);
    attachListener(mainElement, backButton);
}

const createBackButton = () => {

    const backButtonContainer = document.createElement('div');
    backButtonContainer.classList.add('back-button-container');
    backButtonContainer.innerHTML = `
    <button class ="back-to-home-button"> Back to Home </button>
    `;

    return backButtonContainer;

}

const attachListener = (mainElement, backButton) => {
    backButton.addEventListener('click', event => {
        mainElement.innerHTML = getInitialContent();
        initializeHomePageEventListeners();
    });
}

