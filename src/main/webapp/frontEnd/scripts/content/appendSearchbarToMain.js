import { verifyBooleanQuery } from "../utils/verifyBooleanQuery.js";

export const appendSearchbarToMain = (buttonId) => {

    console.log(buttonId);
    const searchDiv = document.createElement('div');
    searchDiv.classList.add('search-div');
    searchDiv.innerHTML = `
    <div class="box">
        <form id ="search-form" name="search">
            <div class="flex-column">
                <input id="query" type="text" class="searchbox" name="txt">
                <button type="submit"> Query! </button>   
            </div>
        </form>
    </div>

    `;
    
    const mainElement = document.querySelector('main');
    mainElement.appendChild(searchDiv);
    const form = document.querySelector('#search-form');
    form.addEventListener('submit', (event) => {
        verifyBooleanQuery(event, buttonId);
    });
}