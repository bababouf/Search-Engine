/*
Creates HTML for a search bar (and submit button). This search bar is used on the boolean query page and the ranked
query page.
 */
export const createSearchBar = () => {

    const searchDiv = document.createElement('div');
    searchDiv.classList.add('search-div');

    searchDiv.innerHTML = `
    <div class="box">
        <form id ="search-form" name="search">
            <div class="flex-column">
                <input id="query" type="text" class="search-box" name="txt" autocomplete="off">
                <button type="submit"> Query! </button>   
            </div>
        </form>
    </div>

    `;

    return searchDiv;
}

