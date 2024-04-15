var KTAppEcommerceReportCustomerOrders = function() {
    var t, e;
    return {
        init: function() {
            (t = document.querySelector("#kt_ecommerce_report_customer_orders_table")) && (t.querySelectorAll("tbody tr").forEach((t => {
                const e = t.querySelectorAll("td"),
                    r = moment(e[3].innerHTML, "YYYY MMM DD, LT").format();
                e[3].setAttribute("data-order", r)
            })), e = $(t).DataTable({
                info: !1,
                order: [],
                pageLength: 10
            }), (() => {
                var t = moment().subtract(29, "days"),
                    e = moment(),
                    r = $("#kt_ecommerce_report_customer_orders_daterangepicker");

                r.daterangepicker({
                    startDate: t,
                    endDate: e,
                    ranges: {
                        오늘: [moment(), moment()],
                        어제: [moment().subtract(1, "days"), moment().subtract(1, "days")],
                        "지난 7일": [moment().subtract(6, "days"), moment()],
                        "지난 30일": [moment().subtract(29, "days"), moment()],
                        "이번달": [moment().startOf("month"), moment().endOf("month")],
                        "지난달": [moment().subtract(1, "month").startOf("month"), moment().subtract(1, "month").endOf("month")]
                    },
                    locale: {
                        format: 'YYYY/MM/DD'
                    }
                }, function(start, end) {
                    r.html(start.format('YYYY/MM/DD') + '  ~  ' + end.format('YYYY/MM/DD'));
                });
            })(), (() => {
                const e = "Customer Orders Report";
                new $.fn.dataTable.Buttons(t, {
                    buttons: [{
                        extend: "copyHtml5",
                        title: e
                    }, {
                        extend: "excelHtml5",
                        title: e
                    }, {
                        extend: "csvHtml5",
                        title: e
                    }, {
                        extend: "pdfHtml5",
                        title: e
                    }]
                }).container().appendTo($("#kt_ecommerce_report_customer_orders_export")), document.querySelectorAll("#kt_ecommerce_report_customer_orders_export_menu [data-kt-ecommerce-export]").forEach((t => {
                    t.addEventListener("click", (t => {
                        t.preventDefault();
                        const e = t.target.getAttribute("data-kt-ecommerce-export");
                        document.querySelector(".dt-buttons .buttons-" + e).click()
                    }))
                }))
            })(), (() => {
                const t = document.querySelector('[data-kt-ecommerce-order-filter="status"]');
                $(t).on("change", (t => {
                    let r = t.target.value;
                    "all" === r && (r = ""), e.column(2).search(r).draw()
                }))
            })())
        }
    }
}();
KTUtil.onDOMContentLoaded((function() {
    KTAppEcommerceReportCustomerOrders.init()
}));
