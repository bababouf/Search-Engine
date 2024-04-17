import {displayQueryResultsPage} from "../content/displayQueryResultsPage.js";

export const verifyQueryDispatch = (buttonId, rankedMode = null) => {

    let endpoint = '';

    if(buttonId === 'boolean-button')
    {
        endpoint = '/booleansearch';
    }
    else if(buttonId === 'ranked-button')
    {
        endpoint = '/rankedsearch';
    }

    verifyQuery(endpoint, rankedMode);
}

const verifyQuery = (endpoint, rankedMode) => {
    const value = document.querySelector('#query');
    console.log(value.value);

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
            displayQueryResultsPage(responseText);
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}
