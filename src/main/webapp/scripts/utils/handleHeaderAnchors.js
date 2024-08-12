import {displayDocumentationPage} from "../display/displayDocumentationPage.js";
import {displayProfilePage} from "../display/displayProfilePage.js";

export const createHeaderAnchorElements = () => {

    if(document.querySelector("#home__anchor") == null && document.querySelector("#documentation__anchor") == null)
    {
        const heroNavbar = document.querySelector(".hero__navbar");

        const homeAnchor = document.createElement('li');
        homeAnchor.innerHTML = `
        <a id="home__anchor" href="#"> Home </a>
    `;
        heroNavbar.appendChild(homeAnchor);

        const documentationAnchor = document.createElement('li');
        documentationAnchor.innerHTML = `
        <a id="documentation__anchor" href="#"> Documentation </a>
    `;
        heroNavbar.appendChild(documentationAnchor);
    }

}

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