// Creates "error-message" paragraph and text
export const createErrorMessage = () => {
    const errorMsg = document.createElement('p');
    errorMsg.id = "error-message";
    errorMsg.textContent = 'Directory must contain all .TXT files or all .JSON files. ';
    errorMsg.style.textAlign = 'center';
    errorMsg.style.color = 'red';

    return errorMsg;
}