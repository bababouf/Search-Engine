import {displayProfilePage} from "../content/displayProfilePage.js";

/*
When a user logs in, an ID token (unique to that session) is generated. This method sends that id token to a servlet
in a POST request to verify (which pretty much makes sure that the ID has been properly signed by Google and is legitimate.
If verification succeeds, a method to display the user's profile page is called.
 */
window.handleCredentialResponse = function(response) {

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

/*
As per the Google oauth 2.0 documentation on the Javascript API: https://developers.google.com/identity/gsi/web/reference/js-reference.
This sets up the Google Sign In client instance, allowing for the One Tap sign in and Sign In with Google buttons to display.
 */
window.onload = function () {
    google.accounts.id.initialize({
        client_id: '529467941335-rrllroamg3ebfvgp9n9i8qeni04tguca.apps.googleusercontent.com',
        callback: handleCredentialResponse
    });

    // Render the Google Sign-In button
    google.accounts.id.renderButton(
        document.getElementById('buttonDiv'),
        { theme: 'outline', size: 'large' }
    );

    // Prompt the One Tap sign-in
    google.accounts.id.prompt();
};



