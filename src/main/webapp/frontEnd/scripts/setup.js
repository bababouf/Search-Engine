import {displayQueryModesPage} from "./content/displayQueryModesPage.js";
import {displayUploadDirectoryPage} from "./content/displayUploadDirectoryPage.js";
import {removeMainElements} from "./utils/removeMainElements.js";
import {displayBooleanSearchPage} from "./content/displayBooleanSearchPage.js";
import {displayRankedSearchPage} from "./content/displayRankedSearchPage.js";
import {captureMainContent} from "./utils/captureMainContent.js";

let mainContent = null;
window.addEventListener('DOMContentLoaded', () => {
    mainContent = captureMainContent();
});

const mainElement = document.querySelector('main');

mainElement.addEventListener('click', event => {
    handleButtonClick(event);
})

const handleButtonClick = (event) => {
    const target = event.target;

    if (target.matches(".directory-selection-button")) {
        handleDirectoryButtonClick(target);
    } else if (target.classList.contains('back-to-home-button')) {
        handleBackToHomeButtonClick();
    } else if (target.classList.contains('back-button')) {
        handleBackButtonClick(target);
    }
}

const handleDirectoryButtonClick = (target) => {
    if (target.id === 'default-directory-button') {
        displayQueryModesPage();
    } else if (target.id === 'upload-directory-button') {
        displayUploadDirectoryPage();
    }
}

const handleBackToHomeButtonClick = () => {
    const mainElement = document.querySelector('main');
    mainElement.innerHTML = mainContent;
}

const handleBackButtonClick = (target) =>  {
    removeMainElements();
    if (target.id === 'boolean-button') {
        displayBooleanSearchPage(target.id);
    } else {
        displayRankedSearchPage(target.id);
    }
}


