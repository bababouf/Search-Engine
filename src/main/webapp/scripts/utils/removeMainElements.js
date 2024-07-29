// Removes all the main elements from the DOM
export const removeMainElements = () => {
    const mainElement = document.querySelector('main');
    if(mainElement)
    {
        mainElement.innerHTML = '';
    }
    else
    {
        const mainElement = document.createElement('main');
        return mainElement;
    }

}
