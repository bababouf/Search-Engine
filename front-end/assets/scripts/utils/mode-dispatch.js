import {hideMainElements} from './toggle-main-elements';
import {displaySearchbar} from '../display/searchbar';
import {displayBooleanRetrievalMode} from '../display/boolean-retrieval-mode';
import {displayRankedRetrievalMode} from '../display/ranked-retrieval-mode';
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

