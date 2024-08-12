/*
This method makes a GET request to the servlet so that the default corpus that the user selected can be set. If no errors
occur, a call to a method to "displayQueryModes" page will occur.
 */
import {displayBooleanSearchPage} from "../display/displayBooleanSearchPage.js";
import {displayQueryModesPage} from "../display/displayQueryModesPage.js";

export const setDefaultDirectory = () => {

    fetch(`/configure`, {
        method: 'GET'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(responseText => {
            displayQueryModesPage(); // Calls method to display "query modes" page
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}

export const setUploadedDirectory = (containerName) => {

    fetch(`/configure`, {
        method: 'POST',
        body: JSON.stringify(
            {containerName: containerName})
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(responseText => {
            displayQueryModesPage(); // Calls method to display "query modes" page
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}

export const downloadCorpus = (buttonId) => {

    if(buttonId === "boolean-button")
    {
        contactServlet("booleansearch")
    }
    else
    {
        contactServlet("rankedsearch")
    }

}

/*
The method sends a GET request to the /search/booleansearch endpoint. When the servlet receives this request, it will
take action to setup the corpus and download the index representing the corpus the user has selected to query.
 */
const contactServlet = (endpoint) => {

    fetch(`/search/${endpoint}`, {
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
            console.log("Successfully contacted servlet")
            const loadingSpinner = document.querySelector("#loading-spinner");
            loadingSpinner.remove();
            displayBooleanSearchPage();

        })
        .catch(error =>
        {
            console.error('There was a problem with the fetch operation:', error);
        });
}