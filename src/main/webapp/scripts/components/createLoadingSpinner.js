// Creates the HTML for a "loading" spinner that is displayed while the uploaded directory is being indexed
export const createLoadingSpinner = () =>
{
    const loadingDiv = document.createElement('div');
    loadingDiv.classList.add('flex-column');
    loadingDiv.id = "loading-spinner";
    loadingDiv.innerHTML = `
            <span class="loading-spinner"></span>
            <p>Indexing...</p>
            `
    return loadingDiv;

}