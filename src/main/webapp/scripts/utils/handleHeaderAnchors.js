import {displayDocumentationPage} from "../display/displayDocumentationPage.js";
import {displayProfilePage} from "../display/displayProfilePage.js";

// Creates the header anchors that are used in the site header
export const createHeaderAnchorElements = () =>
{
    // Check that the header anchors dont already exist
    if (document.querySelector("#site__profile-anchor") == null && document.querySelector("#site__documentation-anchor") == null)
    {
        const heroNavbar = document.querySelector(".site__hero-anchors");
        heroNavbar.innerHTML = `
            <li><a id="site__profile-anchor" href="#"> Profile </a> </li>
            <li><a id="site__documentation-anchor" href="#"> Documentation </a> </li>
        `;
    }

}

// Attach listeners to each of the anchors
export const attachHeaderAnchorListeners = () =>
{
    const documentationAnchor = document.querySelector("#site__documentation-anchor");
    documentationAnchor.addEventListener('click', event =>
    {
        displayDocumentationPage();
    });

    const homeAnchor = document.querySelector("#site__profile-anchor");
    homeAnchor.addEventListener('click', event =>
    {
        displayProfilePage();
    });
}