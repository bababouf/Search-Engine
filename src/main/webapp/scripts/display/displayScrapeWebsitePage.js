import {removeMainElements} from "../utils/removeMainElements.js";
import {createBackToHomeButton} from "../components/createBackToHomeButton.js";
import {scrapeWebsite} from "../utils/contactServlet.js";
import {createLoadingSpinner} from "../components/createLoadingSpinner.js";

// Displays scraping form page
export const displayScrapeWebsitePage = () =>
{
    removeMainElements();
    createScrapeWebpageForm();
    attachListeners();
}

// Creates HTML for the scraping page
const createScrapeWebpageForm = () =>
{

    // Sets the header instructions paragraph text
    setHeaderInstructions();

    const mainElement = document.querySelector('main');
    mainElement.innerHTML = `
            
        <section class="card bg-gradient grid-config">
            <form id="scrape-form" class="scrape-form">
                <!-- Base URL input field -->
                <div class="form-group">
                    <label for="base-url" class="form-label">Base URL</label>
                    <input type="url" id="base-url" name="base-url" class="form-input" placeholder="https://example.com" required>
                </div>
        
                <!-- Depth selection dropdown -->
                <div class="form-group">
                    <label for="scraping-depth" class="form-label">Scraping Depth</label>
                    <select id="scraping-depth" name="scraping-depth" class="form-select" required>
                        <option value="1">1</option>
                        <option value="2">2</option>
                        <option value="3">3</option>
                    </select>
                </div>
        
                <!-- Submit button -->
                <div class="form-group">
                    <button id="scrape-button" class="site__button" type="submit">Start Scraping</button>
                </div>
        
                <!-- Error message -->
                <p id="error-message"></p>
            </form>
        </section>
        `;

    const backButton = createBackToHomeButton();
    mainElement.appendChild(backButton);

}

// Set the header paragraph instructions text
const setHeaderInstructions = () =>
{
    const headerInstructions = document.querySelector('.site-header__instructions');
    headerInstructions.style.display = 'block';
    headerInstructions.textContent = 'Enter the base URL for the webpage you would like to have scraped (i.e https://wikipedia.org). Depth indicates how thorough the scrape is; a depth of 1 collects all of the links on the homepage of the site, a depth of 2 collects all links that are 2 links from the homepage, etc. ';
}

// Attach event listener to the form submit button
const attachListeners = () =>
{
    const scrapeForm = document.querySelector('#scrape-form');

    // Attach a listener to handle form submission
    scrapeForm.addEventListener('submit', (event) =>
    {
        event.preventDefault(); // Prevent the form from submitting normally

        // Get the value of the Base URL input
        const baseURL = document.querySelector('#base-url').value;

        // Get the selected scraping depth value
        const scrapingDepth = document.querySelector('#scraping-depth').value;

        // Remove elements, create and append loading spinner (and text)
        removeMainElements();
        const loadingSpinner = createLoadingSpinner();
        const mainElement = document.querySelector('main');
        mainElement.appendChild(loadingSpinner);

        const spinnerText = document.querySelector('#loading-spinner-text');
        spinnerText.textContent = "Scraping site. This might take a while, as all webpages at the specified depth will be scraped. ";

        // Contact servlet with a POST request containing baseURL and scrapingDepth
        scrapeWebsite(baseURL, scrapingDepth);
    });
}