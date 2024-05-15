// Creates HTML for a back button
export const createBackButton = () => {

    const backButtonContainer = document.createElement('div');
    backButtonContainer.classList.add('back-button-container');
    backButtonContainer.innerHTML = `
    <button class ="back-to-home-button"> Back to Home </button>
    `;
    return backButtonContainer;

}