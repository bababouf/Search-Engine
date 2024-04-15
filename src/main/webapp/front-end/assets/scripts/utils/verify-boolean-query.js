export const verifyBooleanQuery = (event, buttonId) => {
    event.preventDefault();
    console.log(buttonId);
    let endpoint = '';
    if(buttonId === 'boolean-button')
    {
        endpoint = '/booleansearch';
    }
    else if(buttonId === 'ranked-button')
    {
        endpoint = '/rankedsearch';
    }
    const value = document.querySelector('#query');
    console.log(value.value);

    fetch(`/search${endpoint}`, {
        method: 'POST',
        body: JSON.stringify(
            {query: value.value, mode: buttonId})
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(responseText => {
            // Handle the response from the servlet
            console.log('Response from servlet:', responseText);
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}