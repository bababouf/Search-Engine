/* This method is used quite often to remove all elements attached to the main element. Throughout the application,
this method will be called to clear a page so that new content can be displayed. New content that is added is always
appended to the main element.
 */
export const removeMainElements = () =>
{
    const mainElement = document.querySelector("main");


    const instructions = document.querySelector('.site-header__instructions');

    // Clear all elements appended to the main element
    if (mainElement)
    {
        mainElement.innerHTML = '';
    }

    // Set the instructions that are located within the header element to display none
    if (instructions)
    {
        instructions.style.display = 'none';
    }

}
