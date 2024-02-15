$(document).ready(function() {
    var isFirstCall = true;

    function fetchData() {
        $.ajax({
            url: "/api/dashboard-data", // 서버 엔드포인트
            type: "GET",
            success: function(response) {
                console.log(response);
                // response는 각 카드 데이터를 포함하는 객체
                Object.keys(response).forEach(function(key) {
                    // 예: key = 'card-data-1'
                    console.log(key);
                    if(key=="card-data-1"){
                        const cardData = response[key]; // { "count_Miss": "7", "count_Com": "306" }
                        const countMiss = cardData.count_Miss;
                        const countCom = cardData.count_Com;
                        var countSum = cardData.count_sum;
                        const countRate = cardData.processing_rate;


                        if (!isFirstCall) {
                            // Apply animation only if it's not the first call
                            var $countSumElement = $('#count-sum-' + key);
                            $({ Counter: 0 }).animate({ Counter: countSum }, {
                                duration: 1000,
                                easing: 'swing',
                                step: function () {
                                    $countSumElement.text(Math.ceil(this.Counter));
                                }
                            });
                        } else {
                            // Directly set text on the first call without animation
                            $('#count-sum-' + key).text(countSum);
                        }

                        // 요소 ID를 구성하여 각 값 설정
                        $('#count-miss-' + key).text(countMiss); // "count-miss-card-data-1" 요소에 countMiss 값 설정
                        $('#count-com-' + key).text(countCom+'  처리'); // "count-com-card-data-1" 요소에 countCom 값 설정

                        $('#count-sum-' + key).text(countSum); // "count-com-sum-data-1" 요소에 countCom 값 설정
                        $('#count-rate-' + key).text(countRate+'  %'); // "count-com-rate-data-1" 요소에 countCom 값 설정

                        // 프로그레스 바 업데이트
                        $(".progress-bar").css("width", countRate + "%").attr("aria-valuenow", countRate);
                    }else if(key=="card-data-2"){
                        const cardData2 = response[key];
                        const dailyPoint = cardData2.dailyPoint;
                        $('#count-dailPoint-' + key).text(dailyPoint);
                    }else if(key=="pointlist-data"){
                        const pointListData = response[key];
                        $('#pointlist').empty();
                        pointListData.forEach((item, index) => {

                            $('#pointlist').append(`
                                
                                <div class="d-flex fw-semibold align-items-center">
                                    <!--begin::Bullet-->
                                    <div class="bullet w-8px h-3px rounded-2 bg-success me-3"></div>
                                    <div class="text-gray-500 flex-grow-1 me-4">${item.cs_type}</div>
                                    <div class="fw-bolder text-gray-700 text-xxl-end">&#8361;${item.dailyPoint}</div>
                                   
                                </div>
                             `);

                            if(index==0){
                                $('#pointlist').append(`<div class="bullet w-8px h-3px rounded-2 bg-success me-3"></div>`);
                            }else if(index==1){
                                $('#pointlist').append(`<div class="bullet w-8px h-3px rounded-2 bg-primary me-3"></div>`);
                            }else if(index==2) {
                                $('#pointlist').append(`<div class="bullet w-8px h-3px rounded-2 me-3" style="background-color: #E4E6EF"></div>`);
                            }

                            $('#pointlist').append(`        
                                    <div class="text-gray-500 flex-grow-1 me-4">${item.cs_type}</div>
                                    <div class="fw-bolder text-gray-700 text-xxl-end">&#8361;${item.dailyPoint}</div>
                                   
                                </div>
                            `);
                        });
                    }
                });
            },
            error: function(xhr, status, error) {
                console.error("Data load failed:", error);
            }
        });
    }
    fetchData();
    // 5초마다 fetchData 함수를 호출하여 데이터를 새로고침
    setInterval(fetchData, 60000);
});