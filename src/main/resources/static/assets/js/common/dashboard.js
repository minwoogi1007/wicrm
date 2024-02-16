//그래프 생성을 위한 변수
var chartM1=0;
var chartM2=0;
var dailyPointN = 0;

$(document).ready(function() {
    let isFirstCall = true;
    fetchData();
    function fetchData() {
        $.ajax({
            url: "/api/dashboard-data", // 서버 엔드포인트
            type: "GET",
            success: function(response) {
                //console.log(response);
                // response는 각 카드 데이터를 포함하는 객체
                Object.keys(response).forEach(function(key) {
                    // 예: key = 'card-data-1'
                    //console.log(key);
                    if(key=="card-data-1"){
                        const cardData = response[key]; // { "count_Miss": "7", "count_Com": "306" }
                        const countMiss = cardData.count_Miss;
                        const countCom = cardData.count_Com;
                        var countSum = cardData.count_sum;
                        let countRate = cardData.processing_rate;


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
                        dailyPoint = cardData2.dailyPoint;
                        dailyPointN =cardData2.dailyPointN;
                        countRate=cardData2.processing_rate+'%';
                        $('#count-dailPoint-' + key).text(dailyPoint);
                        $('#count-per-' + key).text(countRate);

                    }else if(key=="pointlist-data"){
                        const pointListData = response[key];

                        $('#pointlist').empty();
                        pointListData.forEach((item, index) => {
                            const bulletColor = index === 0 ? 'bg-success' : index === 1 ? 'bg-primary' : 'bg-custom';
                            const bulletStyle = index === 2 ? 'style="background-color: #E4E6EF"' : '';
                            //차트에 필요한 데이터
                            if(index==0){
                                chartM1 = item.dailyPointN;
                            }else if(index==1){
                                chartM2 = item.dailyPointN;
                            }

                            $('#pointlist').append(`
                                
                                <div class="d-flex fw-semibold align-items-center">
                                    <!--begin::Bullet-->
                                        <div class="bullet w-8px h-3px rounded-2 ${bulletColor} me-3" ${bulletStyle}></div>
                                        <div class="text-gray-500 flex-grow-1 me-4">${item.cs_type}</div>
                                        <div class="fw-bolder text-gray-700 text-xxl-end">&#8361;${item.dailyPoint}</div>
                                   
                                </div>
                             `);


                        });
                    }
                });
                KTCardsWidget17.init();
            },
            error: function(xhr, status, error) {
                console.error("Data load failed:", error);
            }
        });

    }

    // 5초마다 fetchData 함수를 호출하여 데이터를 새로고침
    setInterval(fetchData, 10000);
    var KTCardsWidget17 = {
        init: function() {
            ! function() {
                var e = document.getElementById("kt_card_widget_17_chart");
                if (e) {
                    // 캔버스 요소를 제거합니다.
                    e.innerHTML = '';

                    var t = {
                            size: e.getAttribute("data-kt-size") ? parseInt(e.getAttribute("data-kt-size")) : 70,
                            lineWidth: e.getAttribute("data-kt-line") ? parseInt(e.getAttribute("data-kt-line")) : 11,
                            rotate: e.getAttribute("data-kt-rotate") ? parseInt(e.getAttribute("data-kt-rotate")) : 145
                        },
                        a = document.createElement("canvas"),
                        l = document.createElement("span");
                    "undefined" != typeof G_vmlCanvasManager && G_vmlCanvasManager.initElement(a);
                    var r = a.getContext("2d");
                    a.width = a.height = t.size, e.appendChild(l), e.appendChild(a), r.translate(t.size / 2, t.size / 2), r.rotate((t.rotate / 180 - .5) * Math.PI);
                    var o = (t.size - t.lineWidth) / 2,
                        i = function(e, t, a) {
                            a = Math.min(Math.max(0, a || 1), 1), r.beginPath(), r.arc(0, 0, o, 0, 2 * Math.PI * a, !1), r.strokeStyle = e, r.lineCap = "round", r.lineWidth = t, r.stroke()
                        };
                    i("#E4E6EF", t.lineWidth, 1),
                        i(KTUtil.getCssVariableValue("--bs-success"), t.lineWidth, chartM1/dailyPointN),
                        i(KTUtil.getCssVariableValue("--bs-primary"), t.lineWidth, chartM2/dailyPointN)
                }
            }()
        }
    };
    "undefined" != typeof module && (module.exports = KTCardsWidget17), KTUtil.onDOMContentLoaded((function() {
        KTCardsWidget17.init()
    }));

});
