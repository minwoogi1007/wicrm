document.getElementById('searchForm').addEventListener('submit', function(event) {
    event.preventDefault();
    updateDataTable();
});

function updateDataTable() {
    const startDate = document.getElementById('hidden_start_date').value;
    const endDate = document.getElementById('hidden_end_date').value;

    fetch(`/api/searchCons?start_date=${startDate}&end_date=${endDate}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            // Include CSRF token as necessary
        }
    })
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById('kt_ecommerce_report_customer_orders_table').getElementsByTagName('tbody')[0];
            tableBody.innerHTML = '';
            if (data.length === 0) {
                const row = tableBody.insertRow();
                const cell = row.insertCell(0);
                cell.textContent = "조회된 데이터가 없습니다";
                cell.colSpan = 3;
                cell.style.textAlign = 'center';
            } else {
                data.forEach(item => {
                    const row = tableBody.insertRow();
                    const cellDate = row.insertCell(0);
                    const cellType = row.insertCell(1);
                    const cellTotal = row.insertCell(2);

                    cellDate.textContent = item.indate;
                    cellType.textContent = item.csname;
                    cellTotal.textContent = item.cscountst;
                });
            }
        })
        .catch(error => {
            console.error('Error loading the data:', error);
            const row = tableBody.insertRow();
            const cell = row.insertCell(0);
            cell.textContent = "데이터를 불러오는 중 오류가 발생했습니다";
            cell.colSpan = 3;
            cell.style.textAlign = 'center';
        });
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
