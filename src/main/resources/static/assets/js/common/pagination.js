// pagination.js
export class Pagination {
    constructor(data, pageSize, containerId, paginationContainerId) {
        this.data = data;
        this.pageSize = pageSize;
        this.containerId = containerId;
        this.paginationContainerId = paginationContainerId;
        this.currentPage = 1;
    }

    displayData(items) {
        const container = document.getElementById(this.containerId);
        if (!container) {
            console.error("Container not found: ", this.containerId);
            return;
        }
        container.innerHTML = '';
        items.forEach(item => {
            const itemElement = document.createElement('div');
            itemElement.textContent = item.name || "No name provided"; // Adjust based on actual item property
            container.appendChild(itemElement);
        });
    }

    setupPagination() {
        const pageCount = Math.ceil(this.data.length / this.pageSize);
        const paginationContainer = document.getElementById(this.paginationContainerId);
        if (!paginationContainer) {
            console.error("Pagination container not found: ", this.paginationContainerId);
            return;
        }
        paginationContainer.innerHTML = '';
        const ul = document.createElement('ul');
        ul.className = 'pagination';

        for (let i = 1; i <= pageCount; i++) {
            const pageItem = document.createElement('li');
            pageItem.className = 'page-item ' + (this.currentPage === i ? 'active' : '');
            const pageLink = document.createElement('a');
            pageLink.className = 'page-link';
            pageLink.href = '#';
            pageLink.innerText = i;
            pageLink.addEventListener('click', (event) => {
                event.preventDefault();
                this.currentPage = i;
                this.paginate();
            });
            pageItem.appendChild(pageLink);
            ul.appendChild(pageItem);
        }

        paginationContainer.appendChild(ul);
    }

    paginate() {
        const startIndex = (this.currentPage - 1) * this.pageSize;
        const endIndex = Math.min(startIndex + this.pageSize, this.data.length);
        const paginatedItems = this.data.slice(startIndex, endIndex);
        this.displayData(paginatedItems);
    }

    init() {
        this.paginate();
        this.setupPagination();
    }
}
