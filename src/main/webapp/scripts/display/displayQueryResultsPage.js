import {removeMainElements} from "../utils/removeMainElements.js";
import {
    attachBackToSearchListener,
    createBackToSearchButton
} from "../components/createBackToSearchButton.js";


/*
This file contains methods for creating and displaying the "query results" page
*/

export const displayQueryResultsPage = (response, endpoint) =>
{
    // Remove all elements from main
    removeMainElements();

    // Create and display results
    createResultsPage(response, endpoint);
}

// Creates a card that contains the title and URL for each result, displays in grid
const createResultsPage = (response, endpoint) =>
{
    const mainElement = document.querySelector('main');
    mainElement.classList.remove('site-content-grid');

    const listOfResults = JSON.parse(response);
    let resultsDiv;

    // Handle no results
    if (listOfResults?.length === 0 ?? null)
    {
        resultsDiv = handleNoResults();
    }
    // More than one result to display
    else
    {
        resultsDiv = displayResults(listOfResults);
    }

    const resultsHeaderDiv = document.createElement('div');
    resultsHeaderDiv.classList.add('query-results__header');

    resultsHeaderDiv.innerHTML = `
        <h3 class="site__h3"> Displaying ${listOfResults.length} results for query. </h3>
    `;

    // Attach back to search button to the results header
    const backToSearchButton = createBackToSearchButton();
    resultsHeaderDiv.appendChild(backToSearchButton);
    mainElement.appendChild(resultsHeaderDiv);

    attachBackToSearchListener(mainElement, backToSearchButton, endpoint);
    mainElement.appendChild(resultsDiv);

}

// Creates HTML to be displayed when no results are found
const handleNoResults = () =>
{
    const resultsDiv = document.createElement('div');
    resultsDiv.classList.add('site__main-content');

    resultsDiv.innerHTML = `
        <h3 class="site__h3"> No Results. </h3>
    `;

    return resultsDiv;
}

/*
Creates HTML for displaying the results. Results are displayed in rows, where each result is contained in a card (which
contains the title and link).
 */
const displayResults = (listOfResults) =>
{
    const resultsDiv = document.createElement('div');
    resultsDiv.classList.add('query-results__container');

    // Create card that contains title and URL of the results web page
    listOfResults.forEach(result =>
    {
        const card = document.createElement('div');
        card.classList.add('query-results__card');
        card.innerHTML = `
        <p>${result.title}</p>
        <a class = "result-link" href = "${result.url}">URL</a>
        `
        resultsDiv.appendChild(card);

    });

    return resultsDiv;

}

