import {queryModeDispatch} from "../utils/queryModeDispatch.js";
import {hideMainElements} from "../utils/hideMainElements.js";
import {createBackButton} from "./createBackButton.js";


export const displayQueryModesPage = (event) => {

    hideMainElements();
    const mainElement = document.querySelector('main');
    const modesDiv = displayQueryModes();
    mainElement.appendChild(modesDiv);

    const modeButtons = document.querySelectorAll(".mode-button");
    modeButtons.forEach(button => {
        button.addEventListener("click", queryModeDispatch);
    });

    const backButton = createBackButton();
    mainElement.appendChild(backButton);


}

const displayQueryModes = () => {
    const modesDiv = document.createElement('div');
    modesDiv.classList.add('card-container');

    modesDiv.innerHTML = `
    <div class="card">
        <h3> Boolean Retrieval Mode </h3>
        <p> Configures the program to handle boolean queries. </p>
        <button class = "mode-button" id="boolean-button"> Go! </button>  
    </div>
    <div class="card">
        <h3> Ranked Retrieval Mode </h3>
        <p> Configures the program to handle ranked requests. </p>
        <button class = "mode-button" id="ranked-button"> Go! </button>  
    </div>
    `;

    return modesDiv;
}