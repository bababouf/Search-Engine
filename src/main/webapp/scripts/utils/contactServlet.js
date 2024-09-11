import {displayProfilePage} from "../display/displayProfilePage.js";
import {displayQueryResultsPage} from "../display/displayQueryResultsPage.js";
import {displaySiteHeader} from "../display/displaySiteHeader.js";
import {displayBooleanSearchPage} from "../display/displayBooleanSearchPage.js";
import {displayRankedSearchPage} from "../display/displayRankedSearchPage.js";

/*
This file contains all the methods that contact the backend servlets (excluding those that are used to configure the
servlet for indexing a directory -- these are held in a separate file).
 */

// Sends the Google unique id token to the servlet for verification
export const handleCredentialResponse = (response) =>
{
    fetch('/oauth2callback', {
        method: 'POST', headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        }, body: 'id_token=' + encodeURIComponent(response.credential),
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
            // Creates and displays the components necessary for the profile page
            displayProfilePage();
        })
        .catch(error => console.error('There was a problem with the fetch operation:', error));
}

// Retrieves user profile information (first name, profile picture, and the user's uploaded directories)
export const getProfileInformation = () =>
{
    return fetch(`/retrieveProfile`, {
        method: 'GET'
    })
        .then(response =>
        {
            if (!response.ok)
            {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(profileInfo =>
        {
            // Convert from JSON string back into JS object
            return JSON.parse(profileInfo);
        })
        .catch(error =>
        {
            console.error('There was a problem with the fetch operation:', error);
            throw error;
        });
};

// Used to send form data to the servlet (used when user is uploading files)
export const sendFormDataToServlet = (formData) =>
{
    fetch('/upload', {
        method: 'POST',
        body: formData
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
            // Create and display the profile page
            displayProfilePage();
        })
        .catch(error =>
        {
            console.error('There was a problem with the fetch operation:', error);
        });
};

// Sends the user's query (and if applicable, the rankedMode) to the servlet for processing
export const sendQueryToServlet = (endpoint, rankedMode) =>
{
    const value = document.querySelector('#query');

    console.log("Query value: " + value.value);
    fetch(`/search${endpoint}`, {
        method: 'POST',
        body: JSON.stringify(
            {query: value.value, mode: rankedMode})
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
            // Display the results obtained from the servlet
            displayQueryResultsPage(responseText, endpoint);
        })
        .catch(error =>
        {
            console.error('There was a problem with the fetch operation:', error);
        });
}

// Contacts the boolean servlet or ranked servlet, depending on the endpoint
export const contactServlet = (endpoint) =>
{

    fetch(`/search${endpoint}`, {
        method: 'GET'
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
            console.log("removing loading spinner")
            // Remove the loading spinner to display the search page
            const loadingSpinner = document.querySelector("#loading-spinner");
            console.log("removing loading spinner")
            loadingSpinner.remove();

            // Check which endpoint was used and display the appropriate page
            if (endpoint === "/booleansearch")
            {
                displayBooleanSearchPage();
            }
            else
            {
                displayRankedSearchPage();
            }


        })
        .catch(error =>
        {
            console.error('There was a problem with the fetch operation:', error);
        });
}