import {removeMainElements} from "../utils/removeMainElements.js";
import {createBackButton} from "./createBackButton.js";
import {displayQueryModesPage} from "./displayQueryModesPage.js";



export const displayUploadDirectoryPage = () => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const uploadDirectory = displayUploadDirectory();
    const backButton = createBackButton();

    mainElement.appendChild(uploadDirectory);
    mainElement.appendChild(backButton);

    createSubmitDirectoryListener();

}

const displayUploadDirectory = () => {
    const uploadDirectory = document.createElement('div');
    uploadDirectory.classList.add('upload-corpus-div');

    uploadDirectory.innerHTML = `
    <form id="folderForm">
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

        const arrayOfFiles = [...files];
        verifyFileExtension(arrayOfFiles);
    });
}

    const verifyFileExtension = (arrayOfFiles) => {
        const allJsonFiles = arrayOfFiles.every(file => file.name.endsWith('.json'));
        const allTxtFiles = arrayOfFiles.every(file => file.name.endsWith('.txt'));

        if(allJsonFiles || allTxtFiles){
            displayFileNames(arrayOfFiles);
        }
        else
        {
            createErrorMessage();
        }


    }

    const createErrorMessage = () => {
        const errorMsg = document.createElement('p');
        errorMsg.textContent = 'Directory must be all .TXT files or all .JSON files. ';
        errorMsg.style.color = '#880808';
        const mainElement = document.querySelector('main');
        mainElement.insertBefore(errorMsg, mainElement.firstChild);
    }

    const displayFileNames = (arrayOfFiles) => {
        removeMainElements();
        const mainElement = document.querySelector('main');

        const header = document.createElement('h2');
        header.textContent = 'Documents: ';
        mainElement.appendChild(header);
        const displayDiv = document.createElement('div');
        arrayOfFiles.forEach(file => {
            const fileName = document.createElement('p');
            fileName.textContent = file.name;
            displayDiv.appendChild(fileName);
        });

        const nextButtonContainer = document.createElement('div');
        nextButtonContainer.classList.add('card-container');
        nextButtonContainer.innerHTML = `
        <button id="next-button">Next</button>
 
        `;

        mainElement.appendChild(displayDiv);
        mainElement.appendChild(nextButtonContainer);

        nextButtonContainer.addEventListener('click', event => {
            displayQueryModesPage();
        })
    }