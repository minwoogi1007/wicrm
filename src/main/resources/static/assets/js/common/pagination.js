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
        container.innerHTML = '';
        items.forEach(item => {
            const itemElement = document.createElement('div');
            itemElement.textContent = item.name; // 아이템의 'name' 속성 가정
            container.appendChild(itemElement);
        });
    }

    setupPagination() {
        const pageCount = Math.ceil(this.data.length / this.pageSize);
        const paginationContainer = document.getElementById(this.paginationContainerId);
        paginationContainer.innerHTML = '';
        const ul = document.createElement('ul');
        ul.className = 'pagination';

        const addPageItem = (pageNum, text, isActive = false) => {
            const li = document.createElement('li');
            li.className = `page-item ${isActive ? 'active' : ''}`;
            li.innerHTML = `<a class="page-link" href="#">${text}</a>`;
            li.addEventListener('click', (e) => {
                e.preventDefault();
                this.currentPage = pageNum;
                this.paginate();
            });
            ul.appendChild(li);
        };

        addPageItem(Math.max(1, this.currentPage - 1), '<i class="previous"></i>', false);
        for (let i = 1; i <= pageCount; i++) {
            addPageItem(i, i, this.currentPage === i);
        }
        addPageItem(Math.min(pageCount, this.currentPage + 1), '<i class="next"></i>', false);

        paginationContainer.appendChild(ul);
    }

    paginate() {
        const startIndex = (this.currentPage - 1) * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        const paginatedItems = this.data.slice(startIndex, endIndex);
        this.displayData(paginatedItems);
    }

    init() {
        this.paginate();
        this.setupPagination();
    }
}
