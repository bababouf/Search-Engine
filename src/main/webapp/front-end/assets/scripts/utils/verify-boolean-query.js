export const verifyBooleanQuery = (event) => {
    event.preventDefault();
    const value = document.querySelector('#query');
    console.log(value.value);

    fetch('/search', {
        method: 'POST',
        body: JSON.stringify({query: value.value})
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