export const displayUploadDirectory = () => {
    const uploadCorpusDiv = document.createElement('div');
    uploadCorpusDiv.classList.add('upload-corpus-div');

    uploadCorpusDiv.innerHTML = `
    <form id="folderForm">
        <label for="folderPath">Choose a corpus directory: </label>
        <input type="file" id="folderInput" name="folderInput" webkitdirectory directory required>
        <button type="submit">Submit</button>
    </form>
    `;
    uploadCorpusDiv.style.display = 'flex';
    uploadCorpusDiv.style.justifyContent = 'center';
    uploadCorpusDiv.style.gap = '1rem';
    const mainElement = document.querySelector('main');
    mainElement.appendChild(uploadCorpusDiv);

    
}
