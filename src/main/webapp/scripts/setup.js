import { displayDocumentationPage } from "./content/displayDocumentationPage.js";
import { captureMainContent, setInitialContent } from "./utils/homepageContentManager.js";

// Function to check if we're on the homepage
const isHomePage = () => {
    return window.location.pathname === '/' || window.location.pathname.endsWith('index.html');
};

/*
This file runs when the homepage is initially loaded, creating event listeners for the "Get started" button, as well as
the navigation tab (documentation) in the header.
 */

/*
Once the DOM is loaded, the homepage content (HTML content appended to the main element) is saved. This allows for the "back
to home" buttons on other pages to display the homepage content.
 */
let mainContent = null;
window.addEventListener('DOMContentLoaded', () => {
    if (!isHomePage()) {
       return;
    }
    console.log('DOM fully loaded and parsed');
    mainContent = captureMainContent();

    if (mainContent) {
        console.log('Main content captured:', mainContent);
        setInitialContent(mainContent);
        initializeHomePageEventListeners();
    } else {
        console.error('Failed to capture main content');
    }
});

/*
Creates an event listener for the documentation anchor and the "get started" button.
 */
export const initializeHomePageEventListeners = () => {
    const documentationAnchor = document.querySelector('.documentation__anchor');
    documentationAnchor.addEventListener('click', event => {
        event.preventDefault();
        displayDocumentationPage(); // Calls method to handle displaying documentation page
    });

    const nextButton = document.querySelector('.homepage__get-started');
    nextButton.addEventListener('click', event => {
        console.log('Next button clicked');
        window.location.href = '../html/login.html';
        //displaySelectDirectory(); // Calls method to handle displaying "select directory" page
    });
};


