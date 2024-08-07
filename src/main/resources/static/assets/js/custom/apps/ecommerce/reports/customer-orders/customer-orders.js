"use strict";

var KTAppEcommerceReportCustomerOrders = function() {
    var table, dataTable;
    return {
        init: function() {
            table = document.querySelector("#kt_ecommerce_report_customer_orders_table");
            if (table) {
                // 날짜 데이터 처리
                table.querySelectorAll("tbody tr").forEach((row) => {
                    const cells = row.querySelectorAll("td");
                    const formattedDate = moment(cells[3].innerText, "YYYY MMM DD").format("YYYY-MM-DD");
                    cells[3].setAttribute("data-order", formattedDate);
                });

                // DataTable 설정
                dataTable = $(table).DataTable({
                    info: false,
                    order: [],
                    pageLength: 10,
                    search: true  // 검색 활성화 확인
                });

                // 검색 기능 활성화
                var searchBox = document.querySelector('[data-kt-ecommerce-order-filter="search"]');
                searchBox.addEventListener('keyup', function() {
                    dataTable.search(this.value).draw();  // 입력 값으로 필터링
                });

                // 날짜 범위 선택기 설정
                var start = moment().subtract(29, "days"),
                    end = moment(),
                    dateRangePicker = $("#kt_ecommerce_report_customer_orders_daterangepicker");

                dateRangePicker.daterangepicker({
                    startDate: start,
                    endDate: end,
                    ranges: {
                        '오늘': [moment(), moment()],
                        '어제': [moment().subtract(1, "days"), moment().subtract(1, "days")],
                        '지난 7일': [moment().subtract(6, "days"), moment()],
                        '지난 30일': [moment().subtract(29, "days"), moment()],
                        '이번달': [moment().startOf("month"), moment().endOf("month")],
                        '지난달': [moment().subtract(1, "month").startOf("month"), moment().subtract(1, "month").endOf("month")]
                    },
                    locale: {
                        format: 'YYYY/MM/DD'
                    }
                }, (start, end) => {
                    dateRangePicker.html(start.format('YYYY/MM/DD') + ' ~ ' + end.format('YYYY/MM/DD'));
                });

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
