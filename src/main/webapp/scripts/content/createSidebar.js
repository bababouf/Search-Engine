export const createSidebar = () => {
    const sidebarDiv = document.createElement('div');
    sidebarDiv.classList.add('sidebar');
    sidebarDiv.innerHTML = `
        <a href="#sup"">Introduction</a>
        <a href="#documentation__tech-stack-section">Technology Stack</a>
        <a href="#documentation__indexing-section">Indexing Details</a>
        <a href="#documentation__querying-section">Information Retrieval Details</a>
    `;

    return sidebarDiv;
}