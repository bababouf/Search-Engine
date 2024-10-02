// Function to check if all JSON files have the same set of keys
export const validateJsonKeys = async (files) => {
    try {
        // Read and parse all files, returning their keys
        const filePromises = Array.from(files).map(file => {
            return new Promise((resolve, reject) => {
                const reader = new FileReader();
                reader.onload = () => {
                    try {
                        const jsonData = JSON.parse(reader.result);
                        resolve(Object.keys(jsonData)); // Return the keys
                    } catch (error) {
                        reject(`Error parsing JSON file: ${file.name}`);
                    }
                };
                reader.onerror = () => reject(`Error reading file: ${file.name}`);
                reader.readAsText(file);
            });
        });

        const results = await Promise.all(filePromises);

        // Use the first file's keys as a reference
        const referenceKeys = results[0];

        // Check if all JSON files have the same keys
        const allKeysSame = results.every(keys => JSON.stringify(keys) === JSON.stringify(referenceKeys));

        // Return true if keys match, else false
        return allKeysSame ? referenceKeys : null; // Return keys if valid
    } catch (error) {
        console.error(error);
        return null;
    }
};
// Function to validate the uploaded directory based on file type, size, and JSON keys
export const verifyUploadedDirectory = async (files) => {
    const arrayOfFiles = Array.from(files);

    // Set maximum size for directory and maximum number of directories allowed
    const maxSize = 50 * 1024 * 1024; // 50 MB
    const maxDirectories = 3;

    // Check if all files are either .json or .txt
    const allJsonFiles = arrayOfFiles.every(file => file.name.endsWith(".json"));
    const allTxtFiles = arrayOfFiles.every(file => file.name.endsWith(".txt"));
    const validFileTypes = allJsonFiles || allTxtFiles;

    /*
    // Error: Check if the user already has max directories uploaded
    if (userDirectories.length >= maxDirectories) {
        return { valid: false, message: `Maximum ${maxDirectories} directories reached. Please delete one to upload.` };
    }
*/
    // Error: Invalid file types
    if (!validFileTypes) {
        return { valid: false, message: "Directory contains invalid file types. Only .json or .txt files are allowed." };
    }

    // Error: No files uploaded
    if (arrayOfFiles.length === 0) {
        return { valid: false, message: "Please choose a directory to upload." };
    }

    // Calculate total directory size
    const totalSize = arrayOfFiles.reduce((acc, file) => acc + file.size, 0);

    // Error: Directory size exceeds limit
    if (totalSize > maxSize) {
        return { valid: false, message: `Directory exceeds ${maxSize / (1024 * 1024)} MB limit.` };
    }

    // If files are JSON, validate their keys
    if (allJsonFiles) {
        const keys = await validateJsonKeys(arrayOfFiles);
        if (!keys) {
            return { valid: false, message: "JSON files must all have the same set of keys." };
        } else {
            return { valid: true, message: "Valid JSON files", keys: keys }; // Return valid and the keys
        }
    }

    // Return valid for TXT files without key checks
    return { valid: true, message: "Valid TXT files", keys: null };
};