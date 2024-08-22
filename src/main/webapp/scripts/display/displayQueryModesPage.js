import {removeMainElements} from "../utils/removeMainElements.js";
import {
    attachBackToHomeListener,
    createBackToHomeButton
} from "../components/createBackToHomeButton.js";
import {displayBooleanInstructions} from "./displayBooleanSearchPage.js";
import {displayRankedInstructions} from "./displayRankedSearchPage.js";

// Removes all children from the main element and displays the query modes content
export const displayQueryModesPage = () =>
{
    // Remove children from main element
    removeMainElements();

    // Create HTML for query modes
    createQueryModes();

    // Create the back button (to get back to the profile page)
    const backButton = createBackToHomeButton();

    // Append back button to the page
    const contentDiv = document.querySelector('.center-content');
    contentDiv.appendChild(backButton);

    // Call methods to attach listeners to the back button / query mode buttons
    attachBackToHomeListener(backButton);
    attachModeButtonListeners();
}

// Creates the HTML for the boolean and ranked method selection
const createQueryModes = () =>
{
    const mainElement = document.querySelector('main');

    mainElement.innerHTML = `
    <div class="center-content">
        <h2 class="site__h2">Select Query Mode</h2>
        <div class = "card-container">
            <div class="card">
                <h3 class="site__h3"> Boolean Retrieval Mode </h3>
                <img src="../../images/venn-diagram-logo.png" height="40" width="40">
                <p> Configures the program to handle boolean queries. </p>
                <button class = "query-modes__mode-button" id="boolean-button"> Go! </button>  
            </div>
            <div class="card">
                <h3 class="site__h3"> Ranked Retrieval Mode </h3>
                <img src="../../images/search-glass.png" height="40" width="40">
                <p> Configures the program to handle ranked requests. </p>
                <button class = "query-modes__mode-button" id="ranked-button"> Go! </button>  
            </div>
        </div>
        
    </div>
    `;

}

// Creates listeners to each of the method buttons
const attachModeButtonListeners = () =>
{
    const modeButtons = document.querySelectorAll(".query-modes__mode-button");
    modeButtons.forEach(button =>
    {
        button.addEventListener("click", queryModeDispatch);
    });
}

// Calls the appropriate method for handling each of the "query mode" button clicks
const queryModeDispatch = (event) =>
{
    const buttonId = event.currentTarget.id;

    if (buttonId === 'boolean-button')
    {
        displayBooleanInstructions();
    }
    else if (event.currentTarget.id === 'ranked-button')
    {
        displayRankedInstructions();
    }

}
