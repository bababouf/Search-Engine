import {hideMainElements} from "../utils/hideMainElements.js";
import {createBackButton} from "./createBackButton.js";


export const displayUploadDirectoryPage = () => {
    hideMainElements();
    const mainElement = document.querySelector('main');
    const uploadDirectory = displayUploadDirectory();
    mainElement.appendChild(uploadDirectory);
    const backButton = createBackButton();
    mainElement.appendChild(backButton);

}

const displayUploadDirectory = () => {
    const uploadDirectory = document.createElement('div');
    uploadDirectory.classList.add('upload-corpus-div');

    uploadDirectory.innerHTML = `
    <form id="folderForm">
        <label for="folderPath">Choose a corpus directory: </label>
        <input type="file" id="folderInput" name="folderInput" webkitdirectory directory required>
        <button type="submit">Submit</button>
    </form>
    `;

    return uploadDirectory;
}
