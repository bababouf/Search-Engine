import {removeMainElements} from "../utils/removeMainElements.js";


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
    const backButtonContainer = createBackToSearchButton(buttonId);
    mainElement.appendChild(backButtonContainer);
}

const handleNoResults = () => {

    const resultsDiv = document.createElement('div');
    const message = document.createElement('h3');
    message.textContent = 'No results';
    resultsDiv.appendChild(message);
    return resultsDiv;
}

const displayResults = (listOfResults) => {

    const resultsDiv = document.createElement('div');
    resultsDiv.classList.add('card-container');
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

const createBackToSearchButton = (buttonId) => {
    const backButtonContainer = document.createElement('div');
    backButtonContainer.classList.add('back-button-container');
    backButtonContainer.style.display = 'block';
    const backButton = document.createElement('button');
    backButton.classList.add('back-button');
    backButton.id = `${buttonId}`;
    backButton.textContent = 'Back to Search';
    backButtonContainer.appendChild(backButton);
    return backButtonContainer;
}

