import {displayQueryModesPage} from "../content/displayQueryModesPage.js";

/*
This method makes a GET request to the servlet so that the default corpus that the user selected can be set. If no errors
occur, a call to a method to "displayQueryModes" page will occur.
 */
export const setDefaultDirectoryAtServer = () => {

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

export const setUserUploadedDirectoryAtServer = (containerName) => {

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