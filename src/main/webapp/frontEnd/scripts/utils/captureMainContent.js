export const captureMainContent = () => {
    const mainElement = document.querySelector('main');
    return mainElement.innerHTML;
};