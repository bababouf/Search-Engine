import {displayQueryResultsPage} from "../content/displayQueryResultsPage.js";


export const verifyQueryDispatch = (buttonId, rankedMode = null) => {

    let endpoint = setEndpoint(buttonId);
    sendToServlet(endpoint, rankedMode, buttonId)
}

// Depending on which retrieval mode was selected, the proper endpoint is set
const setEndpoint = (buttonID) => {
    let endpoint = '';
    if (buttonId === 'boolean-button') {
        endpoint = '/booleansearch';
    } else if (buttonId === 'ranked-button') {
        endpoint = '/rankedsearch';
    }
    return endpoint;
}

// This method sends the query and mode to the servlet for processing
const sendToServlet = (endpoint, rankedMode, buttonId) => {
    const value = document.querySelector('#query');

    fetch(`/search${endpoint}`, {
        method: 'POST',
        body: JSON.stringify(
            {query: value.value, mode: rankedMode})
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(responseText => {
            displayQueryResultsPage(responseText, buttonId);
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}
