import {removeMainElements} from "../utils/removeMainElements.js";
import {appendBackToHomeButton} from "../utils/appendBackToHomeButton.js";
import {displayFilenamesPage} from "./displayFilenamesPage.js";

// Displays a "page" containing a form for directory upload and a submit button + back button
export const displayUploadDirectoryPage = () => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const uploadDirectory = createUploadDirectoryForm();


    mainElement.appendChild(uploadDirectory);
    appendBackToHomeButton();
    attachSubmitDirectoryListener();
}

// Creates a form used allowing the user to enter a folder of either .TXT documents or .JSON documents
const createUploadDirectoryForm = () => {
    const uploadDirectory = document.createElement('div');


    uploadDirectory.innerHTML = `
    <div class="upload-corpus-div">
    <form id="folderForm" enctype="multipart/form-data">
        <h2>Select a Directory</h2>
        <input type="file" id="folderInput" name="folderInput" webkitdirectory = 'true'>
        <button type="submit">Submit</button>
    </form>
    </div>
    `;

    return uploadDirectory;
}

// Creates the listener for submitting the form using the submit button
const attachSubmitDirectoryListener = () => {
    const form = document.querySelector('#folderForm');
    form.addEventListener('submit', (event) => {
        event.preventDefault(); // Don't reload the page
        const files = document.querySelector('#folderInput').files;
        verifyUploadedDirectory(files);
    });
}

/*
    Ensure uploaded folder of files contains all .TXT or all .JSON documents. If so, continue on and display
    all the filenames. Else display error message.
 */
const verifyUploadedDirectory = (files) => {
    const mainElement = document.querySelector('main');
    const arrayOfFiles = [...files];
    if (arrayOfFiles.every(file => file.name.endsWith('.json')) || arrayOfFiles.every(file => file.name.endsWith('.txt'))) {
        displayFilenamesPage(files);
    } else {
        const errorMsg = createErrorMessage();
        mainElement.insertBefore(errorMsg, mainElement.firstChild);
    }
}

// Creates the HTML + sets CSS attributes for the error message
const createErrorMessage = () => {
    const errorMsg = document.createElement('p');
    errorMsg.textContent = 'Directory must contain all .TXT files or all .JSON files. ';
    errorMsg.style.textAlign = 'center';
    errorMsg.style.color = 'red';

    return errorMsg;
}

