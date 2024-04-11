import { displayQueryModes } from "./display/query-modes";
import { displayUploadDirectory } from "./display/upload-directory";
import { hideMainElements } from "./utils/toggle-main-elements";
import { displayBackButton } from "./display/back-button";

let initialMainContent;

const captureInitialMainContent = () => {
    const mainElement = document.querySelector('main');
    initialMainContent = mainElement.innerHTML;
};

window.addEventListener('DOMContentLoaded', () => {
    captureInitialMainContent();
});

const mainElement = document.querySelector('main');

    mainElement.addEventListener("click", (event) => {
        const target = event.target;
        if (target.matches(".corpus-selection-button")) {
            hideMainElements(); 
            
            if (target.id === 'default-corpus-button') {
                displayQueryModes(event);
                displayBackButton();
            } else if (target.id === 'upload-corpus-button') {
                displayUploadDirectory(event);
                displayBackButton();
            }

        } else if (target.id === 'back-button') {
            const mainElement = document.querySelector('main');
            mainElement.innerHTML = initialMainContent;
        }
    });