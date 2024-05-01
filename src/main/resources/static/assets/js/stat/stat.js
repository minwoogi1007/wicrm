document.getElementById('searchForm').addEventListener('submit', function(event) {
    event.preventDefault();
    updateDataTable();
});

function updateDataTable() {
    const pageSize = 10; // Set the number of items per page
    let currentPage = 1; // Current selected page

    const startDate = document.getElementById('hidden_start_date').value;
    const endDate = document.getElementById('hidden_end_date').value;

    fetch(`/api/searchCons?start_date=${startDate}&end_date=${endDate}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => response.json())
        .then(data => {
            renderTable(data, currentPage, pageSize);
            setupPagination(data, pageSize);
        })
        .catch(error => {
            console.error('Error loading the data:', error);
            displayErrorMessage();
        });
}

function renderTable(data, currentPage, pageSize) {
    const tableBody = document.getElementById('kt_ecommerce_report_customer_orders_table').getElementsByTagName('tbody')[0];
    tableBody.innerHTML = '';

    const start = (currentPage - 1) * pageSize;
    const end = start + pageSize;
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
        const button = document.createElement('button');
        button.textContent = i;
        button.onclick = () => renderTable(data, i, pageSize);
        paginationContainer.appendChild(button);
    }
}

function displayErrorMessage(message = "데이터를 불러오는 중 오류가 발생했습니다") {
    const tableBody = document.getElementById('kt_ecommerce_report_customer_orders_table').getElementsByTagName('tbody')[0];
    const row = tableBody.insertRow();
    const cell = row.insertCell(0);
    cell.textContent = message;
    cell.colSpan = 3;
    cell.style.textAlign = 'center';
}


var KTAppEcommerceReportCustomerOrders = function() {
    var table, dataTable;
    var hiddenStartDate, hiddenEndDate;
    return {
        init: function() {
            table = document.querySelector("#kt_ecommerce_report_customer_orders_table");
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
                updateDataTable();
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
