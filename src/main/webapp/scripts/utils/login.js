import { displaySelectDirectory } from "../content/displaySelectDirectory.js";

// Define the callback in the global scope
// Existing function to handle credential response
window.handleCredentialResponse = function(response) {
    console.log('ID Token: ' + response.credential);

    // Example of sending the token to your server
    fetch('/oauth2callback', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'id_token=' + encodeURIComponent(response.credential),
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(responseText => {
            console.log('Google sign-in successful, calling displaySelectDirectory');
            displaySelectDirectory();
        })
        .catch(error => console.error('There was a problem with the fetch operation:', error));
}

window.onload = function () {
    google.accounts.id.initialize({
        client_id: '529467941335-rrllroamg3ebfvgp9n9i8qeni04tguca.apps.googleusercontent.com',
        callback: handleCredentialResponse
    });
    google.accounts.id.prompt();
};

