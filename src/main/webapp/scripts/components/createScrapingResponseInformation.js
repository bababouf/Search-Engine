import {createBackToHomeButton} from "./createBackToHomeButton.js";

export const createScrapingResponseInformation = (responseText) => {

    const responseMessage = document.createElement('p');
    responseMessage.textContent = responseText.message;
    responseMessage.style.gridColumn = '1/3';

    const pageCount = responseText.pageCount;

    if(pageCount === 0)
    {
        responseMessage.textContent = "A scrape was attempted on the desired website, but security measures have prevented the scrape from continuing. "
    }
    else
    {
        responseMessage.textContent = `The scrape has completed. ${pageCount} pages have been found and compiled into a directory that can be found on your profile page. `;
    }

    const backButton = createBackToHomeButton();
    const mainElement = document.querySelector('main');
    mainElement.appendChild(responseMessage);
    mainElement.appendChild(backButton);
}