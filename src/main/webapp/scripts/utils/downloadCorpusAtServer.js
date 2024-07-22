
export const downloadCorpusAtServer = () => {
    contactServlet();
}

const contactServlet = () => {


    fetch(`/search/booleansearch`, {
        method: 'GET'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(responseText => {
            console.log("Successfully contacted servlet")
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}