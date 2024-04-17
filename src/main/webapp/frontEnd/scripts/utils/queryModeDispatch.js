import {hideMainElements} from './hideMainElements.js';
import {displayBooleanSearchPage} from '../content/displayBooleanSearchPage.js';
import {displayRankedSearchPage} from '../content/displayRankedSearchPage.js';


export const queryModeDispatch = (event) => {

    hideMainElements();

    const buttonId = event.currentTarget.id;
    if(buttonId === 'boolean-button')
    {
        displayBooleanSearchPage(buttonId);
    }
    else if(event.currentTarget.id === 'ranked-button')
    {
        displayRankedSearchPage(buttonId);
    }
   

    
}

