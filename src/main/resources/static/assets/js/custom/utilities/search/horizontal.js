"use strict";
var KTSearchHorizontal = {
    init: function() {
        var e, n;
        e = document.querySelector("#kt_advanced_search_form").querySelector('[name="tags"]');
        if (e) {
            new Tagify(e);
        }

        n = document.querySelector("#kt_horizontal_search_advanced_link");
        var advancedSearchForm = document.querySelector("#kt_advanced_search_form");

        if (n && advancedSearchForm) {
            n.addEventListener("click", function(e) {
                e.preventDefault();
                if (advancedSearchForm.style.display === "none" || advancedSearchForm.style.display === "") {
                    advancedSearchForm.style.display = "block";
                    n.innerHTML = "숨기기";
                } else {
                    advancedSearchForm.style.display = "none";
                    n.innerHTML = "상세조회";
                }
            });
        }
    }
};

KTUtil.onDOMContentLoaded(function() {
    KTSearchHorizontal.init();
});