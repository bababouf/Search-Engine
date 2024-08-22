import {captureMainContent} from "../utils/profileContentManager.js";
import {attachHeaderAnchorListeners, createHeaderAnchorElements} from "../utils/handleHeaderAnchors.js";
import {createLoadingSpinner} from "../components/createLoadingSpinner.js";
import {convertToFormData} from "../utils/convertToFormData.js";
import {setDefaultDirectory, setUploadedDirectory} from "../utils/configureServlet.js";
import {getProfileInformation, sendFormDataToServlet} from "../utils/contactServlet.js";
import {createErrorMessage} from "../components/createErrorMessage.js";
import {removeMainElements} from "../utils/removeMainElements.js";

/*
This file handles all the necessary actions to display the user's profile page. This includes getting user information
from the servlet (through an HTTP GET request), dynamically creating the HTML to display the elements on the page, attaching
listeners to the clickable buttons, verifying the files uploaded by the user, among other actions.
 */
let numberOfDirectores = 0;
export const displayProfilePage = () =>
{
    removeMainElements();
    createHeaderAnchorElements();
    attachHeaderAnchorListeners();
    getProfileInformation()
        .then(profile =>
        {

            createProfileInformation(profile);
            numberOfDirectores = profile.directories;
            populateDirectoryList();
            attachProfileListeners();
            captureMainContent();
        })
        .catch(error =>
        {
            console.error('Error retrieving profile information:', error);
        });
}

/*
Creates the HTML for the profile page using the information passed in the profile object. This profile object
contains the user's firstname, their Google profile image, and a list of directory names that have been uploaded.
This method handles displaying everything in the page except for the user directories -- these directories are populated
using another method.
 */
const createProfileInformation = (profile) =>
{
    let mainElement = document.querySelector('main');

    mainElement.innerHTML = `
    <div class="center-content">
        <h2 class="site__h2"> ${profile.firstname}'s Profile </h2>
        <img src= ${profile.url} class="site__profile-logo" alt="User Profile">
        <div class = "card-container">
            <div class ="card">
                <h3 class="site__h3"> Current Directories </h3>
                <div class="site__user-directories"></div>
            </div>
            <div class="card site__upload-div">
                <h3 class="site__h3"> Upload Directory </h3> 
                <form id="folderForm" enctype="multipart/form-data">
                    <p> Directory must contain all .TXT files, or all .JSON files. Directory must be < 50 MB. </p>
                    <input type="file" id="folderInput" name="folderInput" webkitdirectory = 'true'>
                    <button type="submit">Submit</button>
                </form>
            </div>
            <div class="card">
                <h3 class="site__h3"> Use Default Directory </h3> 
                <p>This directory contains 30,000 NPS.org webpages and can be used to test the various query modes this application has to offer. </p>
                <button class="site__default-directory-button" type="submit"> Go! </button>
            </div>
        
        </div>
    </div>
    `;


}

// Dynamically creates HTML for displaying user directories
const populateDirectoryList = () =>
{
    let directoryDiv = document.querySelector(".site__user-directories");

    // Creates and displays text under the 'Uploaded Directories' card to show no directories have been uploaded
    if (numberOfDirectores.length === 0)
    {
        let noDirectoriesFound = document.createElement('p');
        noDirectoriesFound.textContent = "No directories found.";
        directoryDiv.appendChild(noDirectoriesFound);
    }
    // One or more directories were found
    else
    {
        let directoryUL = document.createElement('ul');

        // Create a paragraph element for each directory found
        numberOfDirectores.forEach(directory =>
        {
            let directoryItem = document.createElement('li');
            directoryItem.innerHTML = `
            <a id="${directory.containerName}" class="site__user-directory" href="#"> ${directory.name} </a>
            `;

            directoryUL.appendChild(directoryItem);
        });
        directoryDiv.appendChild(directoryUL);
    }
}


/*
Attaches the necessary listeners to the buttons and anchor tags on the page. For the anchor tags (directory links), each
user uploaded directory will have an attached listener. Then, when a specific directory is clicked, the ID for that anchor tag
(which is an identifier string to the Blob Storage container) will be used to let the servlet know the user is trying to
query that directory. For the buttons on the page (the default directory button and submit button for uploading a directory),
this method also attaches listeners. When one of these buttons is clicked, a dispatch method is called to handle the rest.
 */
export const attachProfileListeners = () =>
{

    // Create the listeners for each of the uploaded directories
    const directoryLinks = document.querySelectorAll('.site__user-directory');
    directoryLinks.forEach(link =>
    {
        link.addEventListener('click', event =>
        {
            // Get the container name for the uploaded directory
            const directoryID = event.target.id;

            // Contact servlet to set the directory that will be queried
            setUploadedDirectory(directoryID);
        });
    });

    const profileButtons = document.querySelectorAll('button');
    profileButtons.forEach(button => button.addEventListener('click', event =>
    {
        // Handles the rest once a button is clicked
        dispatchDirectoryButtonClick(event);
    }));

}


// Handles the default directory button click, as well as the submit button for the upload directory form
const dispatchDirectoryButtonClick = (event) =>
{
    if (event.target.classList.contains('site__default-directory-button'))
    {
        // Contact servlet to set the default directory to be queried
        setDefaultDirectory();
    }
    else
    {
        event.preventDefault();

        // Obtain the files that the user chose from the upload directory form
        const files = document.querySelector('#folderInput').files;

        // Ensures that the files are appropriate (size, file extension, within the allowed limit of directories uploaded)
        let verificationResponse = verifyUploadedDirectory(files);

        // Create and display loading spinner, convert files to form data, send to servlet
        if (verificationResponse === "Valid")
        {
            const uploadDiv = document.querySelector(".site__upload-div");
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
        // Files cannot be uploaded (verification failed)
        else
        {
            const uploadDiv = document.querySelector(".site__upload-div");
            let errorMessageElement = document.querySelector("#error-message");

            // Display error message if it isn't already displayed
            if (errorMessageElement === null)
            {
                errorMessageElement = createErrorMessage(verificationResponse);
            }
            else
            {
                errorMessageElement.textContent = `${verificationResponse}`;
            }

            uploadDiv.appendChild(errorMessageElement);

        }

    }
}


/*
Checks the uploaded files and ensures the file extensions are all .TXT or all .JSON. Checks that the directory is under
50MB. Checks that the user has < 3 directories uploaded. Based on these constraints, a string is returned to indicate
the files are valid or an error has occurred.
 */
const verifyUploadedDirectory = (files) =>
{

    const arrayOfFiles = [...files];
    const maxSize = 50 * 1024 * 1024; // 50MB in bytes

    const validFileTypes = arrayOfFiles.every(file => file.name.endsWith(".json") || file.name.endsWith(".txt"));
    if (numberOfDirectores >= 3)
    {
        return "Maximum 3 directories limit reached. Please delete a directory to upload a new one. "
    }
    else if (validFileTypes && arrayOfFiles.length > 0)
    {
        // Calculate the total size of the uploaded files
        const totalSize = arrayOfFiles.reduce((acc, file) => acc + file.size, 0);

        // Check if the total size is within the limit
        if (totalSize <= maxSize)
        {
            return "Valid";
        }
        else
        {
            console.log("Directory size exceeds the maximum limit.");
            return "Directory exceeds 50 MB. ";
        }
    }
    else
    {
        console.log("Invalid file types.");
        return "Directory contains invalid file types.";
    }

}

