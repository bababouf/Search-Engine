/*
This file manages setting and retrieving the homepage content, allowing for the "back to home" button to take the user
back to the homepage.
 */


let initialContent = '';

// Sets the "initialContent" global variable to the parameter passed
export const setInitialContent = (content) => {
    initialContent = content;
}

// Returns the "initialContent" global variable
export const getInitialContent = () => {
    return initialContent;
}

// Captures the innerHTML of the main element. This is all the content on the homepage (besides header content)
export const captureMainContent = () => {
    const mainElement = document.querySelector('main');
    return mainElement.innerHTML;
};