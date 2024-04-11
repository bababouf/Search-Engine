import {hideMainElements} from './hide-main-elements';
import {displaySearchbar} from '../content/searchbar';
import {displayBooleanRetrievalMode} from '../content/boolean-retrieval-mode';
import {displayRankedRetrievalMode} from '../content/ranked-retrieval-mode';
import { verifyBooleanQuery } from './verify-boolean-query';

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

