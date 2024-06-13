import {removeMainElements} from "../utils/removeMainElements.js";
import {appendBackToHomeButton} from "../utils/appendBackToHomeButton.js";
import {displayFilenamesPage} from "./displayFilenamesPage.js";

/*
This file contains methods for creating and displaying the "upload directory" page. The HTML to display the page is
created, and an event listener is attached to submit the user-selected directory.
 */
export const displayUploadDirectoryPage = () => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const uploadDirectory = createUploadDirectoryForm();

    mainElement.appendChild(uploadDirectory); // Attaches HTML for "upload directory" page to the main element
    appendBackToHomeButton();
    attachSubmitDirectoryListener(); // Attaches event listener for submitting directory
}

// Creates the HTML that allows the user to select a directory from their filesystem
const createUploadDirectoryForm = () => {
    const uploadDirectory = document.createElement('div');

    uploadDirectory.innerHTML = `
    <div class="upload-corpus-div">
    <form id="folderForm" enctype="multipart/form-data">
        <h2>Select a Directory</h2>
        <p class="margin-horizontal-10rem">Directory must contain all .TXT files, or all .JSON files. A directory containing any other files, or a mixture of .TXT and .JSON will be rejected.</p>
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
Ensures the directory that the user selected contains all .JSON files, or all .TXT files. If this is not the case, an
error message is displayed to the user. If the directory is accepted, a call to a method that displays the filenames that
were uploaded is made
 */
const verifyUploadedDirectory = (files) => {
    const mainElement = document.querySelector('main');
    const arrayOfFiles = [...files];

    // Check if every file is either .JSON or .TXT
    if (arrayOfFiles.every(file => file.name.endsWith('.json')) || arrayOfFiles.every(file => file.name.endsWith('.txt'))) {
        displayFilenamesPage(files); // Call method to display filenames
    }
    else
    {
        const errorMsg = createErrorMessage();
        mainElement.insertBefore(errorMsg, mainElement.firstChild); // Insert error message as first element attached to main
    }
}

// Creates HTML for the error message that is displayed if improper filetypes are uploaded
const createErrorMessage = () => {
    const errorMsg = document.createElement('p');
    errorMsg.textContent = 'Directory must contain all .TXT files or all .JSON files. ';
    errorMsg.style.textAlign = 'center';
    errorMsg.style.color = 'red';

    return errorMsg;
}

