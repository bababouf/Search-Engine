import {hideMainElements} from "../utils/hideMainElements.js";


export const displayQueryResultsPage = (response) => {
    hideMainElements();

    const listOfPages = JSON.parse(response);
    const resultsDiv = document.createElement('div');
    resultsDiv.classList.add('card-container');
    resultsDiv.style.flexWrap = 'wrap';

    let count = 0;
    listOfPages.forEach(result => {

        if (count < 30)
        {
            const card = document.createElement('div');
            card.classList.add('card');
            card.setAttribute('flex', 'flex: 1 0 0');
            const title = document.createElement('p');
            title.textContent = result.title;
            const url = document.createElement('a');
            url.setAttribute('href', `${url}`);
            url.textContent = 'URL';
            url.style.color = 'darkslategray';
            card.appendChild(title);
            card.appendChild(url);
            resultsDiv.appendChild(card);
            console.log('Hola pendejo')
        }
        count++;


    });

    if(count === 0 )
    {

        const message = document.createElement('h3');
        message.textContent = 'No results';
        resultsDiv.appendChild(message);

    }

    const mainElement = document.querySelector('main');
    mainElement.appendChild(resultsDiv);

}