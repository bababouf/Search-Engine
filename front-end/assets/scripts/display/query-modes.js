import {modeDispatch} from "../utils/mode-dispatch";

export const displayQueryModes = (event) => {
    const modes = document.createElement('div');
    modes.classList.add('mode-div');

    modes.innerHTML = `
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
    modes.style.display = 'flex';
    modes.style.justifyContent = 'center';
    modes.style.gap = '1rem';
    const mainElement = document.querySelector('main');
    mainElement.appendChild(modes);
   
    const modeButtons = document.querySelectorAll(".mode-button");
    modeButtons.forEach(button => {
        button.addEventListener("click", modeDispatch);
    });
}