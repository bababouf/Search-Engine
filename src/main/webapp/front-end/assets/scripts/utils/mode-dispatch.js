import {hideMainElements} from './hide-main-elements.js';
import {displaySearchbar} from '../content/searchbar.js';
import {displayBooleanRetrievalMode} from '../content/boolean-retrieval-mode.js';
import {displayRankedRetrievalMode} from '../content/ranked-retrieval-mode.js';
import {displayBackButton} from "../content/back-button.js";


export const modeDispatch = (event) => {

   
    hideMainElements();
    displaySearchbar();
    
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
