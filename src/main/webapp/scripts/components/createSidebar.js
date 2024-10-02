// Creates HTML for the sidebar in the documenation page
export const createSidebar = () =>
{
    const sidebar = document.createElement('aside');
    sidebar.classList.add('sidebar');

    sidebar.innerHTML = `
        <ul class="sidebar__list">
            <li><a href="#documentation__intro-section" class="sidebar__link">Introduction</a></li>
            <li><a href="#documentation__tech-stack-section" class="sidebar__link">Technology Stack</a></li>
            <li><a href="#documentation__indexing-section" class="sidebar__link">Indexing</a></li>
            <li><a href="#documentation__storage-section" class="sidebar__link">Storage</a></li>
            <li><a href="#documentation__boolean-section" class="sidebar__link">Boolean Retrieval</a></li>
            <li><a href="#documentation__ranked-section" class="sidebar__link">Ranked Retrieval</a></li>
            <li><a href="https://github.com/bababouf/Search-Engine/tree/main" target="_blank" class="sidebar__link">GitHub Repo</a></li>
        </ul>
    `;

    return sidebar;
}