/*
This method converts the list of files to form data allows for easy/fast transfer of files to servlet. This is necessary
for the servlet to process each of the files.
 */
export const convertToFormData = (files) => {
    const formData = new FormData();

    // Extract directory name from the first file path
    const fullPath = files[0].webkitRelativePath;
    const directoryName = fullPath.split('/')[0];
    formData.append('directoryName', directoryName);

    // Append each file to form data object
    for (const file of files)
    {
        formData.append('folderInput', file,  file.webkitRelativePath);
    }

    return formData;
}