/*
Creates HTML for a search bar (and submit button). This search bar is used on the boolean query page and the ranked
query page.
 */
export const createSearchBar = () =>
{

    const searchDiv = document.createElement('div');
    searchDiv.classList.add('search-container');

    searchDiv.innerHTML = `
    <form id ="search-form" name="search">
            <input id="query" type="text" class="search-box" name="txt" autocomplete="off">  
    </form>
    
    `;

    return searchDiv;
}

