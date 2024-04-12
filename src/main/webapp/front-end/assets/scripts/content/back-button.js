export const displayBackButton = () => {
    const mainElement = document.querySelector('main');
    const backButtonContainer = document.querySelector('.back-button-container');
    const backButton = document.querySelector('.back-button');
    backButtonContainer.style.display = 'flex';
    backButton.style.display = 'block';
    mainElement.appendChild(backButtonContainer);
}