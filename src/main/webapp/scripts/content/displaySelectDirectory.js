import {removeMainElements} from "../utils/removeMainElements.js";
import {setDirectoryPathAtServer} from "../utils/setDirectoryPathAtServer.js";
import {displayUploadDirectoryPage} from "./displayUploadDirectoryPage.js";
import {appendBackToHomeButton} from "../utils/appendBackToHomeButton.js";

/*
This file contains methods for creating and displaying the "select directory" page. The HTML to display the page is
created, and event listeners are attached to each of the directory buttons.
 */

/*
Removes previous HTML content attached to the main element, and appends the newly created HTML for the "select directory"
page.
 */
export const displaySelectDirectory = () => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const directorySelectionElements = createDirectorySelection();
    mainElement.appendChild(directorySelectionElements); // Appends "select directory" HTML to the main
    appendBackToHomeButton();
    attachDirectoryListeners(); // Attach event listeners to each of the directory buttons

}

// Creates the HTML for the "select directory" page
const createDirectorySelection = () => {
    const directorySelectionContainer = document.createElement('div');

    directorySelectionContainer.innerHTML = `
            <h2>Select Directory</h2>
                <div class="card-container"> 
                    <div class="card">
                        <h3>Default Directory</h3> 
                        <img src="../../images/default-dir-logo.png" height="30" width="40">               
                        <p>This directory contains 30,000 NPS.org webpages and can be used to test the various query modes this application has to offer. </p>
                        <button class ="directory-selection__button" id="default-directory-button" type="submit"> Go! </button>
                    </div>
                    <div class="card">
                        <h3>Upload Directory</h3>
                        <img src="../../images/upload-logo.png" height="30" width="40">
                        <p>This option allows for a directory of .TXT or .JSON files to be uploaded. Indexing time grows as the corpus size increases. </p>
                        <button class ="directory-selection__button" id="upload-directory-button" type="submit"> Go! </button>
                    </div>
                </div>
    `;

    return directorySelectionContainer;
}

// Attaches event listeners to each of the directory buttons
const attachDirectoryListeners = () => {
    const directoryButtons = document.querySelectorAll('button');
    directoryButtons.forEach(button => button.addEventListener('click', event => {
        dispatchDirectoryButtonClick(event);
    }));
}

// Calls the appropriate method for handling each of the directory clicks (default or upload)
const dispatchDirectoryButtonClick = (event) => {
    if (event.target.id === 'default-directory-button') {
        setDirectoryPathAtServer(); // Contacts servlet so that it knows to use default corpus
    }
    else if (event.target.id === 'upload-directory-button')
    {
        displayUploadDirectoryPage(); // Calls method to display "upload directory" page
    }
}



