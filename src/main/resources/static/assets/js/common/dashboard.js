//그래프 생성을 위한 변수
//마일리지
var chartM1=0;
var chartM2=0;
var dailyPointN = 0;


$(document).ready(function() {
    let isFirstCall = true;
    fetchData();
    fetchDataCon();
    function fetchData() {
        $.ajax({
            url: "/api/dashboard-data", // 서버 엔드포인트
            type: "GET",
            success: function(response) {
                console.log(response);
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
                    }else if(key=="card-data-3"){
                        const cardData3 = response[key];
                        const todayMiss = cardData3.todayMiss;
                        const todayCom = cardData3.todayCom;
                        const todayEme = cardData3.todayEme;
                        const yseterdayEme = cardData3.yseterdayEme;
                        const yesterdayCom = cardData3.yesterdayCom;
                        const yesterdayMiss = cardData3.yesterdayMiss;

                        $('#count-todayMiss-' + key).text(todayMiss);
                        $('#count-todayCom-' + key).text(todayCom);
                        $('#count-todayEme-' + key).text(todayEme);
                        $('#count-yseterdayEme-' + key).text(' / '+yseterdayEme);
                        $('#count-yesterdayCom-' + key).text(' / '+yesterdayCom);
                        $('#count-yesterdayMiss-' + key).text(' / '+yesterdayMiss);
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
    //setInterval(fetchData, 10000);



//상담 유형 상위6개
    let conCsTypeD1="";
    let conCsTypeD2="";
    let conCsTypeD3="";
    let conCsTypeD4="";
    let conCsTypeD5="";
    let conCsTypeD6="";

    let conCsCountD1=0;
    let conCsCountD2=0;
    let conCsCountD3=0;
    let conCsCountD4=0;
    let conCsCountD5=0;
    let conCsCountD6=0;

    let conCsPayD1=0;
    let conCsPayD2=0;
    let conCsPayD3=0;
    let conCsPayD4=0;
    let conCsPayD5=0;
    let conCsPayD6=0;

    let conCsPerD1=0;
    let conCsPerD2=0;
    let conCsPerD3=0;
    let conCsPerD4=0;
    let conCsPerD5=0;
    let conCsPerD6=0;


    let conCsTypeW1="";
    let conCsTypeW2="";
    let conCsTypeW3="";
    let conCsTypeW4="";
    let conCsTypeW5="";
    let conCsTypeW6="";

    let conCsCountW1=0;
    let conCsCountW2=0;
    let conCsCountW3=0;
    let conCsCountW4=0;
    let conCsCountW5=0;
    let conCsCountW6=0;

    let conCsPayW1=0;
    let conCsPayW2=0;
    let conCsPayW3=0;
    let conCsPayW4=0;
    let conCsPayW5=0;
    let conCsPayW6=0;

    let conCsPerW1=0;
    let conCsPerW2=0;
    let conCsPerW3=0;
    let conCsPerW4=0;
    let conCsPerW5=0;
    let conCsPerW6=0;

    function fetchDataCon() {
        $.ajax({
            url: "/api/dashboard-conCount-data", // 서버 엔드포인트
            type: "GET",
            success: function(response) {
                console.log("상담유형 그래프 조회");
                console.log(response);
                // response는 각 카드 데이터를 포함하는 객체
                Object.keys(response).forEach(function(key) {
                    // 예: key = 'card-data-1'
                    //console.log(key);
                    if (response["dashStatCount-data"]) {
                        const conCountList = response["dashStatCount-data"];
                        let weekData = [];
                        let dayData = [];

                        // 데이터 변환
                        conCountList.forEach((item,index) => {

                            if(index==0){
                                conCsTypeW1=item.cs_Name;
                                conCsCountW1=item.cs_Type_Count;
                                conCsPayW1=item.cs_Type_Point;
                                conCsPerW1=item.cs_Type_Percentage;
                            }else if(index==1){
                                conCsTypeW2=item.cs_Name;
                                conCsCountW2=item.cs_Type_Count;
                                conCsPayW2=item.cs_Type_Point;
                                conCsPerW2=item.cs_Type_Percentage;
                            }else if(index==2){
                                conCsTypeW3=item.cs_Name;
                                conCsCountW3=item.cs_Type_Count;
                                conCsPayW3=item.cs_Type_Point;
                                conCsPerW3=item.cs_Type_Percentage;
                            }else if(index==3){
                                conCsTypeW4=item.cs_Name;
                                conCsCountW4=item.cs_Type_Count;
                                conCsPayW4=item.cs_Type_Point;
                                conCsPerW4=item.cs_Type_Percentage;
                            }else if(index==4){
                                conCsTypeW5=item.cs_Name;
                                conCsCountW5=item.cs_Type_Count;
                                conCsPayW5=item.cs_Type_Point;
                                conCsPerW5=item.cs_Type_Percentage;
                            }else if(index==5){
                                conCsTypeW6=item.cs_Name;
                                conCsCountW6=item.cs_Type_Count;
                                conCsPayW6=item.cs_Type_Point;
                                conCsPerW6=item.cs_Type_Percentage;
                            }else if(index==6){

                                conCsTypeD1=item.cs_Name;
                                conCsCountD1=item.cs_Type_Count;
                                conCsPayD1=item.cs_Type_Point;
                                conCsPerD1=item.cs_Type_Percentage;

                            }else if(index==7){

                                conCsTypeD2=item.cs_Name;
                                conCsCountD2=item.cs_Type_Count;
                                conCsPayD2=item.cs_Type_Point;
                                conCsPerD2=item.cs_Type_Percentage;

                            }else if(index==8){

                                conCsTypeD3=item.cs_Name;
                                conCsCountD3=item.cs_Type_Count;
                                conCsPayD3=item.cs_Type_Point;
                                conCsPerD3=item.cs_Type_Percentage;

                            }else if(index==9){

                                conCsTypeD4=item.cs_Name;
                                conCsCountD4=item.cs_Type_Count;
                                conCsPayD4=item.cs_Type_Point;
                                conCsPerD4=item.cs_Type_Percentage;

                            }else if(index==10){

                                conCsTypeD5=item.cs_Name;
                                conCsCountD5=item.cs_Type_Count;
                                conCsPayD5=item.cs_Type_Point;
                                conCsPerD5=item.cs_Type_Percentage;

                            }else if(index==11){
                                conCsTypeD6=item.cs_Name;
                                conCsCountD6=item.cs_Type_Count;
                                conCsPayD6=item.cs_Type_Point;
                                conCsPerD6=item.cs_Type_Percentage;

                            }
                          });

                        // 차트 그리기 함수 호출
                        KTChartsWidget8.init();
                        //a(e, "#kt_chart_widget_8_week_toggle", "#kt_chart_widget_8_week_chart", weekData, false);
                        //a(t, "#kt_chart_widget_8_month_toggle", "#kt_chart_widget_8_month_chart", dayData, true);
                    }

                });

            },
            error: function(xhr, status, error) {
                console.error("Data load failed:", error);
            }
        });

    }
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

    var KTChartsWidget8 = function() {
        var e = {
                self: null,
                rendered: !1
            },
            t = {
                self: null,
                rendered: !1
            },
            a = function(e, t, a, l, r) {
                var o = document.querySelector(a);
                if (o) {
                    var i = parseInt(KTUtil.css(o, "height")),
                        s = KTUtil.getCssVariableValue("--bs-border-dashed-color"),
                        n = {
                            series: [{
                                name: "Social Campaigns",
                                data: l[0]
                            }, {
                                name: "Email Newsletter",
                                data: l[1]
                            }, {
                                name: "TV Campaign",
                                data: l[2]
                            }, {
                                name: "Google Ads",
                                data: l[3]
                            }, {
                                name: "Courses",
                                data: l[4]
                            }, {
                                name: "Radio",
                                data: l[5]
                            }],
                            chart: {
                                fontFamily: "inherit",
                                type: "bubble",
                                height: i,
                                toolbar: {
                                    show: !1
                                }
                            },
                            plotOptions: {
                                bubble: {}
                            },
                            stroke: {
                                show: !1,
                                width: 0
                            },
                            legend: {
                                show: !1
                            },
                            dataLabels: {
                                enabled: !1
                            },
                            xaxis: {
                                type: "numeric",
                                tickAmount: 7,
                                min: 0,
                                max: 700,
                                axisBorder: {
                                    show: !1
                                },
                                axisTicks: {
                                    show: !0,
                                    height: 0
                                },
                                labels: {
                                    show: !0,
                                    trim: !0,
                                    style: {
                                        colors: KTUtil.getCssVariableValue("--bs-gray-500"),
                                        fontSize: "13px"
                                    }
                                }
                            },
                            yaxis: {
                                tickAmount: 7,
                                min: 0,
                                max: 700,
                                labels: {
                                    style: {
                                        colors: KTUtil.getCssVariableValue("--bs-gray-500"),
                                        fontSize: "13px"
                                    }
                                }
                            },
                            tooltip: {
                                style: {
                                    fontSize: "12px"
                                },
                                x: {
                                    formatter: function(e) {
                                        return "Clicks: " + e
                                    }
                                },
                                y: {
                                    formatter: function(e) {
                                        return "$" + e + "K"
                                    }
                                },
                                z: {
                                    title: "Impression: "
                                }
                            },
                            crosshairs: {
                                show: !0,
                                position: "front",
                                stroke: {
                                    color: KTUtil.getCssVariableValue("--bs-border-dashed-color"),
                                    width: 1,
                                    dashArray: 0
                                }
                            },
                            colors: [KTUtil.getCssVariableValue("--bs-primary"), KTUtil.getCssVariableValue("--bs-success"), KTUtil.getCssVariableValue("--bs-warning"), KTUtil.getCssVariableValue("--bs-danger"), KTUtil.getCssVariableValue("--bs-info"), "#43CED7"],
                            fill: {
                                opacity: 1
                            },
                            markers: {
                                strokeWidth: 0
                            },
                            grid: {
                                borderColor: s,
                                strokeDashArray: 4,
                                padding: {
                                    right: 20
                                },
                                yaxis: {
                                    lines: {
                                        show: !0
                                    }
                                }
                            }
                        };
                    e.self = new ApexCharts(o, n);
                    var d = document.querySelector(t);
                    !0 === r && setTimeout((function() {
                        e.self.render(), e.rendered = !0
                    }), 200), d.addEventListener("shown.bs.tab", (function(t) {
                        !1 === e.rendered && (e.self.render(), e.rendered = !0)
                    }))
                }
            };
        return {
            init: function() {
                var l = [
                        [
                            [100, 250, 30]
                        ],
                        [
                            [225, 300, 35]
                        ],
                        [
                            [300, 350, 25]
                        ],
                        [
                            [350, 350, 20]
                        ],
                        [
                            [450, 400, 25]
                        ],
                        [
                            [550, 350, 35]
                        ]
                    ],
                    r = [
                        [
                            [125, 300, 40]
                        ],
                        [
                            [250, 350, 35]
                        ],
                        [
                            [350, 450, 30]
                        ],
                        [
                            [450, 250, 25]
                        ],
                        [
                            [500, 500, 30]
                        ],
                        [
                            [600, 250, 28]
                        ]
                    ];
                console.log(l);
                console.log(r);
                a(e, "#kt_chart_widget_8_week_toggle", "#kt_chart_widget_8_week_chart", l, !1), a(t, "#kt_chart_widget_8_month_toggle", "#kt_chart_widget_8_month_chart", r, !0);
                KTThemeMode.on("kt.thememode.change", (function() {
                    e.rendered && e.self.destroy(), t.rendered && t.self.destroy(), a(e, "#kt_chart_widget_8_week_toggle", "#kt_chart_widget_8_week_chart", l, e.rendered), a(t, "#kt_chart_widget_8_month_toggle", "#kt_chart_widget_8_month_chart", r, t.rendered)
                }))
            }
        }
    }();
});
