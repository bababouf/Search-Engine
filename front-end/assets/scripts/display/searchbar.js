import { verifyBooleanQuery } from "../utils/verify-boolean-query";

export const displaySearchbar = () => {

    const searchDiv = document.createElement('div');
    searchDiv.classList.add('search-div');
    searchDiv.innerHTML = `
    <div class="box">
        <form id ="search-form" name="search">
            <div id="input-container">
                <input id="query" type="text" class="input" name="txt">
                <button id ="submit-query" type="submit"> Query! </button>   
            </div>
        </form>
    </div>

    `;
    
    /*
    searchDiv.style.display = 'flex';
    searchDiv.style.justifyContent = 'center';
    searchDiv.style.padding = '2rem';
    */
    const mainElement = document.querySelector('main');
    const searchBar = document.querySelector('#searchbar');
    mainElement.appendChild(searchDiv);
    const inputcont = document.querySelector('#input-container');
    inputcont.style.display = 'flex';
    inputcont.style.flexDirection = 'column';
    inputcont.style.alignItems = 'center';

    const submitQuery = document.querySelector('#submit-query');
    submitQuery.style.margin = '15px';
    const form = document.querySelector('#search-form');
    form.addEventListener('submit', verifyBooleanQuery);
   
}