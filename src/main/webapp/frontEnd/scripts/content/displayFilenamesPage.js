import {removeMainElements} from "../utils/removeMainElements.js";

export const displayFilenamesPage = (files) => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const fileNames = listFilenames(files);
    const nextButton = createNextButton();

    mainElement.appendChild(fileNames);
    mainElement.appendChild(nextButton);

    nextButton.addEventListener('click', () => {
        const formData = convertToFormData(files);
        handleUploadedDir(formData);
    })
}

const listFilenames = (files) => {
    const container = document.createElement('div');
    container.classList.add('flex-column');
    const header = document.createElement('h3');
    header.textContent = 'Documents';
    container.appendChild(header);

    const arrayOfFiles = [...files];
    arrayOfFiles.forEach(file => {
        const fileName = document.createElement('p');
        fileName.textContent = file.name;
        container.appendChild(fileName);
    });

    return container;

}

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

const convertToFormData = (files) => {
    const formData = new FormData();
    for (let i = 0; i < files.length; i++) {
        formData.append('files[]', files[i]);
    }
    return formData;
}

const handleUploadedDir = (formData) => {
    removeMainElements();
    createLoadingSpinner();
    sendToServlet(formData);
}

const createLoadingSpinner = () => {
    removeMainElements();
    const loadingDiv = document.createElement('div');
    loadingDiv.classList.add('flex-column');
    loadingDiv.innerHTML = `
            <span class="loader"></span>
            <p>Indexing...</p>
            `
    const mainElement = document.querySelector('main');
    mainElement.appendChild(loadingDiv);

}

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
            console.log("Holaaaa");
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}