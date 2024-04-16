import {modeDispatch} from "./mode-dispatch.js";

export const displayQueryModes = (event) => {
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
   
    const mainElement = document.querySelector('main');
    mainElement.appendChild(modesDiv);
    
    const modeButtons = document.querySelectorAll(".mode-button");
    modeButtons.forEach(button => {
        button.addEventListener("click", modeDispatch);
    });
}