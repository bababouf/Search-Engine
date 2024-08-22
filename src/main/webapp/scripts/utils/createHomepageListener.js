// Initialize event listener for the "get-started" button when the page loads
window.addEventListener('DOMContentLoaded', () =>
{
    initializeHomePageEventListeners();
});

/*
Creates an event listener for the documentation anchor and the "get started" button.
 */
const initializeHomePageEventListeners = () =>
{

    // Clicking the "get started" button loads the login.html page
    const nextButton = document.querySelector('.homepage__get-started-button');
    nextButton.addEventListener('click', event =>
    {
        // Displays the Google login prompt page
        window.location.href = '../../html/login.html'

    });
};


