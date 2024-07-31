import {setDirectoryPathAtServer} from "../utils/setDirectoryPathAtServer.js";
import {captureMainContent} from "../utils/homepageContentManager.js";


export const displayProfilePage = () => {
    getProfileInformation();

}

/*
Makes a GET request to obtain profile information for the user. This includes the user's first name, profile picture URL,
and a list of uploaded directories.
 */
const getProfileInformation = () => {
    fetch(`/retrieveProfile`, {
        method: 'GET'
    })
        .then(response => {
            if (!response.ok)
            {
                throw new Error('Network response was not ok');
            }

            return response.text();
        })
        .then(profileInfo => {

            // Creates the JSON profile object
            const profile = JSON.parse(profileInfo);

            // Call method to dynamically create HTML to display profile
            createProfileInformation(profile);

        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
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
        <div id="directory-list"></div>
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

    // Dynamically add directory names to "directory-list"
    populateDirectoryList(profile.directories);

    // Add event listeners to each button on the page
    attachDirectoryListeners();

    // Capture main content so that back buttons on other pages take the user back to the profile page
    captureMainContent();
}

// Dynamically creates HTML for displaying user directories
const populateDirectoryList = (directories) => {
    let directoryListElement = document.getElementById('directory-list');

    if (directories.length === 0)
    {
        let noDirectoriesFound = document.createElement('p');
        noDirectoriesFound.textContent = "No directories found.";
        directoryListElement.appendChild(noDirectoriesFound);
    }
    else
    {
        directories.forEach(directory => {
            let directoryElement = document.createElement('p');
            directoryElement.textContent = directory;
            directoryListElement.appendChild(directoryElement);
        });
    }
}

// Creates a listener for each button, calling a dispatch method that handles each case
export const attachDirectoryListeners = () => {

    const directoryButtons = document.querySelectorAll('button');
    directoryButtons.forEach(button => button.addEventListener('click', event => {
        dispatchDirectoryButtonClick(event);
    }));
}


// Handles each of the different button clicks
const dispatchDirectoryButtonClick = (event) => {
    if (event.target.id === 'default-directory-button')
    {
        setDirectoryPathAtServer();
    }
    else
    {
        event.preventDefault();
        const files = document.querySelector('#folderInput').files;

        // Verifies that the files uploaded are either .TXT or .JSON
        verifyUploadedDirectory(files);
    }
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
    if (arrayOfFiles.every(file => file.name.endsWith('.json')) || arrayOfFiles.every(file => file.name.endsWith('.txt')))
    {
        const uploadDiv = document.querySelector('.upload-div');
        uploadDiv.innerHTML = '';
        const uploadDirectoryHeader = document.createElement('h3');
        uploadDirectoryHeader.textContent = 'Upload Directory'
        uploadDiv.appendChild(uploadDirectoryHeader);
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
