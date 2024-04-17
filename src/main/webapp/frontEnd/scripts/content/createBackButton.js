export const createBackButton = () => {

    const backButtonContainer = document.createElement('div');
    backButtonContainer.classList.add('back-button-container');
    const backButton = document.createElement('button');
    backButton.classList.add('back-button');
    backButton.textContent = 'Back to Home';
    backButtonContainer.appendChild(backButton);

    return backButtonContainer;

}