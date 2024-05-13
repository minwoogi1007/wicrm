"use strict";
var KTSearchHorizontal = {
    init: function() {
        var e, n;
        e = document.querySelector("#kt_advanced_search_form").querySelector('[name="tags"]'), new Tagify(e), (n = document.querySelector("#kt_horizontal_search_advanced_link")).addEventListener("click", (function(e) {
            e.preventDefault(), "상세조회" === n.innerHTML ? n.innerHTML = "숨기기" : n.innerHTML = "상세조회"
        }))
    }
};
KTUtil.onDOMContentLoaded((function() {
    KTSearchHorizontal.init()
}));