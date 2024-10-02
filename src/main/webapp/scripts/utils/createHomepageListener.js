import {captureHomepageContent, setHomepageContent} from "./homepageContentManager.js";

// Ensure the content is loaded before capturing the content on the page and initializing listeners
window.addEventListener('DOMContentLoaded', () =>
{
    captureHomepageContent();
    initializeHomePageEventListeners();
});

// Initializes listeners for the documentation anchor and the "get started" button
export const initializeHomePageEventListeners = () =>
{
    // Load the login.html page when the "get started" button is clicked
    const nextButton = document.querySelector('#homepage__button-start');
    nextButton.addEventListener('click', event =>
    {
        window.location.href = '../../html/login.html'
    });
};


