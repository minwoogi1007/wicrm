
$(document).ready(function() {

    let inCons = [];
    let outCons = [];
    let inCall = [];
    let outCall = [];

    let inConsCount;
    let outConsCount;
    let inCallCount;
    let outCallCount;
    let callSum;

    var hiddenStartDate = document.getElementById('hidden_start_date');
    var hiddenEndDate = document.getElementById('hidden_end_date');
    function fetchCallData() {
        $.ajax({
            url: "/api/statTime/searchTime", // 서버 엔드포인트
            type: "GET",
            data: {
                start_date: hiddenStartDate.value,
                end_date: hiddenEndDate.value
            },
            success: function(response) {
                //console.log(response);
                inCons = [];
                outCons = [];
                inCall = [];
                outCall = [];
                // response는 각 카드 데이터를 포함하는 객체
                Object.keys(response).forEach(function(key) {
                    // 예: key = 'card-data-1'
                    const statCons = response[key];
                    //console.log(key);
                    statCons.forEach((item, index) => {

                        if(item.csname == 1){
                            inCons.push(0,item.hour09, item.hour10, item.hour11, item.hour_12, item.hour13, item.hour14, item.hour15, item.hour16, item.hour17, item.hour18,0);

                        }else if(item.csname == 2){
                            outCons.push(0,item.hour09, item.hour10, item.hour11, item.hour_12, item.hour13, item.hour14, item.hour15, item.hour16, item.hour17, item.hour18,0);

                        }else if(item.csname == 3){
                            inCall.push(0,item.hour09, item.hour10, item.hour11, item.hour_12, item.hour13, item.hour14, item.hour15, item.hour16, item.hour17, item.hour18,0);
                        }else{
                            outCall.push(0,item.hour09, item.hour10, item.hour11, item.hour_12, item.hour13, item.hour14, item.hour15, item.hour16, item.hour17, item.hour18,0);
                        }

                        // callSum = item.callSum;
                    });


                });
                inConsCount = Math.max(...inCons);
                outConsCount =Math.max(...outCons);
                inCallCount=Math.max(...inCall);
                outCallCount=Math.max(...outCall);


                callSum = Math.max(inConsCount, outConsCount, inCallCount,outCallCount);

                KTChartsWidget36.init()
            },
            error: function(xhr, status, error) {
                console.error("Data load failed:", error);
            }
        });


    }

    var KTChartsWidget36 = function() {
        var e = {
                self: null,
                rendered: !1
            },
            t = function(e) {
                var t = document.getElementById("kt_charts_widget_36");
                if (t) {
                    if (e.self) {
                        e.self.destroy(); // 기존 차트 인스턴스 제거
                        e.rendered = false;
                    }
                    var a = parseInt(KTUtil.css(t, "height")),
                        l = KTUtil.getCssVariableValue("--bs-gray-500"),
                        r = KTUtil.getCssVariableValue("--bs-border-dashed-color"),
                        o = KTUtil.getCssVariableValue("--bs-info"),
                        i = KTUtil.getCssVariableValue("--bs-primary"),
                        p = KTUtil.getCssVariableValue("--bs-primary"),
                        s = KTUtil.getCssVariableValue("--bs-success"),
                        z = KTUtil.getCssVariableValue("--bs-warning"),
                        n = {
                            series: [{
                                name: "상담처리 수신",
                                data: inCons

                            }, {
                                name: "상담처리 발신",
                                data: outCons

                            }, {
                                name: "수신",
                                data: inCall
                            }, {
                                name: "발신",
                                data: outCall
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
                                    stops: [15, 120, 100,150]
                                }
                            },
                            stroke: {
                                curve: "smooth",
                                show: !0,
                                width: 3,
                                colors: [p, o, s,z]
                            },
                            xaxis: {
                                categories: ["",   "9 AM", "10 AM", "11 AM", "12 PM", "13 PM", "14 PM", "15 PM", "16 PM", "17 PM", "18 PM",""],
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
                                        color: [p, o, s,z],
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
                                max: callSum,
                                min: 0,
                                tickAmount: 6,
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
                            colors: [p,o, s,z],
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
                                strokeColor: [p, o, s,z],
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

    fetchCallData();

});
