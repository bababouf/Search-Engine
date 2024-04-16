import { displayQueryModes } from "./query-modes.js";
import { displayUploadDirectory } from "./upload-directory.js";
import { hideMainElements } from "./hide-main-elements.js";

import {displayBackButton} from "./back-button.js";
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
                console.log('hi');
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