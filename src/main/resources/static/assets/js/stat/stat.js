document.getElementById('searchForm').addEventListener('submit', async function(event) {
    event.preventDefault();
    const startDate = document.getElementById('hidden_start_date').value;
    const endDate = document.getElementById('hidden_end_date').value;
    const data = await fetchConsultationData(startDate, endDate);
    renderTable(data, 1, 10);
    setupPagination(data, 10);
});

async function fetchConsultationData(startDate, endDate) {
    try {
        const response = await fetch(`/api/searchCons?start_date=${startDate}&end_date=${endDate}`, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        });
        if (!response.ok) throw new Error('Failed to fetch data');
        return await response.json();
    } catch (error) {
        console.error('Error loading the data:', error);
        displayErrorMessage('데이터를 불러오는 중 오류가 발생했습니다.');
        return []; // Return an empty array to handle in downstream functions
    }
}

function renderTable(data, currentPage, pageSize) {
    const tableBody = document.getElementById('statTable').querySelector('tbody');
    tableBody.innerHTML = '';
    const start = (currentPage - 1) * pageSize;
    const end = Math.min(start + pageSize, data.length);
    const paginatedItems = data.slice(start, end);

    if (paginatedItems.length === 0) {
        displayErrorMessage("조회된 데이터가 없습니다");
        return;
    }

    paginatedItems.forEach(item => {
        const row = tableBody.insertRow();
        row.insertCell(0).textContent = item.indate;
        row.insertCell(1).textContent = item.csname;
        row.insertCell(2).textContent = item.cscountst;
    });
}

function setupPagination(data, pageSize) {
    const pageCount = Math.ceil(data.length / pageSize);
    const paginationContainer = document.getElementById('pagination-container');
    paginationContainer.innerHTML = '';

    for (let i = 1; i <= pageCount; i++) {
        const pageItem = document.createElement('li');
        pageItem.className = 'page-item ' + (i === 1 ? 'active' : '');
        const pageLink = document.createElement('a');
        pageLink.className = 'page-link';
        pageLink.href = '#';
        pageLink.innerText = i;
        pageLink.addEventListener('click', (event) => {
            event.preventDefault();
            renderTable(data, i, pageSize);
            // Update active class on pagination
            document.querySelectorAll('#pagination-container .page-item').forEach(p => p.classList.remove('active'));
            pageItem.classList.add('active');
        });
        pageItem.appendChild(pageLink);
        paginationContainer.appendChild(pageItem);
    }
}

function displayErrorMessage(message) {
    const tableBody = document.getElementById('statTable').querySelector('tbody');
    tableBody.innerHTML = '<tr><td colspan="3" class="text-center">' + message + '</td></tr>';
}

var KTAppEcommerceReportCustomerOrders = function() {
    var table, dataTable;
    var hiddenStartDate, hiddenEndDate;
    return {
        init: function() {
            table = document.querySelector("#statTable");
            dataTable = $(table).DataTable({
                info: false,
                order: [],
                pageLength: 10
            });

            var dateRangePicker = $("#kt_ecommerce_report_customer_orders_daterangepicker");
            hiddenStartDate = document.getElementById('hidden_start_date');
            hiddenEndDate = document.getElementById('hidden_end_date');

            // Today's date setup
            var today = moment();
            hiddenStartDate.value = today.format('YYYYMMDD');
            hiddenEndDate.value = today.format('YYYYMMDD');

            dateRangePicker.daterangepicker({
                startDate: moment(),
                endDate: moment(),
                ranges: {
                    '오늘': [moment(), moment()],
                    '어제': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                    '지난 7일': [moment().subtract(6, 'days'), moment()],
                    '지난 30일': [moment().subtract(29, 'days'), moment()],
                    '이번달': [moment().startOf('month'), moment().endOf('month')],
                    '지난달': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
                },
                locale: {
                    format: 'YYYY/MM/DD'
                }
            }, function(start, end) {
                hiddenStartDate.value = start.format('YYYYMMDD');
                hiddenEndDate.value = end.format('YYYYMMDD');

            });

            // Set the initial dates and trigger data update
            dateRangePicker.data('daterangepicker').setStartDate(moment());
            dateRangePicker.data('daterangepicker').setEndDate(moment());
            dateRangePicker.data('daterangepicker').clickApply();
        }
    }
}();

KTUtil.onDOMContentLoaded(function() {
    KTAppEcommerceReportCustomerOrders.init();
});
