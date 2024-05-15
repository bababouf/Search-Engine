// Removes all the main elements from the DOM
export const removeMainElements = () => {
    const mainElement = document.querySelector('main');
    mainElement.innerHTML = '';
}
