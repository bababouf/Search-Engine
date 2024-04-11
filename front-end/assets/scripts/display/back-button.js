export const displayBackButton = () => {
    const mainElement = document.querySelector('main');
    const backButtonContainer = document.querySelector('.backbutton-container');
    const backButton = document.querySelector('#back-button');
    backButtonContainer.style.display = 'flex';
    backButton.style.display = 'block';
    mainElement.appendChild(backButtonContainer);
}