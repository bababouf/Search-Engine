import {removeMainElements} from "../utils/removeMainElements.js";
import {createSearchBar} from "../components/createSearchBar.js";
import {createBackToHomeButton} from "../components/createBackToHomeButton.js";
import {contactServlet, sendQueryToServlet} from "../utils/contactServlet.js";
import {createLoadingSpinner} from "../components/createLoadingSpinner.js";

/*
This file contains methods for creating and displaying the "ranked search" page. The HTML to display the page is created,
and event listeners are attached to each of the ranking schemes that the user may select. Before the ranked search page is
displayed, a page containing information on ranked searches is first displayed. When the user clicks the next button, the
ranked search page is displayed.
 */

let rankedMode = null; // Initially null, this global variable is set when a ranking scheme is selected
const endpoint = '/rankedsearch';

// Displays the ranked search instructions, as well as a next button to proceed to the ranked search page
export const displayRankedInstructions = () =>
{
    // Remove all elements from the main
    removeMainElements();

    // Create and attach instructions content
    const instructions = createRankedInstructions();
    const mainElement = document.querySelector('main');
    mainElement.appendChild(instructions)

    // Attach listener to the next button
    const nextButton = document.querySelector('#next-button');
    nextButton.addEventListener('click', () =>
    {
        // Remove the next button
        const nextButton = document.querySelector('#next-button');
        nextButton.remove();

        // Create and attach loading spinner
        const loadingSpinner = createLoadingSpinner();
        instructions.appendChild(loadingSpinner);

        // Contact servlet to prep the server for the index (and ranking mode) that will be queried
        contactServlet(endpoint)
    });

}

// Creates HTMl for ranked retrieval instructions
const createRankedInstructions = () =>
{
    const rankedSearchDiv = document.createElement('div');
    rankedSearchDiv.classList.add('flex-column');

    rankedSearchDiv.innerHTML = `
        <p class="ranking-schemes__description" > 
        Ranked queries treat each query as a "bag of words," meaning they don’t have a fixed structure. For example, a query like 
        "dogs" will rank documents with various forms of "dog" highly. However, this approach falters with queries like "the dogs," 
        where a document might contain "the" frequently but lack "dog" variations. Thus, document ranking is based on a combination 
        of the query term's weight and its weight within each document.
        </p>
        <button id="next-button">Next</button>
        
    `;

    return rankedSearchDiv;
}

// When the next button is clicked, the ranked modes, search bar, and back button are displayed
export const displayRankedSearchPage = () =>
{
    // Remove main elements
    removeMainElements();
    const mainElement = document.querySelector('main');

    // Create and attach ranking modes, search bar, and back button
    const rankedModes = createRankedModes();
    const searchBar = createSearchBar();
    const backButton = createBackToHomeButton();

    rankedModes.appendChild(searchBar);
    rankedModes.appendChild(backButton);
    mainElement.appendChild(rankedModes);

    // Enables toggling of the different modes; when a user clicks a ranking scheme, it turns "active"
    toggleModeSelection();
    attachQuerySubmitListener();
}

// Creates HTML for the various ranked retrieval modes the user can select from
const createRankedModes = () =>
{
    const rankingSchemes = document.createElement('div');
    rankingSchemes.classList.add('ranking-schemes');

    rankingSchemes.innerHTML = `
    <p class="ranking-schemes__description"> 
    Each ranking scheme offers a slightly different approach in how documents are ranked. Each formula calculates three 
    variables used in the ranking: weight of query term (WQT), weight of document term (WDT), and LD (normalizing factor to 
    account for varying document lengths). 
     </p>
    <div class="ranking-schemes__card-container">
    <div class="card bg-gradient" style="width: 300px">
        <h3 class="card-title"> Default </h3>
        
        <button id ="default-ranked" class="ranked-button site__button"> Select </button>
  
    </div>
    <div class="card bg-gradient" style="width: 300px">
        <h3 class="card-title"> TFIDF </h3>
        
        <button id ="tfidf-ranked" class="ranked-button site__button"> Select </button>
    </div>
    <div class="card bg-gradient" style="width: 300px">
        <h3 class="card-title"> Okapi BM25 </h3>
        
        <button id ="okapi-ranked" class="ranked-button site__button"> Select </button>
    </div>
    <div class="card bg-gradient" style="width: 300px">
        <h3 class="card-title"> Wacky </h3>
        
        <button id ="wacky-ranked" class="ranked-button site__button"> Select </button>
    </div>
    </div>
    `;

    return rankingSchemes;
}

// Creates a listener on each ranked mode button that will toggle (select) one of the ranked modes at a time
const toggleModeSelection = () =>
{
    const rankModeButtons = document.querySelectorAll('.ranked-button');
    rankModeButtons.forEach(button =>
    {
        button.addEventListener('click', (event) =>
        {
            document.querySelector?.(".active")?.classList?.remove('active'); // Deselects the previously selected ranked mode (if there was one)
            event.currentTarget.classList.add('active'); // Selects the new mode
            rankedMode = event.currentTarget.id;
            console.log(`selecting ${event.currentTarget.id}`)
        })
    })

}

// Creates listener for submitting a query
const attachQuerySubmitListener = () =>
{
    console.log("in the submit query listener")
    const form = document.querySelector('#search-form');
    form.addEventListener('submit', (event) =>
    {
        event.preventDefault();

        // Creates error message if no mode was selected
        if (rankedMode === null)
        {
            createErrorMessage();
        }
        else
        {
            sendQueryToServlet(endpoint, rankedMode);
            rankedMode = null;
        }

    });
}

// Creates the HTML and CSS for the error message that will be displayed if no mode was selected
const createErrorMessage = () =>
{
    const errorMsg = document.createElement('p');
    errorMsg.textContent = 'Please select a ranked mode before submitting a query. ';
    errorMsg.style.color = '#880808';
    const searchContainer = document.querySelector('.search-container');
    const rankingSchemes = document.querySelector('.ranking-schemes');
    rankingSchemes.insertBefore(errorMsg, searchContainer);
}

