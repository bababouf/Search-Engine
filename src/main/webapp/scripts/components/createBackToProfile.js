
import {displayProfilePage} from "../display/displayProfilePage";

export const createBackToProfile = () =>
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
        displayProfilePage();
    });

    return buttonDiv;
};