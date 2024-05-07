var KTAppEcommerceReportCustomerOrders = function() {
    var table, dataTable;
    var hiddenStartDate, hiddenEndDate;
    var dateRangePicker;

    return {
        init: function() {
            table = document.querySelector("#kt_ecommerce_report_customer_orders_table");
            dataTable = $(table).DataTable({
                info: false,
                order: [],
                pageLength: 10
            });

            dateRangePicker = $("#kt_ecommerce_report_customer_orders_daterangepicker");
            hiddenStartDate = document.getElementById('hidden_start_date');
            hiddenEndDate = document.getElementById('hidden_end_date');


            // Today's date setup
            // Check if hidden inputs have values and use them, otherwise set to today's date
            var today = moment().format('YYYYMMDD'); // Format today's date as 'YYYYMMDD'
            var defaultStart = hiddenStartDate.value && moment(hiddenStartDate.value, 'YYYYMMDD', true).isValid()
                ? moment(hiddenStartDate.value, 'YYYYMMDD')
                : moment();
            var defaultEnd = hiddenEndDate.value && moment(hiddenEndDate.value, 'YYYYMMDD', true).isValid()
                ? moment(hiddenEndDate.value, 'YYYYMMDD')
                : moment();


            dateRangePicker.daterangepicker({
                startDate: defaultStart,
                endDate: defaultEnd,
                ranges: {
                    '오늘': [moment(), moment()],
                    '어제': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                    '지난 7일': [moment().subtract(6, 'days'), moment()],
                    '지난 30일': [moment().subtract(29, 'days'), moment()],
                    '이번달': [moment().startOf('month'), moment().endOf('month')],
                    '지난달': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
                },
                locale: {
                    format: 'YYYY/MM/DD',
                    monthNames: ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"]
                }
            }, function(start, end) {

                hiddenStartDate.value = start.format('YYYYMMDD');
                hiddenEndDate.value = end.format('YYYYMMDD');
                dateRangePicker.val(start.format('YYYY/MM/DD') + ' - ' + end.format('YYYY/MM/DD'));
                $(document).trigger('dateRangeUpdated'); // Trigger event when dates change


            });
            dateRangePicker.val(defaultStart.format('YYYY/MM/DD') + ' - ' + defaultEnd.format('YYYY/MM/DD'));


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
