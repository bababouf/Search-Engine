let initialContent = '';

// Sets the "initialContent" global variable to the parameter passed
export const setHomepageContent = (content) =>
{
    initialContent = content;
}

// Returns the "initialContent" global variable
export const getHomepageContent = () =>
{
    return initialContent
}

// Captures the innerHTML of the body element. This is all the content on the homepage
export const captureHomepageContent = () =>
{
    const bodyElement = document.querySelector('body');
    setHomepageContent(bodyElement.innerHTML);

};