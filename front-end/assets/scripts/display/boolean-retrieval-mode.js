import { displayBackButton } from "./back-button";

export const displayBooleanRetrievalMode = () => {

    const instructions = document.createElement('div');
    instructions.innerHTML = `
        <h2>Instructions</h2>
        <p> The program accepts boolean queries that are in disjunctive normal form (DNF), also described as an OR of ANDS. 
        In addition, phrase queries can be entered by enclosing the query in double quotes. Below examples of acceptable form are shown.</p>
    `;
    
    instructions.style.margin = '0rem 10rem';
    instructions.style.textAlign = 'center';
    const mainElement = document.querySelector('main');
    mainElement.insertBefore(instructions, mainElement.firstChild);
    const instructionsDiv = document.createElement('div');
    instructionsDiv.classList.add('instructions-div');
    instructionsDiv.innerHTML = `
    <div class="card">
        <h3> AND Query </h3>
        <p> Returns documents containing each of the query terms being AND'd. </p>
        <p> Example: dogs cats birds </p>
  
    </div>
    <div class="card">
        <h3> OR Query </h3>
        <p> Returns documents containing at least one of the terms being OR'd. </p>
        <p> Example: dogs + cats + birds </p>
         
    </div>
    <div class="card">
        <h3> OR of ANDs </h3>
        <p> Terms can be AND'd together, and these groups can then be OR'd. </p>
        <p> Example: dogs cats + birds walruses
         
    </div>
    <div class="card">
        <h3> Phrase Query </h3>
        <p> Returns documents that contain the queried phrase. </p>
        <p> Example: "fires in yosemite" </p>
        
    </div>
    `;
    
    instructionsDiv.style.display = 'flex';
    instructionsDiv.style.justifyContent = 'center';
   
    mainElement.appendChild(instructionsDiv);
    displayBackButton();
}