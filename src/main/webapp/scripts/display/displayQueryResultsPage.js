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
    removeMainElements();
    createResultsPage(response, endpoint);
}

const createResultsPage = (response, endpoint) => {

    const mainElement = document.querySelector('main');
    const listOfResults = JSON.parse(response);
    let resultsDiv;

    if (listOfResults?.length === 0 ?? null)
    {
        resultsDiv = handleNoResults();
    }
    else
    {
        resultsDiv = displayResults(listOfResults);
    }

    const resultsHeaderDiv = document.createElement('div');
    resultsHeaderDiv.classList.add('site__results-header');
    resultsHeaderDiv.innerHTML = `
        <h3 class="site__h3"> Displaying ${listOfResults.length} results for query. </h3>
    `;

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
    resultsDiv.classList.add('flex-row');
    resultsDiv.style.flexWrap = 'wrap';

    listOfResults.forEach(result =>
    {
        const card = document.createElement('div');
        card.classList.add('card');
        card.setAttribute('flex', 'flex: 1 0 0');

        card.innerHTML = `
        <p>${result.title}</p>
        <a class = "result-link" href = "${result.url}">URL</a>
        `
        resultsDiv.appendChild(card);

    });

    return resultsDiv;

}

