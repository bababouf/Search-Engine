import {displayQueryModesPage} from "./content/displayQueryModesPage.js";
import { displayUploadDirectoryPage } from "./content/displayUploadDirectoryPage.js";

let initialMainContent;
window.addEventListener('DOMContentLoaded', () => {
    captureInitialMainContent();
});

const captureInitialMainContent = () => {
    const mainElement = document.querySelector('main');
    initialMainContent = mainElement.innerHTML;
};

const mainElement = document.querySelector('main');

    mainElement.addEventListener("click", (event) => {

        const target = event.target;

        if (target.matches(".directory-selection-button")) {
            
            if (target.id === 'default-directory-button') {
                displayQueryModesPage(event);
            } else if (target.id === 'upload-directory-button') {
                displayUploadDirectoryPage(event);
            }

        } else if (target.classList.contains('back-button')) {
            const mainElement = document.querySelector('main');
            mainElement.innerHTML = initialMainContent;
        }
    });

