"use strict";
var KTSearchHorizontal = {
    init: function() {
        var advancedSearchForm = document.querySelector("#kt_advanced_search_form");
        
        // 해당 요소가 없는 페이지에서는 초기화 스킵
        if (!advancedSearchForm) {
            return;
        }

        var tagsInput = advancedSearchForm.querySelector('[name="tags"]');
        if (tagsInput && typeof Tagify !== 'undefined') {
            new Tagify(tagsInput);
        }

        var advancedLink = document.querySelector("#kt_horizontal_search_advanced_link");

        if (advancedLink) {
            advancedLink.addEventListener("click", function(e) {
                e.preventDefault();
                if (advancedSearchForm.style.display === "none" || advancedSearchForm.style.display === "") {
                    advancedSearchForm.style.display = "block";
                    advancedLink.innerHTML = "숨기기";
                } else {
                    advancedSearchForm.style.display = "none";
                    advancedLink.innerHTML = "상세조회";
                }
            });
        }
    }
};

KTUtil.onDOMContentLoaded(function() {
    KTSearchHorizontal.init();
});