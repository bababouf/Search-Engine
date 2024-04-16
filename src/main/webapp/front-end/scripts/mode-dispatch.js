import {hideMainElements} from './hide-main-elements.js';
import {displaySearchbar} from './searchbar.js';
import {displayBooleanRetrievalMode} from './boolean-retrieval-mode.js';
import {displayRankedRetrievalMode} from './ranked-retrieval-mode.js';
import {displayBackButton} from "./back-button.js";


export const modeDispatch = (event) => {

   
    hideMainElements();
    displaySearchbar(event.currentTarget.id);
    
    if(event.currentTarget.id === 'boolean-button')
    {
        displayBooleanRetrievalMode();
        displayBackButton();
        
        // verify user query correct form
        // send to back end
    }
    else if(event.currentTarget.id === 'ranked-button')
    {
        displayRankedRetrievalMode();
        displayBackButton();
        // very user query correct form
        // send to back end
    }
   

    
}

