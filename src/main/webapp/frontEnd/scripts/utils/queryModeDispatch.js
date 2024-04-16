import {hideMainElements} from './hideMainElements.js';
import {appendSearchbarToMain} from '../content/appendSearchbarToMain.js';
import {displayBooleanRetrievalMode} from '../content/displayBooleanRetrievalMode.js';
import {displayRankedRetrievalMode} from '../content/displayRankedRetrievalMode.js';
import {displayBackButton} from "../content/appendBackButtonToMain.js";


export const queryModeDispatch = (event) => {

   
    hideMainElements();
    appendSearchbarToMain(event.currentTarget.id);
    
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

