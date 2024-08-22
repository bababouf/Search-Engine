// Creates "error-message" paragraph and text
export const createErrorMessage = (errorMessage) =>
{
    const errorMsg = document.createElement('p');
    errorMsg.id = "error-message";
    errorMsg.textContent = `${errorMessage}`;
    errorMsg.style.textAlign = 'center';
    errorMsg.style.color = 'red';

    return errorMsg;
}