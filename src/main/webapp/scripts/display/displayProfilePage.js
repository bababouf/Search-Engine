// Import utilities
import {captureMainContent} from "../utils/profileContentManager.js";
import {convertToFormData} from "../utils/convertToFormData.js";
import {setDefaultDirectory, setUploadedDirectory} from "../utils/configureServlet.js";
import {deleteUserDirectory, getProfileInformation, sendFormDataToServlet} from "../utils/contactServlet.js";
import {removeMainElements} from "../utils/removeMainElements.js";

// Import components
import {createLoadingSpinner} from "../components/createLoadingSpinner.js";
import {verifyUploadedDirectory} from "../utils/handleUploadedDirectory.js";
import {displayScrapeWebsitePage} from "./displayScrapeWebsitePage.js";

let userDirectories = 0;
let selectedKey = '';

// Calls methods to create HTML for the page, attach listeners, and capture the content
export const displayProfilePage = () =>
{
    removeMainElements();
    getProfileInformation()
        .then(profile =>
        {
            userDirectories = profile.directories;
            createProfileInformation(profile);
            populateDirectoryList(userDirectories);
            attachProfileListeners();
            captureMainContent();
        })
        .catch(error => console.error('Error retrieving profile information:', error));
};

// Calls methods to generate the header HTML and profile content HTML
const createProfileInformation = (profile) =>
{
    document.querySelector('body').innerHTML = `
        <div class="site-page">
            <header class="site-header__container">
                ${generateHeader(profile)}
            </header>
            <main class="site-content-grid">
                ${generateProfileContent()}
            </main>
        </div>
    `;
};

// Generates header HTML
const generateHeader = (profile) => `
    <h1 class="site-header__title"> Search Genie </h1>
    <div class="logo-container">
        <img src="../../images/search-genie-logo-transparent.png" class="logo" alt="Search Genie Logo">
    </div>
    <h2 class="site-header__welcome-user"> Welcome, ${profile.firstname} </h2>
    <p class="site-header__instructions">
        Select a directory from your file system, input a homepage URL to scrape webpages into a directory, or try querying 
        the default corpus. Once you upload a directory or scrape a URL, it will appear under the 'Current Directories' section.
    </p>
    <img src="${profile.url}" class="site-header__profile-picture" alt="${profile.firstname}'s profile picture">
`;

// Generates profile content HTML
const generateProfileContent = () => `
    <section class="card profile-directories bg-gradient">
        <h3 class="card-title">Current Directories</h3>
        <ul class="profile__user-dir-list"></ul>
    </section>
    <section class="card profile-upload bg-gradient">
        ${generateUploadDirectoryCard()}
    </section>
    <section class="card profile-scrape bg-gradient">
        ${generateScrapeWebsiteCard()}
    </section>
    <section class="card profile-default-directory bg-gradient">
        ${generateDefaultDirectoryCard()}
    </section>
`;

// Generates the HTML for the upload directory card (for uploading through file system)
const generateUploadDirectoryCard = () => `
    <h3 class="card-title">Upload Directory</h3>
    <div class="profile-upload__content">
        <p class="profile__upload-dir-note"> Upload a directory of .JSON files or .TXT files. </p>
        <ul class="profile__upload-restrictions-note"> 
            <li> Maximum 3 directories uploaded at a time </li>
            <li> Directories must be <= 50 MB </li>
            <li> JSON directories must contain 1 set of keys </li>
        </ul>
        <form id="upload-dir-form" enctype="multipart/form-data">
            <input type="file" id="folderInput" name="folderInput" webkitdirectory='true' class="profile__upload-dir-input">
            <button id="profile__upload-button" class="site__button" type="submit">Submit</button>
            <p id="error-message"></p>
        </form>
    </div>
`;

// Generates HTML for scraping website card
const generateScrapeWebsiteCard = () =>
    `
    <h3 class="card-title"> Scrape Website </h3>
    <p class="profile__upload-website-note"> Scrape a website's pages into .JSON files.  </p>
    <ul class="profile__upload-restrictions-note"> 
            <li> Enables users to enter a URL of the website to be scraped </li>
            <li> Webpages will be scraped into a directory that can be queried </li>
        </ul>
    <button id="profile__scrape-button" class="site__button" type="submit"> Go! </button>
    `;

// Generates HTML for default directory selection card
const generateDefaultDirectoryCard = () =>
    `
    <h3 class="card-title">Query Default Directory</h3>
        <p class="profile__default-dir-note"> Test the application on a default directory of 30,000 .JSON files scraped from NPS.gov! </p>
        <ul class="profile__upload-restrictions-note"> 
            <li> Query the directory using the boolean retrieval mode, or using ranked retrieval </li>
        </ul>
        <button id="profile__default-button" class="site__button">Go!</button>
    `;

// This method populates the user directory list ("Current Directories" card) with the user's uploaded directories
const populateDirectoryList = (directories) =>
{
    // Obtain the list
    const userDirectoryList = document.querySelector(".profile__user-dir-list");
    userDirectoryList.innerHTML = '';

    // If no directories are found
    if (directories.length === 0)
    {
        userDirectoryList.innerHTML = "<p>No directories found.</p>";
    }
    else
    {
        // Create a li for each user directory found
        directories.forEach(directory =>
        {
            userDirectoryList.appendChild(createDirectoryItem(directory));
        });
    }
};

