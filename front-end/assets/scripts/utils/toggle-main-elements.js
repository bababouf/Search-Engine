export const hideMainElements = () => {
    const mainElement = document.querySelector('main');
    const children = mainElement.children;

    for (let i = 0; i < children.length; i++) {
        children[i].style.display = 'none';
    }
}

export const displayMainElements = () => {


   

}