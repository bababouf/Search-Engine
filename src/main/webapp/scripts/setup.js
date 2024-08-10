


window.addEventListener('DOMContentLoaded', () => {

    initializeHomePageEventListeners();

});

/*
Creates an event listener for the documentation anchor and the "get started" button.
 */
export const initializeHomePageEventListeners = () => {

    // Clicking the "get started" button loads the login.html page
    const nextButton = document.querySelector('.homepage__get-started');
    nextButton.addEventListener('click', event => {
        window.location.href = '../html/login.html';

    });
};


