import {captureMainContent} from "../utils/profileContentManager.js";
import {attachHeaderAnchorListeners, createHeaderAnchorElements} from "../utils/handleHeaderAnchors.js";
import {createLoadingSpinner} from "../components/createLoadingSpinner.js";
import {convertToFormData} from "../utils/convertToFormData.js";
import {setDefaultDirectory, setUploadedDirectory} from "../utils/configureServlet.js";
import {getProfileInformation, sendFormDataToServlet} from "../utils/contactServlet.js";
import {createErrorMessage} from "../components/createErrorMessage.js";

/*
This file handles all the necessary actions to display the user's profile page. This includes getting user information
from the servlet (through an HTTP GET request), dynamically creating the HTML to display the elements on the page, attaching
listeners to the clickable buttons, verifying the files uploaded by the user, among other actions.
 */
export const displayProfilePage = () => {
    createHeaderAnchorElements();
    attachHeaderAnchorListeners();
    getProfileInformation()
        .then(profile => {

            createProfileInformation(profile);
            populateDirectoryList(profile.directories);
            attachProfileListeners();
            captureMainContent();
        })
        .catch(error => {
            console.error('Error retrieving profile information:', error);
        });
}

// Dynamically creates the HTML for profile page
const createProfileInformation = (profile) => {

    let mainElement = document.querySelector('main');

    mainElement.innerHTML = `
    <h2> ${profile.firstname}'s Profile </h2>
    <img src= ${profile.url} height="200" width="200" style="border-radius: 50%" alt="User Profile" class="profile__logo">
    <div class = "card-container">
    <div class="card">
        <h3> Current Directories </h3>
        <div id="directory-div"></div>
    </div>
    <div class="card flex-column upload-div">
        <h3> Upload Directory </h3> 
        <form id="folderForm" enctype="multipart/form-data">
            <p>Directory must contain all .TXT files, or all .JSON files</p>
            <input type="file" id="folderInput" name="folderInput" webkitdirectory = 'true'>
            <button type="submit">Submit</button>
        </form>
    </div>
    <div class="card">
        <h3> Use Default Directory </h3> 
        <p>This directory contains 30,000 NPS.org webpages and can be used to test the various query modes this application has to offer. </p>
        <button class ="directory-selection__button" id="default-directory-button" type="submit"> Go! </button>
    </div>
    
    </div>
    `;

}


// Dynamically creates HTML for displaying user directories
const populateDirectoryList = (directories) => {
    let directoryDiv = document.getElementById('directory-div');

    // In the case no directories were found
    if (directories.length === 0)
    {
        let noDirectoriesFound = document.createElement('p');
        noDirectoriesFound.textContent = "No directories found.";
        directoryDiv.appendChild(noDirectoriesFound);
    }
    // One or more directories were found
    else
    {
        // Create a paragraph element for each directory found
        directories.forEach(directory =>
        {
            let directoryID = directory.containerName;
            let directoryUL = document.createElement('ul');
            let directoryElement = document.createElement('li');
            let directoryLink = document.createElement('a');

            directoryLink.id = directoryID;
            directoryLink.className = "directory";
            directoryLink.href = "#";
            directoryLink.textContent = directory.name;
            directoryElement.appendChild(directoryLink);
            directoryUL.appendChild(directoryElement);
            directoryDiv.appendChild(directoryUL);
        });
    }
}


// Creates a listener for each button, calling a dispatch method that handles each case
export const attachProfileListeners = () => {

    // Create the listeners for each of the uploaded directories
    const directoryLinks = document.querySelectorAll('.directory');
    directoryLinks.forEach(link => {
        link.addEventListener('click', event => {
            event.preventDefault();
            const directoryID = event.target.id;

            // Contact servlet to set the directory that will be queried
            setUploadedDirectory(directoryID);
        });
    });

    const profileButtons = document.querySelectorAll('button');
    profileButtons.forEach(button => button.addEventListener('click', event => {
        dispatchDirectoryButtonClick(event);
    }));

}



// Handles each of the button clicks
const dispatchDirectoryButtonClick = (event) => {
    if (event.target.id === 'default-directory-button')
    {
        // Contact servlet to set the default directory to be queried
        setDefaultDirectory();
    }
    else
    {
        event.preventDefault();
        const files = document.querySelector('#folderInput').files;

        // The files uploaded have a valid file extension and can continue to uploading process
        if(verifyUploadedDirectory(files) === true)
        {
            const uploadDiv = document.querySelector(".upload-div");
            const uploadDirText = document.createElement("h3");
            uploadDirText.textContent = "Upload Directory";
            const loadingSpinner = createLoadingSpinner();
            uploadDiv.innerHTML = '';
            uploadDiv.appendChild(uploadDirText);
            uploadDiv.appendChild(loadingSpinner);

            // Converts files to form data so that they can be sent to the servlet
            const formData = convertToFormData(files);

            // Contacts servlet with the files that will be uploaded and indexed
            sendFormDataToServlet(formData);
        }
        // User attempted to upload a directory containing one or more invalid file extensions
        else
        {
            const uploadDiv = document.querySelector(".upload-div");

            // Display error message if it isn't already displayed
            if(document.querySelector("#error-message") === null)
            {
                const errorMessage = createErrorMessage();
                uploadDiv.appendChild(errorMessage);
            }

        }

    }
}


// Ensures the directory that the user selected contains all .JSON files, or all .TXT files.
const verifyUploadedDirectory = (files) => {
    const arrayOfFiles = [...files];

    // Check if every file is either .JSON or .TXT
    if ((arrayOfFiles.every(file => file.name.endsWith('.json')) || arrayOfFiles.every(file => file.name.endsWith('.txt'))) && arrayOfFiles.length > 0)
    {
        return true;
    }
}

