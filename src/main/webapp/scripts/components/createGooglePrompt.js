import {handleCredentialResponse} from "../utils/contactServlet.js";

/*
As per the Google oauth 2.0 documentation on the Javascript API: https://developers.google.com/identity/gsi/web/reference/js-reference.
This sets up the Google Sign In client instance, allowing for the One Tap sign in and Sign In with Google buttons to display.
 */
window.onload = function ()
{
    google.accounts.id.initialize({
        client_id: '529467941335-rrllroamg3ebfvgp9n9i8qeni04tguca.apps.googleusercontent.com',
        callback: handleCredentialResponse
    });

    // Render the Google Sign-In button
    google.accounts.id.renderButton(
        document.getElementById('google-login'),
        {theme: 'filled_black', size: 'large', text: 'continue_with', shape: 'pill'}
    );

    // Prompt the One Tap sign-in
    google.accounts.id.prompt();
};



