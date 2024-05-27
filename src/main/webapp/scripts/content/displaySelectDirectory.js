import {removeMainElements} from "../utils/removeMainElements.js";
import {setDirectoryPathAtServer} from "../utils/setDirectoryPathAtServer.js";
import {displayUploadDirectoryPage} from "./displayUploadDirectoryPage.js";
import {appendBackToHomeButton} from "../utils/appendBackToHomeButton.js";

export const displaySelectDirectory = () => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const directorySelectionElements = createDirectorySelection();
    mainElement.appendChild(directorySelectionElements);
    appendBackToHomeButton();
    attachDirectoryListeners();

}

const createDirectorySelection = () => {
    const directorySelectionContainer = document.createElement('div');

    directorySelectionContainer.innerHTML = `
            <h2>Select Directory</h2>
                <div class="card-container">
                    <div class="card">
                        <h3>Default Directory</h3> 
                        <img src="../../images/default-dir-logo.png" height="55" width="75">               
                        <p>This directory contains 30,000 NPS.org webpages and can be used to test the various query modes this application has to offer. </p>
                        <button class ="directory-selection__button" id="default-directory-button" type="submit"> Go! </button>
                    </div>
                    <div class="card">
                        <h3>Upload Directory</h3>
                        <img src="../../images/upload-logo.png" height="55" width="75">
                        <p>This option allows for a directory of .TXT or .JSON files to be uploaded. Indexing time grows as the corpus size increases. </p>
                        <button class ="directory-selection__button" id="upload-directory-button" type="submit"> Go! </button>
                    </div>
                </div>
    `;

    return directorySelectionContainer;
}

const attachDirectoryListeners = () => {
    const directoryButtons = document.querySelectorAll('button');
    directoryButtons.forEach(button => button.addEventListener('click', event => {
        dispatchButtonClick(event);
    }));
}

const dispatchButtonClick = (event) => {
    const target = event.currentTarget;

    if (target.classList.contains("directory-selection__button")) {
        dispatchDirectoryButtonClick(target);
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



