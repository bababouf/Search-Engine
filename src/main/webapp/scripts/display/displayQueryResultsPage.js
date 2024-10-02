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

    if (listOfResults?.length === 0 ?? null)
    {
        resultsDiv = handleNoResults();
    }
    else
    {
        resultsDiv = displayResults(listOfResults);
    }

    const resultsHeaderDiv = document.createElement('div');
    resultsHeaderDiv.classList.add('query-results__header');

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
    resultsDiv.classList.add('site__main-content');

    resultsDiv.innerHTML = `
        <h3 class="site__h3"> No Results. </h3>
    `;

    return resultsDiv;
}

const displayResults = (listOfResults) =>
{
    const resultsDiv = document.createElement('div');
    resultsDiv.classList.add('query-results__container');

    listOfResults.forEach((result, index) =>
    {
        const card = createResultCard(result, index, listOfResults);
        resultsDiv.appendChild(card);
    });

    return resultsDiv;
};

// Helper function to create result cards
const createResultCard = (result, index, listOfResults) =>
{
    const card = document.createElement('div');
    card.classList.add('query-results__card');

    if (!result.content)
    {
        card.innerHTML = `
            <p>${result.title}</p>
            <a class="result-link" href="${result.url}">URL</a>
        `;
    }
    else
    {
        card.innerHTML = `
            <p>${result.title}</p>
            <button class="query-results__button" id="result-${index}">Display File</button>
        `;
        const button = card.querySelector('.query-results__button');
        button.addEventListener('click', () => displayFile(result, index));
    }

    return card;
};

// Helper function to display the file
const displayFile = (result, index) =>
{
    const displayResultDiv = document.createElement('div');
    displayResultDiv.id = "display-results-container";
    displayResultDiv.classList.add('flex-row-center');
    displayResultDiv.innerHTML = `
        <div class="card bg-gradient">
            <h3>${result.title}</h3>
            <p>${result.content}</p>
            <button id="back-to-results-button">Back to Results</button>
        </div>
    `;

    const resultsContainer = document.querySelector('.query-results__container');
    resultsContainer.style.display = 'none';

    const mainElement = document.querySelector('main');
    mainElement.appendChild(displayResultDiv);

    const backButton = document.querySelector('#back-to-results-button');
    backButton.addEventListener('click', () =>
    {
        displayResultDiv.remove();
        resultsContainer.style.display = 'flex';
    });
};

