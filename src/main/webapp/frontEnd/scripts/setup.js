import {displayUploadDirectoryPage} from "./content/displayUploadDirectoryPage.js";
import {removeMainElements} from "./utils/removeMainElements.js";
import {displayBooleanSearchPage} from "./content/displayBooleanSearchPage.js";
import {displayRankedSearchPage} from "./content/displayRankedSearchPage.js";
import {captureMainContent} from "./utils/captureMainContent.js";
import {setDirectoryPathAtServer} from "./utils/setDirectoryPathAtServer.js";

// Initially captures the main content once the HTML page has been loaded
let mainContent = null;
window.addEventListener('DOMContentLoaded', () => {
    mainContent = captureMainContent();
});

const mainElement = document.querySelector('main');
mainElement.addEventListener('click', event => {
    dispatchButtonClick(event);
});

// Dispatches work to the proper method depending on which button has been clicked
const dispatchButtonClick = (event) => {
    const target = event.target;

    if (target.matches(".directory-selection-button")) {
        dispatchDirectoryButtonClick(target);
    } else if (target.classList.contains('back-to-home-button')) {
        handleBackToHomeButtonClick();
    } else if (target.classList.contains('back-button')) {
        dispatchBackButtonClick(target);
    }
}

// Depending on which of the directory buttons are clicked, work will again be dispatched to the proper method
const dispatchDirectoryButtonClick = (target) => {
    if (target.id === 'default-directory-button') {
        setDirectoryPathAtServer();
    } else if (target.id === 'upload-directory-button') {
        displayUploadDirectoryPage();
    }
}

// Allows for content to be set back to what it was when the page initially loaded
const handleBackToHomeButtonClick = () => {
    const mainElement = document.querySelector('main');
    mainElement.innerHTML = mainContent;
}

// Allows the user to go back and enter another query after the search results have been displayed
const dispatchBackButtonClick = (target) =>  {
    removeMainElements();
    if (target.id === 'boolean-button') {
        displayBooleanSearchPage(target.id);
    } else {
        displayRankedSearchPage(target.id);
    }
}


