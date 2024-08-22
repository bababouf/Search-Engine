import {displayQueryModesPage} from "../display/displayQueryModesPage.js";

/*
This file contains method that configure the servlet for the directory that will be queried. It contains a method for setting
the default directory at the servlet, as well as a user uploaded directory.
 */

// Used to configure the servlet to handle querying the default directory
export const setDefaultDirectory = () =>
{

    fetch(`/configure`, {
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
            displayQueryModesPage(); // Calls method to display "query modes" page
        })
        .catch(error =>
        {
            console.error('There was a problem with the fetch operation:', error);
        });
}

/*
Used to configure the servlet for handling the user uploaded directory. In order to do this, the container name (built from
the hashed user ID and directory name) is passed to the servlet.
 */
export const setUploadedDirectory = (containerName) =>
{

    fetch(`/configure`, {
        method: 'POST',
        body: JSON.stringify(
            {containerName: containerName})
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
            // Displays the query modes that the user can select from
            displayQueryModesPage();
        })
        .catch(error =>
        {
            console.error('There was a problem with the fetch operation:', error);
        });
}

