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
                $(document).trigger('dateRangeUpdated'); // 날짜가 변경되면 이벤트 트리거
            });

            // 내보내기 버튼 설정
            const reportTitle = "통계";
            new $.fn.dataTable.Buttons(table, {
                buttons: [
                    {extend: "copyHtml5", title: reportTitle},
                    {extend: "excelHtml5", title: reportTitle},
                    {extend: "csvHtml5", title: reportTitle},
                    {extend: "pdfHtml5", title: reportTitle}
                ]
            }).container().appendTo($("#kt_ecommerce_report_customer_orders_export"));

            document.querySelectorAll("#kt_ecommerce_report_customer_orders_export_menu [data-kt-ecommerce-export]").forEach((button) => {
                button.addEventListener("click", (event) => {
                    event.preventDefault();
                    const format = event.target.getAttribute("data-kt-ecommerce-export");
                    document.querySelector(".dt-buttons .buttons-" + format).click();
                });
            });
        }
    }
}();

KTUtil.onDOMContentLoaded(function() {
    KTAppEcommerceReportCustomerOrders.init();
});
