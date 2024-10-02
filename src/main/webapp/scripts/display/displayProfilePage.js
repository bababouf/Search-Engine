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

const generateHeader = (profile) => `
    <h1 class="site-header__title"> Search Genie </h1>
    <div class="logo-container">
        <img src="../../images/search-genie-logo-transparent.png" class="logo" alt="Search Genie Logo">
    </div>
    <h2 class="site-header__welcome-user"> Welcome, ${profile.firstname} </h2>
    <p class="site-header__instructions">
        Choose a directory from your filesystem or test out the querying modes on the default corpus! After uploading a
        directory, it will show up under the 'Current Directories' card.
    </p>
    <img src="${profile.url}" class="site-header__profile-picture" alt="${profile.firstname}'s profile picture">
`;

const generateProfileContent = () => `
    <section class="card profile-directories bg-gradient">
        <h3 class="card-title">Current Directories</h3>
        <ul class="profile__user-dir-list"></ul>
    </section>
    <section class="card profile-upload bg-gradient">
        ${generateUploadDirectoryForm()}
    </section>
    <section class="card profile-scrape bg-gradient">
        ${generateScrapeWebsiteForm()}
    </section>
    <section class="card profile-default-directory bg-gradient">
        <h3 class="card-title">Query Default Directory</h3>
        <p class="profile__default-dir-note"> Test the application on a default directory of 30,000 .JSON files scraped from NPS.gov! </p>
        <ul class="profile__upload-restrictions-note"> 
            <li> Query the directory using the boolean retrieval mode, or using ranked retrieval </li>
            <li> Some results may provide broken links due to the website updating its pages </li>
        </ul>
        <button id="profile__default-button" class="site__button">Go!</button>
    </section>
`;

const generateUploadDirectoryForm = () => `
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

const generateScrapeWebsiteForm = () =>
    `
    <h3 class="card-title"> Scrape Website </h3>
    <p class="profile__upload-website-note"> Scrape a website's pages into .JSON files.  </p>
    <ul class="profile__upload-restrictions-note"> 
            <li> Accepts the homepage URL of a website </li>
            <li> .JSON files will be saved into a directory that can be queried </li>
            <li> Certain sites cannot be scraped due to strict security policies </li>
        </ul>
    <button id="profile__scrape-button" class="site__button" type="submit"> Go! </button>
    `;

const populateDirectoryList = (directories) =>
{
    const userDirectoryList = document.querySelector(".profile__user-dir-list");
    userDirectoryList.innerHTML = '';

    if (directories.length === 0)
    {
        userDirectoryList.innerHTML = "<p>No directories found.</p>";
    }
    else
    {
        directories.forEach(directory =>
        {
            userDirectoryList.appendChild(createDirectoryItem(directory));
        });
    }
};

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

export const attachProfileListeners = () =>
{
    document.querySelectorAll('.user-dir-btn').forEach(button =>
    {
        button.addEventListener('click', handleDirectoryButtonClick);
    });

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
    document.querySelector('#profile__scrape-button').addEventListener('click', event =>
    {
        displayScrapeWebsitePage();
    });
};

const handleDirectoryButtonClick = (event) =>
{
    const target = event.target;
    const directoryID = target.id;

    if (target.classList.contains('delete-btn'))
    {
        deleteUserDirectory(directoryID);
    }
    else if (target.classList.contains('query-btn'))
    {
        setUploadedDirectory(directoryID);
    }
};

const handleDefaultDirectory = async () =>
{
    try
    {
        await setDefaultDirectory();
    }
    catch (error)
    {
        console.error('Error setting default directory:', error);
    }
};

const handleUploadDirectory = async () =>
{
    const files = document.querySelector('#folderInput').files;

    try
    {
        const verificationResponse = await verifyUploadedDirectory(files);
        await handleUploadResponse(verificationResponse, files);
    }
    catch (error)
    {
        displayErrorMessage("An error occurred while processing your upload.");
    }
};


const handleUploadResponse = async (response, files) =>
{
    if (response.valid)
    {
        clearErrorMessage();
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

const startUpload = (files) =>
{
    const loadingSpinner = createLoadingSpinner();
    document.querySelector('.profile-upload__content').innerHTML = '';
    document.querySelector('.profile-upload__content').appendChild(loadingSpinner);

    const formData = convertToFormData(files);
    sendFormDataToServlet(formData);
};

const displayErrorMessage = (message) =>
{
    const errorMessageElement = document.querySelector("#error-message");
    errorMessageElement.textContent = message;
    errorMessageElement.style.display = 'block';
};

const clearErrorMessage = () =>
{
    const errorMessageElement = document.querySelector("#error-message");
    if (errorMessageElement)
    {
        errorMessageElement.style.display = "none";
    }
};

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

    attachSelectButtonListener(files);
};

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