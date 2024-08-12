import {displayProfilePage} from "../display/displayProfilePage.js";
import {displayQueryResultsPage} from "../display/displayQueryResultsPage.js";

export const handleCredentialResponse = (response) => {
    fetch('/oauth2callback', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'id_token=' + encodeURIComponent(response.credential),
    })
        .then(response =>
        {
            if (!response.ok)
            {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(responseText =>
        {
            displayProfilePage();
        })
        .catch(error => console.error('There was a problem with the fetch operation:', error));
}

export const getProfileInformation = () => {
    return fetch(`/retrieveProfile`, {
        method: 'GET'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(profileInfo => {
            // Parse the JSON response
            const profile = JSON.parse(profileInfo);
            // Return the parsed profile object
            return profile;
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
            // Optionally return a fallback value or rethrow the error
            throw error;
        });
};

export const sendFormDataToServlet = (formData) => {
    fetch('/upload', {
        method: 'POST',
        body: formData
    })
        .then(response => {
            if (!response.ok)
            {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(responseText => {
            displayProfilePage();
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
};

export const sendQueryToServlet = (endpoint, rankedMode, buttonId) => {
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