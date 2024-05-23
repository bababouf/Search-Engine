import { displayDocumentationPage } from "./content/displayDocumentationPage.js";
import { displaySelectDirectory } from "./content/displaySelectDirectory.js";
import {captureMainContent, setInitialContent} from "./utils/homepageContentManager.js";

let mainContent = null;
window.addEventListener('DOMContentLoaded', () => {
    mainContent = captureMainContent();
    setInitialContent(mainContent);
    initializeHomePageEventListeners();
});

export const initializeHomePageEventListeners = () => {
    const documentationAnchor = document.querySelector('.documentation__anchor');
    documentationAnchor.addEventListener('click', event => {
        event.preventDefault();
        displayDocumentationPage();
    });

    const nextButton = document.querySelector('.homepage__get-started');
    nextButton.addEventListener('click', event => {
        displaySelectDirectory();
    });
};




