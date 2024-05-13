import {displayQueryModesPage} from "../content/displayQueryModesPage.js";


//This method contacts the servlet so that it knows the default directory has been selected.
export const setDirectoryPathAtServer = () => {

    fetch(`/home`, {
        method: 'GET'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(responseText => {
            displayQueryModesPage();
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}