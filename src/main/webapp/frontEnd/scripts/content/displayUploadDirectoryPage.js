import {removeMainElements} from "../utils/removeMainElements.js";
import {createBackButton} from "./createBackButton.js";


export const displayUploadDirectoryPage = () => {
    removeMainElements();
    const mainElement = document.querySelector('main');
    const uploadDirectory = displayUploadDirectory();
    const backButton = createBackButton();

    mainElement.appendChild(uploadDirectory);
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
