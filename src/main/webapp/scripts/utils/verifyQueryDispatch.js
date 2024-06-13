import {displayQueryResultsPage} from "../content/displayQueryResultsPage.js";

/*
This file contains methods for sending a user-entered query in the browser to the proper servlet for processing. The same
method is used for both boolean queries and ranked queries, but each is processed in its own servlet (with a unique
endpoint). Boolean queries are processed in the servlet with "/booleansearch" endpoint, and ranked queries are processed
in the servlet with the "/rankedsearch" endpoint.
 */
export const verifyQueryDispatch = (buttonId, rankedMode = null) => {

    let endpoint = setEndpoint(buttonId);
    sendToServlet(endpoint, rankedMode, buttonId)
}

// The button id will either contain "boolean-button" or "ranked-button", and this allows for the correct endpoint to be set
const setEndpoint = (buttonId) => {
    let endpoint = '';
    if (buttonId === 'boolean-button') {
        endpoint = '/booleansearch';
    } else if (buttonId === 'ranked-button') {
        endpoint = '/rankedsearch';
    }
    return endpoint;
}

/*
This method sends a GET request to the endpoint passed as a parameter. In the body of the request, the user-entered query
is set, as well as the ranking scheme selected (this will be null for boolean queries). If no errors occur, a call to a method
that displays the query results will be made.
 */
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
