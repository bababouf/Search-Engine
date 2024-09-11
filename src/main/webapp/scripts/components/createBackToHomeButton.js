import {getInitialContent} from "../utils/profileContentManager.js";
import {attachProfileListeners} from "../display/displayProfilePage.js";

// Creates the HTML for the "back to home" button
export const createBackToHomeButton = () =>
{
    const buttonDiv = document.createElement('div');
    buttonDiv.classList.add('button-container');

    const mainElement = document.querySelector("main");
    const button = document.createElement('button');
    button.textContent = 'Back to Profile';
    buttonDiv.appendChild(button);

    // Attach the listener inside the method
    button.addEventListener('click', () =>
    {
        mainElement.innerHTML = getInitialContent();
        attachProfileListeners();
    });

    return buttonDiv;
};


