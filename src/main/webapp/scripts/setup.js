import { displayDocumentationPage } from "./content/displayDocumentationPage.js";
import { displaySelectDirectory } from "./content/displaySelectDirectory.js";
import {captureMainContent, setInitialContent} from "./utils/homepageContentManager.js";

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
    mainContent = captureMainContent();
    setInitialContent(mainContent);
    initializeHomePageEventListeners();
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
        displaySelectDirectory(); // Calls method to handle displaying "select directory" page
    });
};




