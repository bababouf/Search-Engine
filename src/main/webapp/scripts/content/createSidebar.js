export const createSidebar = () => {
    const sidebarDiv = document.createElement('div');
    sidebarDiv.classList.add('sidebar');
    sidebarDiv.innerHTML = `
        <a href="#documentation__intro-section" >Introduction</a>
        <a href="#documentation__tech-stack-section" >Technology Stack</a>
        <a href="#documentation__indexing-section" >Indexing Details</a>
        <a href="#documentation__boolean-section" >Boolean Retrieval Details</a>
        <a href="#documentation__ranked-section" >Ranked Retrieval Details</a>
    `;

    return sidebarDiv;
}