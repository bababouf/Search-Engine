import { displayDocumentationPage } from "./content/displayDocumentationPage.js";

// Function to check if on the homepage
const isHomePage = () => {
    return window.location.pathname === '/' || window.location.pathname.endsWith('index.html');
};

window.addEventListener('DOMContentLoaded', () => {
    if (isHomePage()) {
        initializeHomePageEventListeners();
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

    // Clicking the "get started" button loads the login.html page
    const nextButton = document.querySelector('.homepage__get-started');
    nextButton.addEventListener('click', event => {
        window.location.href = '../html/login.html';

    });
};