// Creates a single user directory item; each item has an associated query and delete button attached
const createDirectoryItem = (directory) =>
{
    const listItem = document.createElement('li');
    listItem.className = 'profile__user-dir-item';
    listItem.innerHTML = `
        <a class="profile__user-dir-link" href="#">${directory.name}</a>
        <div class="flex-row-center">
            <button id="${directory.containerName}" class="user-dir-btn delete-btn">Delete</button>
            <button id="${directory.containerName}" class="user-dir-btn query-btn">Query</button>
        </div>
    `;
    return listItem;
};

// Attaches listeners to the buttons on the page (default, upload dir, and scrape), as well as the user directories
export const attachProfileListeners = () =>
{
    // Attaches listeners to each of the user directories
    document.querySelectorAll('.user-dir-btn').forEach(button =>
    {
        button.addEventListener('click', handleDirectoryButtonClick);
    });

    // Attaches listeners to the upload dir button and default dir selection button
    document.querySelectorAll('button').forEach(button =>
    {
        button.addEventListener('click', async (event) =>
        {
            if (event.target.id === 'profile__default-button')
            {
                await handleDefaultDirectory();
            }
            else if (event.target.id === 'profile__upload-button')
            {
                event.preventDefault();
                await handleUploadDirectory();
            }
        });
    });

    // Attaches listener to the scrape website button
    document.querySelector('#profile__scrape-button').addEventListener('click', event =>
    {
        displayScrapeWebsitePage();
    });
};

// Handles clicking the delete or query buttons that are associated with each user uploaded directory
const handleDirectoryButtonClick = (event) =>
{
    const target = event.target;
    const directoryID = target.id;

    // Contact servlet to handle deleting directory
    if (target.classList.contains('delete-btn'))
    {
        deleteUserDirectory(directoryID);
    }
    // Contact servlet to set up querying user directory
    else if (target.classList.contains('query-btn'))
    {
        setUploadedDirectory(directoryID);
    }
};

// Handles user selection to query default directory
const handleDefaultDirectory = async () =>
{
    try
    {
        // Contact servlet to handle querying default directory
        await setDefaultDirectory();
    }
    catch (error)
    {
        console.error('Error setting default directory:', error);
    }
};

// Handles user uploading of a directory
const handleUploadDirectory = async () =>
{
    // Get the files that were uploaded
    const files = document.querySelector('#folderInput').files;

    try
    {
        // Ensure the files meet the requirements (all .TXT or all .JSON, < 3 dirs uploaded, <50 MB per dir)
        const verificationResponse = await verifyUploadedDirectory(files);

        // Call method to handle uploading
        await handleUploadResponse(verificationResponse, files);
    }
    catch (error)
    {
        displayErrorMessage("An error occurred while processing your upload.");
    }
};

// Handles the uploading process
const handleUploadResponse = async (response, files) =>
{
    if (response.valid)
    {
        clearErrorMessage();

        // JSON directories require selecting a specific key whose content will be indexed
        if (response.keys)
        {
            displayJsonKeys(response.keys, files);
        }
        else
        {
            startUpload(files);
        }
    }
    else
    {
        displayErrorMessage(response.message);
    }
};

// Create a loading spinner to notify the user of the upload process starting, contact servlet to process upload
const startUpload = (files) =>
{
    const loadingSpinner = createLoadingSpinner();
    document.querySelector('.profile-upload__content').innerHTML = '';
    document.querySelector('.profile-upload__content').appendChild(loadingSpinner);

    const formData = convertToFormData(files);
    sendFormDataToServlet(formData);
};

// Error message for invalid directory uploads
const displayErrorMessage = (message) =>
{
    const errorMessageElement = document.querySelector("#error-message");
    errorMessageElement.textContent = message;
    errorMessageElement.style.display = 'block';
};

// Clears the error message for upload directory errors
const clearErrorMessage = () =>
{
    const errorMessageElement = document.querySelector("#error-message");
    if (errorMessageElement)
    {
        errorMessageElement.style.display = "none";
    }
};

// Displays HTML for key selection
const displayJsonKeys = (keys, files) =>
{
    const uploadDiv = document.querySelector(".profile-upload__content");
    const keyContainer = document.createElement('div');
    keyContainer.classList.add('profile-upload__key-container');

    keys.forEach(key =>
    {
        const keyItem = document.createElement('button');
        keyItem.textContent = key;
        keyItem.classList.add('directory__key-item');
        keyContainer.appendChild(keyItem);
    });

    uploadDiv.innerHTML = `
        <p class="key-selection-instructions">Select the key for querying content.</p>
        ${keyContainer.outerHTML}
        <button class="site__button key-select-button">Go!</button>
    `;

    // Attaches listener to determine which key was selected
    attachSelectButtonListener(files);
};

// Determine which key was selected and send it (along with the form data) to the servlet for processing
const attachSelectButtonListener = (files) =>
{
    document.querySelector('.key-select-button').addEventListener('click', () =>
    {
        const loadingSpinner = createLoadingSpinner();
        document.querySelector('.profile-upload__content').innerHTML = '';
        document.querySelector('.profile-upload__content').append(loadingSpinner);

        const formData = convertToFormData(files);
        formData.append("key", selectedKey);
        sendFormDataToServlet(formData);
    });

    document.querySelectorAll('.directory__key-item').forEach(button =>
    {
        button.addEventListener('click', (event) =>
        {
            document.querySelector(".key-select-active")?.classList.remove('key-select-active');
            selectedKey = event.target.textContent;
            event.target.classList.add("key-select-active");
        });
    });
};