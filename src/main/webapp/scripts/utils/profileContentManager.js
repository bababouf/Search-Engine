/*
This file manages setting and retrieving the profile page content, allowing for the "back to home" button to take the user
back to the profile page.
 */

let initialContent = '';
let headerInstructionsText = '';

// Sets the "initialContent" global variable to the parameter passed
export const setInitialContent = (content) =>
{
    initialContent = content;
}

// Returns the "initialContent" global variable
export const getInitialContent = () =>
{

    const instructions = document.querySelector('.site-header__instructions');
    if (instructions)
    {
        instructions.style.display = 'block';
        instructions.textContent = headerInstructionsText;
    }
    return initialContent
}

// Captures the innerHTML of the main element. This is all the content on the homepage (besides header content)
export const captureMainContent = () =>
{
    const mainElement = document.querySelector('main');
    setInitialContent(mainElement.innerHTML);

    headerInstructionsText = document.querySelector('.site-header__instructions').textContent;
};