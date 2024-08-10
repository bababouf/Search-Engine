import {displayDocumentationPage} from "../content/displayDocumentationPage.js";
import {displayProfilePage} from "../content/displayProfilePage.js";

export const attachHeaderAnchorListeners = () => {
    const documentationAnchor = document.querySelector("#documentation__anchor");
    documentationAnchor.addEventListener('click', event => {
        displayDocumentationPage();

    });

    const homeAnchor = document.querySelector("#home__anchor");
    homeAnchor.addEventListener('click', event => {
        displayProfilePage();

    });
}