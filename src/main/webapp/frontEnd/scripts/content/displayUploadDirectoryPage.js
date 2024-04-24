import {removeMainElements} from "../utils/removeMainElements.js";
import {createBackButton} from "./createBackButton.js";
import {displayFilenamesPage} from "./displayFilenamesPage.js";

export const displayUploadDirectoryPage = () => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const uploadDirectory = createUploadDirectoryForm();
    const backButton = createBackButton();

    mainElement.appendChild(uploadDirectory);
    mainElement.appendChild(backButton);
    createSubmitDirectoryListener();
}

const createUploadDirectoryForm = () => {
    const uploadDirectory = document.createElement('div');
    uploadDirectory.classList.add('upload-corpus-div');

    uploadDirectory.innerHTML = `
    <form id="folderForm" enctype="multipart/form-data">
        <label for="folderPath">Choose a corpus directory: </label>
        <input type="file" id="folderInput" name="folderInput" webkitdirectory = 'true'>
        <button type="submit">Submit</button>
    </form>
    `;

    return uploadDirectory;
}

const createSubmitDirectoryListener = () => {
    const form = document.querySelector('#folderForm');
    form.addEventListener('submit', (event) => {
        event.preventDefault();
        const files = document.querySelector('#folderInput').files;
        verifyUploadedDir(files);
    });
}

const verifyUploadedDir = (files) => {
    const mainElement = document.querySelector('main');
    const arrayOfFiles = [...files];
    if (arrayOfFiles.every(file => file.name.endsWith('.json')) || arrayOfFiles.every(file => file.name.endsWith('.txt'))) {
        displayFilenamesPage(files);
    } else {
        const errorMsg = createErrorMessage();
        mainElement.insertBefore(errorMsg, mainElement.firstChild);
    }
}

const createErrorMessage = () => {
    const errorMsg = document.createElement('p');
    errorMsg.textContent = 'Directory must contain all .TXT files or all .JSON files. ';
    errorMsg.style.textAlign = 'center';
    errorMsg.style.color = 'red';

    return errorMsg;
}

