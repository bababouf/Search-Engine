import {removeMainElements} from "../utils/removeMainElements.js";
import {appendBackToSearchButton} from "../utils/appendBackToSearchButton.js";


/*
This file contains methods for creating and displaying the "query results" page
*/

export const displayQueryResultsPage = (response, buttonId) => {

    removeMainElements();
    const mainElement = document.querySelector('main');
    const listOfResults = JSON.parse(response);
    let resultsDiv;

    if (listOfResults?.length === 0 ?? null) {
        resultsDiv = handleNoResults();
    } else {
        resultsDiv = displayResults(listOfResults);
    }

    const numberOfResultsMsg = document.createElement('h3');
    numberOfResultsMsg.textContent = `Displaying ${listOfResults.length} results for query. `;

    mainElement.appendChild(numberOfResultsMsg);
    mainElement.appendChild(resultsDiv);
    appendBackToSearchButton(buttonId);
}

// Creates HTML to be displayed when no results are found
const handleNoResults = () => {
    const resultsDiv = document.createElement('div');
    const message = document.createElement('h3');
    message.textContent = 'No results';
    resultsDiv.appendChild(message);
    return resultsDiv;
}

/*
Creates HTML for displaying the results. Results are displayed in rows, where each result is contained in a card (which
contains the title and link).
 */
const displayResults = (listOfResults) => {

    const resultsDiv = document.createElement('div');
    resultsDiv.classList.add('flex-row');
    resultsDiv.style.flexWrap = 'wrap';

    listOfResults.forEach(result => {
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

