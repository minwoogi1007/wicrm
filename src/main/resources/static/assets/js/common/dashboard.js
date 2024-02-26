//그래프 생성을 위한 변수
//마일리지
let chartM1=0;
let chartM2=0;
let dailyPointN = 0;
let countMiss =0;
let countCom =0;
let countSum =0;
let countRate = 0;

$(document).ready(function() {
    let isFirstCall = true;
    fetchData();
    fetchCallData()

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
                        countMiss = cardData.count_Miss;
                        countCom = cardData.count_Com;
                        countSum = cardData.count_sum;
                        countRate = cardData.processing_rate;
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

                        $('#callSum').text(countSum+'  calls today');
                        $('#count-rate-' + key).text(countRate+'  %'); // "count-com-rate-data-1" 요소에 countCom 값 설정

                        // 프로그레스 바 업데이트
                        $(".progress-bar").css("width", countRate + "%").attr("aria-valuenow", countRate);
                    }else if(key=="card-data-2"){
                        const cardData2 = response[key];
                        dailyPoint = cardData2.dailyPoint;
                        dailyPointN =cardData2.dailyPointN;
                        countRate=cardData2.processing_rate+'%';

                        $('#count-dailPoint-' + key).text(countCom);

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
                                console.log('1111111=='+chartM1);
                                console.log('2222222=='+chartM2);
                            }else if(index==1){
                                chartM2 = item.dailyPointN;
                                console.log('333333333=='+chartM1);
                                console.log('44444444=='+chartM2);
                            }

                            $('#pointlist').append(`
                                
                                <div class="d-flex fw-semibold align-items-center">
                                    <!--begin::Bullet-->
                                        <div class="bullet w-8px h-3px rounded-2 ${bulletColor} me-3" ${bulletStyle}></div>
                                        <div class="text-gray-500 flex-grow-1 me-4">${item.cs_type}</div>
                                        <div class="fw-bolder text-gray-700 text-xxl-end">${item.dailyPoint} 건</div>
                                   
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
                    $('#count-dailPoint-card-data-2').text(countCom);
                    $('#count-per-card-data-2' ).text(countRate+'%');
                });
                KTCardsWidget17.init();
            },
            error: function(xhr, status, error) {
                console.error("Data load failed:", error);
            }
        });

    }
    let comData = [];
    let missData = [];
    function fetchCallData() {
        $.ajax({
            url: "/api/dashboard-callCount-data", // 서버 엔드포인트
            type: "GET",
            success: function(response) {
                console.log(response);

                // response는 각 카드 데이터를 포함하는 객체
                Object.keys(response).forEach(function(key) {
                    // 예: key = 'card-data-1'
                    const callCountList = response[key];
                    //console.log(key);
                    callCountList.forEach((item, index) => {

                        if(item.gubn == 1){
                            comData.push(0,item.hour_09, item.hour_10, item.hour_11, item.hour_12, item.hour_13, item.hour_14, item.hour_15, item.hour_16, item.hour_17, item.hour_18, item.hour_19,0);

                        }else{
                            missData.push(0,item.hour_09, item.hour_10, item.hour_11, item.hour_12, item.hour_13, item.hour_14, item.hour_15, item.hour_16, item.hour_17, item.hour_18, item.hour_19,0);
                        }

                    });


                });
                KTChartsWidget36.init()
            },
            error: function(xhr, status, error) {
                console.error("Data load failed:", error);
            }
        });


    }

    // 5초마다 fetchData 함수를 호출하여 데이터를 새로고침
    //setInterval(fetchData, 10000);



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
                        i(KTUtil.getCssVariableValue("--bs-success"), t.lineWidth, 1-chartM1/countCom),
                        i(KTUtil.getCssVariableValue("--bs-primary"), t.lineWidth, chartM2/countCom)
                }
            }()
            console.log('countCom'+countCom);
            console.log('chartM1/countCom'+chartM1/countCom);

            console.log('chartM2/countCom'+chartM2/countCom);
        }
    };


//상담유형
    var KTChartsWidget6 = function() {
        var e = {
                self: null,
                rendered: !1
            },
            t = function(e) {
                var t = document.getElementById("kt_charts_widget_6");
                if (t) {
                    var a = KTUtil.getCssVariableValue("--bs-gray-800"),
                        l = KTUtil.getCssVariableValue("--bs-border-dashed-color"),
                        r = {
                            series: [{
                                name: "Sales",
                                data: [15, 12, 10, 8, 7]
                            }],
                            chart: {
                                fontFamily: "inherit",
                                type: "bar",
                                height: 350,
                                toolbar: {
                                    show: !1
                                }
                            },
                            plotOptions: {
                                bar: {
                                    borderRadius: 8,
                                    horizontal: !0,
                                    distributed: !0,
                                    barHeight: 50,
                                    dataLabels: {
                                        position: "bottom"
                                    }
                                }
                            },
                            dataLabels: {
                                enabled: !0,
                                textAnchor: "start",
                                offsetX: 0,
                                formatter: function(e, t) {
                                    e *= 1e3;
                                    return wNumb({
                                        thousand: ","
                                    }).to(e)
                                },
                                style: {
                                    fontSize: "14px",
                                    fontWeight: "600",
                                    align: "left"
                                }
                            },
                            legend: {
                                show: !1
                            },
                            colors: ["#3E97FF", "#F1416C", "#50CD89", "#FFC700", "#7239EA"],
                            xaxis: {
                                categories: ["ECR - 90%", "FGI - 82%", "EOQ - 75%", "FMG - 60%", "PLG - 50%"],
                                labels: {
                                    formatter: function(e) {
                                        return e + "K"
                                    },
                                    style: {
                                        colors: [a],
                                        fontSize: "14px",
                                        fontWeight: "600",
                                        align: "left"
                                    }
                                },
                                axisBorder: {
                                    show: !1
                                }
                            },
                            yaxis: {
                                labels: {
                                    formatter: function(e, t) {
                                        return Number.isInteger(e) ? e + " - " + parseInt(100 * e / 18).toString() + "%" : e
                                    },
                                    style: {
                                        colors: a,
                                        fontSize: "14px",
                                        fontWeight: "600"
                                    },
                                    offsetY: 2,
                                    align: "left"
                                }
                            },
                            grid: {
                                borderColor: l,
                                xaxis: {
                                    lines: {
                                        show: !0
                                    }
                                },
                                yaxis: {
                                    lines: {
                                        show: !1
                                    }
                                },
                                strokeDashArray: 4
                            },
                            tooltip: {
                                style: {
                                    fontSize: "12px"
                                },
                                y: {
                                    formatter: function(e) {
                                        return e + "K"
                                    }
                                }
                            }
                        };
                    e.self = new ApexCharts(t, r), setTimeout((function() {
                        e.self.render(), e.rendered = !0
                    }), 200)
                }
            };
        return {
            init: function() {
                t(e), KTThemeMode.on("kt.thememode.change", (function() {
                    e.rendered && e.self.destroy(), t(e)
                }))
            }
        }
    }();
    "undefined" != typeof module && (module.exports = KTChartsWidget6), KTUtil.onDOMContentLoaded((function() {
        KTChartsWidget6.init()
    }));



    var KTChartsWidget36 = function() {
        var e = {
                self: null,
                rendered: !1
            },
            t = function(e) {
                var t = document.getElementById("kt_charts_widget_36");
                if (t) {
                    var a = parseInt(KTUtil.css(t, "height")),
                        l = KTUtil.getCssVariableValue("--bs-gray-500"),
                        r = KTUtil.getCssVariableValue("--bs-border-dashed-color"),
                        o = KTUtil.getCssVariableValue("--bs-primary"),
                        i = KTUtil.getCssVariableValue("--bs-primary"),
                        s = KTUtil.getCssVariableValue("--bs-success"),
                        n = {
                            series: [{
                                name: "처리 완료",
                                data: comData
                            }, {
                                name: "미처리",
                                data: missData
                            }],
                            chart: {
                                fontFamily: "inherit",
                                type: "area",
                                height: a,
                                toolbar: {
                                    show: !1
                                }
                            },
                            plotOptions: {},
                            legend: {
                                show: !1
                            },
                            dataLabels: {
                                enabled: !1
                            },
                            fill: {
                                type: "gradient",
                                gradient: {
                                    shadeIntensity: 1,
                                    opacityFrom: .4,
                                    opacityTo: .2,
                                    stops: [15, 120, 100]
                                }
                            },
                            stroke: {
                                curve: "smooth",
                                show: !0,
                                width: 3,
                                colors: [o, s]
                            },
                            xaxis: {
                                categories: ["",   "9 AM", "10 AM", "11 AM", "12 PM", "13 PM", "14 PM", "15 PM", "16 PM", "17 PM", "18 PM", "19 PM",""],
                                axisBorder: {
                                    show: !1
                                },
                                axisTicks: {
                                    show: !1
                                },
                                tickAmount: 6,
                                labels: {
                                    rotate: 0,
                                    rotateAlways: !0,
                                    style: {
                                        colors: l,
                                        fontSize: "12px"
                                    }
                                },
                                crosshairs: {
                                    position: "front",
                                    stroke: {
                                        color: [o, s],
                                        width: 1,
                                        dashArray: 3
                                    }
                                },
                                tooltip: {
                                    enabled: !0,
                                    formatter: void 0,
                                    offsetY: 0,
                                    style: {
                                        fontSize: "12px"
                                    }
                                }
                            },
                            yaxis: {
                                max: 200,
                                min: 0,
                                tickAmount: 10,
                                labels: {
                                    style: {
                                        colors: l,
                                        fontSize: "12px"
                                    }
                                }
                            },
                            states: {
                                normal: {
                                    filter: {
                                        type: "none",
                                        value: 0
                                    }
                                },
                                hover: {
                                    filter: {
                                        type: "none",
                                        value: 0
                                    }
                                },
                                active: {
                                    allowMultipleDataPointsSelection: !1,
                                    filter: {
                                        type: "none",
                                        value: 0
                                    }
                                }
                            },
                            tooltip: {
                                style: {
                                    fontSize: "12px"
                                }
                            },
                            colors: [i, KTUtil.getCssVariableValue("--bs-success")],
                            grid: {
                                borderColor: r,
                                strokeDashArray: 4,
                                yaxis: {
                                    lines: {
                                        show: !0
                                    }
                                }
                            },
                            markers: {
                                strokeColor: [o, s],
                                strokeWidth: 3
                            }
                        };
                    e.self = new ApexCharts(t, n), setTimeout((function() {
                        e.self.render(), e.rendered = !0
                    }), 200)
                }
            };
        return {
            init: function() {
                t(e), KTThemeMode.on("kt.thememode.change", (function() {
                    e.rendered && e.self.destroy(), t(e)
                }))
            }
        }
    }();

});
