
export const downloadCorpusAtServer = () => {
    contactServlet();
}

/*
The method sends a GET request to the /search/booleansearch endpoint. When the servlet receives this request, it will
take action to setup the corpus and download the index representing the corpus the user has selected to query.
 */
const contactServlet = () => {

    fetch(`/search/booleansearch`, {
        method: 'GET'
    })
        .then(response =>
        {
            if (!response.ok)
            {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(responseText =>
        {
            console.log("Successfully contacted servlet")
        })
        .catch(error =>
        {
            console.error('There was a problem with the fetch operation:', error);
        });
}