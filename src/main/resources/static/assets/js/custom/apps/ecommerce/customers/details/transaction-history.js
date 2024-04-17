"use strict";

var KTCustomerViewPaymentTable = function() {
    var dataTable, paymentTable = document.querySelector("#kt_table_customers_payment");

    return {
        init: function() {
            if (paymentTable) {
                // Initialize the DataTable on the #kt_table_customers_payment element
                dataTable = $(paymentTable).DataTable({
                    info: false,               // Disables the information text that is usually shown below the table
                    paging: true,              // Enables pagination
                    order: [],                 // No initial order
                    pageLength: 10,             // Set number of entries to show per page
                    lengthChange: false,       // User cannot change the number of entries shown per page
                    searching: false,          // Disables the search/filter functionality
                    ordering: false            // Disables all column-based ordering to keep the setup simple
                });
            }
        }
    }
}();

// This function will be called when the DOM is fully loaded to ensure all HTML is ready
KTUtil.onDOMContentLoaded(function() {
    KTCustomerViewPaymentTable.init();
});
