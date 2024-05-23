let initialContent = '';
export const setInitialContent = (content) => {
    initialContent = content;
}

export const getInitialContent = () => {
    return initialContent;
}

export const captureMainContent = () => {
    const mainElement = document.querySelector('main');
    return mainElement.innerHTML;
};