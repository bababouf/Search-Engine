import {captureMainContent} from "../utils/profileContentManager.js";
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
let numberOfDirectories = 0;
export const displayProfilePage = () =>
{
    removeMainElements();
    getProfileInformation()
        .then(profile =>
        {
            createProfileInformation(profile);
            numberOfDirectories = profile.directories;
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
    let bodyElement = document.querySelector('body');

    bodyElement.innerHTML = `
    <div class="site-page">
    <header class="site-header__container">
        <h1 class="site-header__title"> Search Genie </h1>
        <div class="logo-container">
            <img src="../../images/search-genie-logo-transparent.png" class="logo" alt="Search Genie Logo">
        </div>
        <h2 class="site-header__welcome-user"> Welcome, ${profile.firstname} </h2>
        <p class="site-header__instructions"> Choose a directory from your filesystem or test out the querying modes on the default corpus! After uploading a 
        directory, it will show up under the 'Current Directories' card. 
        </p>
        <img src="${profile.url}" class="site-header__profile-picture" alt="${profile.firstname}'s profile picture">
    </header>
    <main class="site-content-grid">
        <section class="card profile-directories bg-gradient">
            <h3 class="card-title">Current Directories</h3>
            <ul class="profile__user-dir-list"></ul>
        </section>
        <section class="card profile-upload bg-gradient">
            <h3 class="card-title">Upload Directory</h3> 
            <form enctype="multipart/form-data">
                <p class="profile__upload-dir-note">Directory must contain all .TXT files, or all .JSON files. Directory must be < 50 MB.</p>
                <input type="file" id="folderInput" name="folderInput" webkitdirectory='true' class="profile__upload-dir-input">
                <button id="profile__upload-button" class="site__button" type="submit">Submit</button>
            </form>
        </section>
        <section class="card profile-default-directory bg-gradient">
            <h3 class="card-title">Use Default Directory</h3> 
            <p class="profile__default-dir-note">This directory contains 30,000 NPS.org webpages and can be used to test the various query modes this application has to offer.</p>
            <button id="profile__default-button" class="site__button" type="submit">Go!</button>
        </section>  
    </main>
</div>  
    `;

}

// Dynamically creates HTML for displaying user directories
const populateDirectoryList = () =>
{
    let userDirectoryList = document.querySelector(".profile__user-dir-list");

    // Clear any existing content
    userDirectoryList.innerHTML = '';

    // No directories associated with user
    if (numberOfDirectories.length === 0)
    {
        let noDirectoriesFound = document.createElement('p');
        noDirectoriesFound.textContent = "No directories found.";
        userDirectoryList.appendChild(noDirectoriesFound);
    }
    // One or more directories associated with user
    else
    {
        // Loop through each directory name and create a li anchor element
        numberOfDirectories.forEach(directory =>
        {
            let directoryItem = document.createElement('li');
            directoryItem.className = 'profile__user-dir-item';
            directoryItem.innerHTML = `
                <a id="${directory.containerName}" class="profile__user-dir-link" href="#">${directory.name}</a>
            `;
            userDirectoryList.appendChild(directoryItem);
        });

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
    const directoryLinks = document.querySelectorAll('.profile__user-dir-list');
    directoryLinks.forEach(link =>
    {
        link.addEventListener('click', event =>
        {
            const directoryID = event.target.id;
            setUploadedDirectory(directoryID);
        });
    });

    const profileButtons = document.querySelectorAll('button');
    profileButtons.forEach(button => button.addEventListener('click', event =>
    {
        if (event.target.id === 'profile__default-button')
        {
            setDefaultDirectory();
        }
        else
        {
            event.preventDefault();
            handleUploadDirectory();
        }

    }));

}


// Handles the default directory button click, as well as the submit button for the upload directory form
const handleUploadDirectory = () =>
{

    // Obtain the files that the user chose from the upload directory form
    const files = document.querySelector('#folderInput').files;

    // Ensures that the files are appropriate (size, file extension, within the allowed limit of directories uploaded)
    const verificationResponse = verifyUploadedDirectory(files);
    const uploadDiv = document.querySelector(".profile-upload");


    // Clear the uploadDiv content



    // Create and display loading spinner, and upload directory title
    //const uploadDirText = document.createElement("h3");
    //uploadDirText.classList.add('profile__card-title');
    //uploadDirText.textContent = "Upload Directory";



    if (verificationResponse === "Valid")
    {
        const loadingSpinner = createLoadingSpinner();
        uploadDiv.append(loadingSpinner);
        // Convert files to form data and send to servlet
        const formData = convertToFormData(files);
        sendFormDataToServlet(formData);
    }
    else
    {
        // Display error message if verification failed
        let errorMessageElement = document.querySelector("#error-message");

        // Check if the error message already exists (user has previously attempted to upload invalid directory)
        if (!errorMessageElement)
        {
            errorMessageElement = createErrorMessage(verificationResponse);
        }
        else
        {
            errorMessageElement.textContent = verificationResponse;
        }

        uploadDiv.appendChild(errorMessageElement);
    }
}


/*
Checks the uploaded files and ensures the file extensions are all .TXT or all .JSON. Checks that the directory is under
50MB. Checks that the user has < 3 directories uploaded. Based on these constraints, a string is returned to indicate
the files are valid or an error has occurred.
 */
const verifyUploadedDirectory = (files) => {
    const arrayOfFiles = [...files];
    const maxSize = 50 * 1024 * 1024; // 50MB in bytes
    const maxDirectories = 3;

    const validFileTypes = arrayOfFiles.every(file =>
        file.name.endsWith(".json") || file.name.endsWith(".txt")
    );

    if (numberOfDirectories >= maxDirectories) {
        return `Maximum ${maxDirectories} directories limit reached. Please delete a directory to upload a new one.`;
    }

    if (!validFileTypes || arrayOfFiles.length === 0) {
        return "Directory contains invalid file types.";
    }

    const totalSize = arrayOfFiles.reduce((acc, file) => acc + file.size, 0);

    return totalSize <= maxSize ? "Valid" : `Directory exceeds ${maxSize / (1024 * 1024)} MB.`;
};

