$(document).ready(function() {
    $.ajax({
        url: "/api/dashboard-data", // 서버 엔드포인트
        type: "GET",
        success: function(response) {
            // response는 각 카드 데이터를 포함하는 객체
            Object.keys(response).forEach(function(key) {
                // 예: key = 'card-data-1'
                const cardData = response[key]; // { "count_Miss": "7", "count_Com": "306" }


                const countMiss = cardData.count_Miss;
                const countCom = cardData.count_Com;
                const countSum = cardData.count_sum;
                const countRate = cardData.processing_rate;

                // 요소 ID를 구성하여 각 값 설정
                $('#count-miss-' + key).text(countMiss); // "count-miss-card-data-1" 요소에 countMiss 값 설정
                $('#count-com-' + key).text(countCom+'  처리'); // "count-com-card-data-1" 요소에 countCom 값 설정
                $('#count-sum-' + key).text(countSum); // "count-com-sum-data-1" 요소에 countCom 값 설정
                $('#count-rate-' + key).text(countRate+'  %'); // "count-com-rate-data-1" 요소에 countCom 값 설정

                // 프로그레스 바 업데이트
                $(".progress-bar").css("width", countRate + "%").attr("aria-valuenow", countRate);
            });
        },
        error: function(xhr, status, error) {
            console.error("Data load failed:", error);
        }
    });
});
