import { displayQueryModes } from "./content/displayQueryModes.js";
import { displayUploadDirectory } from "./content/displayUploadDirectory.js";
import { hideMainElements } from "./utils/hideMainElements.js";

import {displayBackButton} from "./content/appendBackButtonToMain.js";
let initialMainContent;

const captureInitialMainContent = () => {
    const mainElement = document.querySelector('main');
    initialMainContent = mainElement.innerHTML;
};

window.addEventListener('DOMContentLoaded', () => {
    captureInitialMainContent();
});
console.log('hi');
const mainElement = document.querySelector('main');

    mainElement.addEventListener("click", (event) => {
        const target = event.target;
        if (target.matches(".directory-selection-button")) {
            hideMainElements(); 
            
            if (target.id === 'default-directory-button') {
                // Async GET /search/booleansearch
                displayQueryModes(event);
                displayBackButton();
            } else if (target.id === 'upload-directory-button') {
                displayUploadDirectory(event);
                displayBackButton();
            }

        } else if (target.classList.contains('back-button')) {
            const mainElement = document.querySelector('main');
            mainElement.innerHTML = initialMainContent;
        }
    });