// Displays the site header
export const displaySiteHeader = () =>
{
    const header = createHeader();
    const body = document.querySelector('body');
    body.insertBefore(header, body.firstChild);
}

// Creates the HTML for the site header
const createHeader = () =>
{
    const header = document.createElement('header');
    header.classList.add('site__hero-container');
    const headerDiv = document.createElement('div');
    headerDiv.classList.add('site__title-and-anchors');

    headerDiv.innerHTML = `
    <h1 class="site__h1"> Search Engine </h1>
    <ul class="site__hero-anchors">
    </ul>
    
    `;

    header.appendChild(headerDiv);
    return header;
}