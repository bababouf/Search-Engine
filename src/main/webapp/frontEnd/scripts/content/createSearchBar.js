
export const createSearchBar = () => {

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

    return searchDiv;
}

