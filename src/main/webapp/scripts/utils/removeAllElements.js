// Remove all elements from the body element
export const removeAllElements = () =>
{
    const body = document.querySelector('body');
    body.innerHTML = '';
}