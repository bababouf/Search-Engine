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

// Initialize Google Identity Services
window.onload = function() {
    console.log('Window onload - initializing Google Identity Services');
    google.accounts.id.initialize({
        client_id: "529467941335-rrllroamg3ebfvgp9n9i8qeni04tguca.apps.googleusercontent.com",
        use_fedcm_for_prompt: true,
        callback: (response) => {
            console.log('Callback executed');
            handleCredentialResponse(response);
        }
    });
    google.accounts.id.prompt(); // Show the Google Sign-In prompt

    // Optional: Check for existing credentials (e.g., for auto-reauthentication)
    getCredentials();
};

// Async function to get credentials using FedCM
async function getCredentials() {
    try {
        const cred = await navigator.credentials.get({
            identity: {
                providers: [{
                    configURL: "https://idp.example/fedcm.json", // Replace with your actual FedCM config URL
                    clientId: "529467941335-rrllroamg3ebfvgp9n9i8qeni04tguca.apps.googleusercontent.com", // Your client ID
                }],
            },
            mediation: 'optional', // Auto-reauth if possible
        });
        console.log('Credentials:', cred);
        // Handle the credentials here, e.g., send to server or update UI
    } catch (error) {
        console.error('Failed to get credentials', error);
    }
}