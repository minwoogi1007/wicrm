"use strict";

var KTAppEcommerceReportCustomerOrders = function() {
    var table, dataTable;
    return {
        init: function() {
            table = document.querySelector("#kt_ecommerce_report_customer_orders_table");
            if (table) {
                // 날짜 데이터 초기화 및 설정
                table.querySelectorAll("tbody tr").forEach((row) => {
                    const cells = row.querySelectorAll("td"),
                        dateValue = moment(cells[3].innerHTML, "YYYY MMM DD, LT").format();
                    cells[3].setAttribute("data-order", dateValue);
                });

                // DataTable 초기화
                dataTable = $(table).DataTable({
                    info: false,
                    order: [],
                    pageLength: 10
                });

                // 날짜 범위 선택기 설정
                var today = moment(),
                    dateRangePicker = $("#kt_ecommerce_report_customer_orders_daterangepicker");

                dateRangePicker.daterangepicker({
                    startDate: today,
                    endDate: today,
                    ranges: {
                        '오늘': [today, today],
                        '어제': [moment().subtract(1, "days"), moment().subtract(1, "days")],
                        '지난 7일': [moment().subtract(6, "days"), moment()],
                        '지난 30일': [moment().subtract(29, "days"), moment()],
                        '이번달': [moment().startOf("month"), moment().endOf("month")],
                        '지난달': [moment().subtract(1, "month").startOf("month"), moment().subtract(1, "month").endOf("month")]
                    },
                    locale: {
                        format: 'YYYY/MM/DD'
                    },
                    alwaysShowCalendars: true
                }, function(start, end) {
                    dateRangePicker.html(start.format('YYYY/MM/DD') + ' ~ ' + end.format('YYYY/MM/DD'));
                    dataTable.draw();
                });

                // 처음 페이지 로드 시 '오늘' 날짜로 필터링
                dateRangePicker.data('daterangepicker').setStartDate(today);
                dateRangePicker.data('daterangepicker').setEndDate(today);
                dateRangePicker.data('daterangepicker').clickApply();

                // 내보내기 버튼 설정
                const reportTitle = "Customer Orders Report";
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

                // 상태 필터 설정
                const statusFilter = document.querySelector('[data-kt-ecommerce-order-filter="status"]');
                $(statusFilter).on("change", (event) => {
                    const searchVal = event.target.value === "all" ? "" : event.target.value;
                    dataTable.column(2).search(searchVal).draw();
                });
            }
        }
    }
}();

KTUtil.onDOMContentLoaded(function() {
    KTAppEcommerceReportCustomerOrders.init();
});
