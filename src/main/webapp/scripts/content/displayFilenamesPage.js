import {removeMainElements} from "../utils/removeMainElements.js";
import {displayQueryModesPage} from "./displayQueryModesPage.js";

/*
This file contains methods for creating and displaying the "filenames" page. The HTML to display the page is
created, and an event listener is created for the "proceed to next" page
 */
export const displayFilenamesPage = (files) => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const fileNames = createFilenamesList(files);
    const nextButton = createNextButton();

    mainElement.appendChild(fileNames); // Appends filenames HTML to main element
    mainElement.appendChild(nextButton); // Appends next button to main element

    nextButton.addEventListener('click', () => {
        const formData = convertToFormData(files);
        handleUploadedDirectory(formData);
    })
}

// Creates HTML for listing the filenames in the uploaded directory
const createFilenamesList = (files) => {
    const container = document.createElement('div');
    container.classList.add('flex-column');
    const header = document.createElement('h3');
    header.textContent = 'Documents';
    container.appendChild(header);

    const arrayOfFiles = [...files]; // Convert file list to array of files

    // For each file, create a paragraph element to display the filename
    arrayOfFiles.forEach(file => {
        const fileName = document.createElement('p');
        fileName.textContent = file.name;
        container.appendChild(fileName);
    });

    return container;

}


// Creates the "proceed to next" button, as well as a paragraph element (to explain what will happen if the user proceeds)
const createNextButton = () => {
    const nextButtonContainer = document.createElement('div');
    nextButtonContainer.classList.add('flex-column');
    nextButtonContainer.innerHTML = `
            <button id="next-button">Index Corpus </button>
            <p> Indexing will create a hashmap of the unique terms in the corpus, where each term is associated with a posting list (list of documents and the positions 
            at which the term appears). The indexing process may take a couple of minutes. </p>
 
            `;
    return nextButtonContainer;
}

/*
This method converts the list of files to form data allows for easy/fast transfer of files to servlet. This is necessary
for the servlet to process each of the files.
 */
const convertToFormData = (files) => {
    const formData = new FormData();
    for (let i = 0; i < files.length; i++)
    {
        formData.append('files[]', files[i]);
    }

    return formData;
}

/*
Calls a method to remove the main elements, display a "loading spinner" (to let the user know the indexing process has begun),
and sends the uploaded files (converted to form data) to be processed by the servlet
 */
const handleUploadedDirectory = (formData) => {
    removeMainElements();
    createLoadingSpinner();
    sendToServlet(formData);
}

// Creates the HTML for a "loading" spinner that is displayed while the uploaded directory is being indexed
const createLoadingSpinner = () => {
    removeMainElements();
    const loadingDiv = document.createElement('div');
    loadingDiv.classList.add('flex-column');
    loadingDiv.innerHTML = `
            <span class="loading-spinner"></span>
            <p>Indexing...</p>
            `
    const mainElement = document.querySelector('main');
    mainElement.appendChild(loadingDiv);

}

/*
Sends the files (converted to form data) to the servlet. If no errors occur, a call to display the "query modes" page is
made.
 */
const sendToServlet = (formData) => {
    const value = document.querySelector('#query');

    fetch(`/upload`, {
        method: 'POST',
        body: formData
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(responseText => {
            displayQueryModesPage();
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}