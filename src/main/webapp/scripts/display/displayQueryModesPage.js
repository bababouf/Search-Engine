import {removeMainElements} from "../utils/removeMainElements.js";
import {

    createBackToHomeButton
} from "../components/createBackToHomeButton.js";
import {displayBooleanInstructions} from "./displayBooleanSearchPage.js";
import {displayRankedInstructions} from "./displayRankedSearchPage.js";

// Removes all children from the main element and displays the query modes content
export const displayQueryModesPage = () =>
{
    // Remove all elements from main
    removeMainElements();

    // Create and attach query modes content
    createQueryModes();

    // Create and attach back button
    const backButton = createBackToHomeButton();
    const mainElement = document.querySelector('main');
    mainElement.appendChild(backButton);
}

// Creates the HTML for the boolean and ranked method selection
const createQueryModes = () =>
{
    setHeaderInstructions();
    const mainElement = document.querySelector('main');
    mainElement.innerHTML = `
            
            <section class="card bg-gradient">
                <div class="flex-row-center">
                    <h3 class="card-title">Boolean Retrieval Mode</h3>
                </div>
                <p class="query-modes__card-description">Enables Boolean queries, allowing you to combine keywords with AND and OR operators, or search for exact phrases. This mode helps refine your search for more accurate results.</p>
                <button id="boolean-button" class="site__button">Go!</button>
            </section>
            <section class="card bg-gradient">
                <div class="flex-row-center">
                    <h3 class="card-title">Ranked Retrieval Mode</h3>
                </div>
                <p class="query-modes__card-description">Enables ranked retrieval, using algorithms to assign weights to query terms, document content, and length, providing more relevant and prioritized search results.</p>
                <button id="ranked-button" class="site__button">Go!</button>
            </section>
        
        `;

    mainElement.querySelectorAll('button').forEach(button =>
    {
        button.addEventListener('click', queryModeDispatch);
    });
}

// Sets the instructions paragraph text content in the header
const setHeaderInstructions = () =>
{
    const headerInstructions = document.querySelector('.site-header__instructions');
    headerInstructions.style.display = 'block';
    headerInstructions.textContent = 'Select a query mode below that will be used to process queries. ';
}

// Calls the appropriate method for handling each of the "query mode" button clicks
const queryModeDispatch = (event) =>
{
    const buttonId = event.currentTarget.id;

    if (buttonId === 'boolean-button')
    {
        displayBooleanInstructions();
    }
    else if (buttonId === 'ranked-button')
    {
        displayRankedInstructions();
    }

}
