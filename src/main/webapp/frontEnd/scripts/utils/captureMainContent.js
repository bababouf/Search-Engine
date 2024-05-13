// "Saves" the current main elements in the DOM
export const captureMainContent = () => {
    const mainElement = document.querySelector('main');
    return mainElement.innerHTML;
};