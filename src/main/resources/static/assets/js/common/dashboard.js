//그래프 생성을 위한 변수
//마일리지
let chartM1=0;
let chartM2=0;
let dailyPointN = 0;
let countMiss =0;
let countCom =0;
let countSum =0;
let countRate = 0;
let callSum=0;
let comSum =0;
let missSum = 0;
let yesterComSum = 0;


$(document).ready(function() {
    let isFirstCall = true;
    fetchData();
    fetchCallData();
    fetchPersonData();
    fetchMonthData();
    fetchDailyData();
    // ID를 사용하여 버튼 선택
    const customCount = document.getElementById('customCount');
    const timeCount = document.getElementById('timeCount');
    const dailyAvg = document.getElementById('dailyAvg');

    customCount.addEventListener('click', function() {
        fetchPersonData()
    });
    timeCount.addEventListener('click', function() {
        fetchCallData()
    });
    dailyAvg.addEventListener('click', function() {
        fetchDailyData()
    });
    $(document).ready(function() {
        $.ajax({
            url: '/api/dashboard-employee',
            type: 'GET',
            success: function(response) {
                // 성공적으로 데이터를 받아온 경우 처리 로직
                console.log(response);

                // 예: 받아온 데이터를 HTML 요소에 동적으로 추가
                // $('#employeeList').append('<p>' + JSON.stringify(response) + '</p>');
            },
            error: function(xhr, status, error) {
                // 요청 실패 시 처리 로직
                console.error("Error: " + status + " " + error);
            }
        });
    });
    function formatNumberWithCommas(number) {
        return number.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }
    function fetchMonthData(){
        $.ajax({
            url: "/api/dashboard-month-data", // 서버 엔드포인트
            type: "GET",
            success: function(response) {
                let thisMonth=0;
                let previousMonth=0;
                let percentChange=0;
                let percent=0;



                // response는 각 카드 데이터를 포함하는 객체
                Object.keys(response).forEach(function(key) {
                    const monthCount = response[key];
                    thisMonth = monthCount.thisMonth;
                    previousMonth =monthCount.previousMonth;
                    percentChange = monthCount.percentChange;

                    console.log(percentChange);
                    if(percentChange <100){
                        percent = 100 - percentChange;
                        console.log("percentChange <100  ===="+percent);
                        $('#percent').text(percent +'%');
                        $('#directionIcon').empty().append(`
                                <i class="ki-duotone ki-arrow-down fs-5 text-danger ms-n1">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                </i>
                                
                         `);


                        $('#percentColor').removeClass('badge-light-success').addClass('badge-light-danger');
                        $('#progressbarMonth').css('width', percentChange + '%').attr('aria-valuenow', percentChange);
                    }else {
                        percent = percentChange-100;
                        console.log("percentChange >100  ===="+percent);
                        $('#percent').text(percent +'%');
                        $('#directionIcon').empty().append(`
                                <i class="ki-duotone ki-arrow-up fs-5 text-success ms-n1">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                </i>
                                
                         `);
                        $('#percentColor').removeClass('badge-light-danger').addClass('badge-light-success');

                        $('#progressbarMonth').css('width', 100 + '%').attr('aria-valuenow', 100);
                    }

                    $('#thisMonth').text(thisMonth.toLocaleString()+'  이번달' );
                    $('#previousMonth').text(previousMonth.toLocaleString() );
                    $('#percentChange').text(percentChange +'%');
                    //
                    // 프로그레스바 너비 업데이트
                    if(percentChange>100){

                    }else{

                    }


                });

            },
            error: function(xhr, status, error) {
                console.error("Data load failed:", error);
            }
        });
    }
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
                        countMiss = cardData.count_Miss;
                        countCom = cardData.count_Com;
                        countSum = cardData.count_sum;
                        countRate = cardData.processing_rate;
                        if (!isFirstCall) {
                            // Apply animation only if it's not the first call
                            // 현재 countSum 요소의 값을 가져와서 숫자로 변환합니다.
                            var currentCountSum = parseInt($('#count-sum-' + key).text()) || 0;

                            var $countSumElement = $('#count-sum-' + key);
                            $({ Counter: currentCountSum }).animate({ Counter: countSum }, {
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

                        // 기타 요소 값 설정
                        $('#count-miss-' + key).text(countMiss);
                        $('#count-com-' + key).text(countCom + ' 처리');
                        // $('#count-sum-' + key).text(countSum); // 애니메이션으로 값이 설정되므로 이 줄은 필요 없습니다.
                        $('#callSum').text(countSum + ' calls today');
                        $('#count-rate-' + key).text(countRate + ' %');

                        // 프로그레스 바 업데이트
                        $(".progress-bar").css("width", countRate + "%").attr("aria-valuenow", countRate);
                    }else if(key=="card-data-2"){
                        const cardData2 = response[key];




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
                                //console.log('1111111=='+chartM1);
                                //console.log('2222222=='+chartM2);
                            }else if(index==1){
                                chartM2 = item.dailyPointN;
                                //console.log('333333333=='+chartM1);
                                //console.log('44444444=='+chartM2);
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
                        const yesterdayEme = cardData3.yesterdayEme;
                        const yesterdayCom = cardData3.yesterdayCom;
                        const yesterdayMiss = cardData3.yesterdayMiss;

                        $('#count-todayMiss-' + key).text(todayMiss);
                        $('#count-todayCom-' + key).text(todayCom);
                        $('#count-todayEme-' + key).text(todayEme);
                        $('#count-yesterdayEme-' + key).text(' / '+yesterdayEme);
                        $('#count-yesterdayCom-' + key).text(' / '+yesterdayCom);
                        $('#count-yesterdayMiss-' + key).text(' / '+yesterdayMiss);

                        if(yesterdayMiss > todayMiss){
                            $('#card-data-3_miss').empty().append(`
                                <i class="ki-duotone ki-arrow-down-right fs-2 text-danger me-2">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                </i>
                            `);
                        }else{
                            $('#card-data-3_miss').empty().append(`
                                <i class="ki-duotone ki-arrow-up-right fs-2 text-success me-2">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                 </i>
                              `);
                        }
                        if(yesterdayEme > todayEme){
                            $('#card-data-3_eme').empty().append(`
                                <i class="ki-duotone ki-arrow-down-right fs-2 text-danger me-2">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                </i>
                            `);
                        }else{
                            $('#card-data-3_eme').empty().append(`
                                <i class="ki-duotone ki-arrow-up-right fs-2 text-success me-2">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                 </i>
                              `);
                        }
                        if(yesterdayCom > todayCom){
                            $('#card-data-3_com').empty().append(`
                                <i class="ki-duotone ki-arrow-down-right fs-2 text-danger me-2">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                </i>
                            `);
                        }else{
                            $('#card-data-3_com').empty().append(`
                                <i class="ki-duotone ki-arrow-up-right fs-2 text-success me-2">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                 </i>
                              `);
                        }
                        //console.log('yesterdayCom==='+yesterdayCom);
                        //console.log('todayCom==='+todayCom);
                        //console.log('yesterdayEme==='+yesterdayEme);
                        //console.log('todayEme==='+todayEme);
                        //console.log('yesterdayMiss==='+yesterdayMiss);
                        //console.log('todayMiss==='+todayMiss);
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
    let yesterComData = [];
    function fetchCallData() {
        $.ajax({
            url: "/api/dashboard-callCount-data", // 서버 엔드포인트
            type: "GET",
            success: function(response) {
                //console.log(response);
                comData = [];
                missData = [];
                yesterComData = [];
                // response는 각 카드 데이터를 포함하는 객체
                Object.keys(response).forEach(function(key) {
                    // 예: key = 'card-data-1'
                    const callCountList = response[key];


                    //console.log(key);
                    callCountList.forEach((item, index) => {

                        if(item.gubn == 1){
                            comData.push(0,item.hour_09, item.hour_10, item.hour_11, item.hour_12, item.hour_13, item.hour_14, item.hour_15, item.hour_16, item.hour_17, item.hour_18, item.hour_19,0);

                        }else if(item.gubn == 2){
                            missData.push(0,item.hour_09, item.hour_10, item.hour_11, item.hour_12, item.hour_13, item.hour_14, item.hour_15, item.hour_16, item.hour_17, item.hour_18, item.hour_19,0);

                        }else{
                            yesterComData.push(0,item.hour_09, item.hour_10, item.hour_11, item.hour_12, item.hour_13, item.hour_14, item.hour_15, item.hour_16, item.hour_17, item.hour_18, item.hour_19,0);
                        }

                       // callSum = item.callSum;
                    });


                });
                comSum = Math.max(...comData);
                missSum =Math.max(...missData);
                yesterComSum=Math.max(...yesterComData);


                callSum = Math.max(comSum, missSum, yesterComSum);

                KTChartsWidget36.init()
            },
            error: function(xhr, status, error) {
                console.error("Data load failed:", error);
            }
        });


    }

    let personMonth = [];
    let newPersonCount = [];
    let oldPersonCount = [];
    let newPersonMax=0;
    let oldPersonMax=0;
    function fetchPersonData(){


        $.ajax({
            url: "/api/dashboard-personCount-data", // 서버 엔드포인트
            type: "GET",
            success: function(response) {
                personMonth = [];
                newPersonCount = [];
                oldPersonCount = [];

                // response는 각 카드 데이터를 포함하는 객체
                Object.keys(response).forEach(function(key) {

                    const personCountList = response[key];



                    //console.log(key);
                    personCountList.forEach((item, index) => {
                        personMonth.push(item.personMonth);
                        newPersonCount.push(item.newPersonCount);
                        oldPersonCount.push(item.oldPersonCount*-1);

                    });


                });


                newPersonMax =Math.max(...newPersonCount);
                oldPersonMax =Math.min(...oldPersonCount);

                if(newPersonMax <100 ){
                    newPersonMax =Math.ceil(newPersonMax/10)*10;
                }else{
                    newPersonMax =Math.ceil(newPersonMax/100)*100;
                }
                if(newPersonMax <100 ){
                    oldPersonMax = Math.floor(oldPersonMax/10)*10;
                }else{
                    oldPersonMax = Math.floor(oldPersonMax/100)*100;
                }


                //console.log(newPersonMax);
                //console.log(oldPersonMax);

                //console.log(personMonth);
                //console.log(newPersonCount);
                //console.log(oldPersonCount);

                KTChartsWidget1.init()
            },
            error: function(xhr, status, error) {
                console.error("Data load failed:", error);
            }
        });
    }
    let avg1week = [];
    let avg1month = [];
    let avg3months = [];
    let avg6months = [];
    let avg9months = [];
    let avg12months = [];

    function fetchDailyData() {
        $.ajax({
            url: "/api/dashboard-daily-data",
            type: "GET",
            success: function(response) {
                // 배열 초기화
                avg1week = [];
                avg1month = [];
                avg3months = [];
                avg6months = [];
                avg9months = [];
                avg12months = [];

                // 데이터 처리
                response.dailyAve.forEach(function(item) {
                    avg1week.push(item.AVG1WEEK);
                    avg1month.push(item.AVG1MONTH);
                    avg3months.push(item.AVG3MONTHS);
                    avg6months.push(item.AVG6MONTHS);
                    avg9months.push(item.AVG9MONTHS);
                    avg12months.push(item.AVG12MONTHS);
                });

                // 차트 업데이트
                KTChartsWidget18.init()
            },
            error: function(xhr, status, error) {
                console.error("Data load failed:", error);
            }
        });
    }

    // 5초마다 fetchData 함수를 호출하여 데이터를 새로고침
    setInterval(fetchData, 25000);
    setInterval(fetchMonthData, 25000);

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
                        i(KTUtil.getCssVariableValue("--bs-success"), t.lineWidth, chartM1/countCom+chartM2/countCom),
                        i(KTUtil.getCssVariableValue("--bs-primary"), t.lineWidth, chartM2/countCom)
                }
            }()
            //console.log('countCom'+countCom);
            //console.log('chartM1/countCom'+chartM1/countCom);

            //console.log('chartM2/countCom'+chartM2/countCom);
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
                    if (e.self) {
                        e.self.destroy(); // 기존 차트 인스턴스 제거
                        e.rendered = false;
                    }
                    var a = parseInt(KTUtil.css(t, "height")),
                        l = KTUtil.getCssVariableValue("--bs-gray-500"),
                        r = KTUtil.getCssVariableValue("--bs-border-dashed-color"),
                        o = KTUtil.getCssVariableValue("--bs-primary"),
                        i = KTUtil.getCssVariableValue("--bs-primary"),
                        p = KTUtil.getCssVariableValue("--bs-secondary"),
                        s = KTUtil.getCssVariableValue("--bs-success"),
                        n = {
                            series: [{
                                name: "전일 처리 완료",
                                data: yesterComData

                            }, {
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
                                colors: [p, o, s]
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
                                        color: [p, o, s],
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
                            colors: [p,i, KTUtil.getCssVariableValue("--bs-success")],
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
                                strokeColor: [p, o, s],
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

    var KTChartsWidget1 = function() {
        var e = {
                self: null,
                rendered: !1
            },
            t = function() {
                var t = document.getElementById("kt_charts_widget_1");
                if (t) {
                    if (e.self) {
                        e.self.destroy(); // 기존 차트 인스턴스 제거
                        e.rendered = false;
                    }
                    var a = t.hasAttribute("data-kt-negative-color") ? t.getAttribute("data-kt-negative-color") : KTUtil.getCssVariableValue("--bs-success"),
                        l = parseInt(KTUtil.css(t, "height")),
                        r = KTUtil.getCssVariableValue("--bs-gray-500"),
                        o = KTUtil.getCssVariableValue("--bs-border-dashed-color"),
                        i = {
                            series: [{
                                name: "신규 인입 고객",
                                data: newPersonCount
                            }, {
                                name: "기존 인입 고객",
                                data: oldPersonCount
                            }],
                            chart: {
                                fontFamily: "inherit",
                                type: "bar",
                                stacked: !0,
                                height: l,
                                toolbar: {
                                    show: !1
                                }
                            },
                            plotOptions: {
                                bar: {
                                    columnWidth: "35%",
                                    barHeight: "70%",
                                    borderRadius: [6, 6]
                                }
                            },
                            legend: {
                                show: !1
                            },
                            dataLabels: {
                                enabled: !1
                            },
                            xaxis: {
                                categories: personMonth,
                                axisBorder: {
                                    show: !1
                                },
                                axisTicks: {
                                    show: !1
                                },
                                tickAmount: 10,
                                labels: {
                                    style: {
                                        colors: [r],
                                        fontSize: "12px"
                                    }
                                },
                                crosshairs: {
                                    show: !1
                                }
                            },
                            yaxis: {
                                min: oldPersonMax,
                                max: newPersonMax,
                                tickAmount: 10,
                                labels: {
                                    style: {
                                        colors: [r],
                                        fontSize: "12px"
                                    },
                                    formatter: function(e) {
                                            if(parseInt(e) < 0 ){
                                                return parseInt(e)*-1
                                            }else{
                                                return parseInt(e)
                                            }

                                    }
                                }
                            },
                            fill: {
                                opacity: 1
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
                                    fontSize: "12px",
                                    borderRadius: 4
                                },
                                y: {
                                    formatter: function(e) {
                                        return e > 0 ? e: Math.abs(e)
                                    }
                                }
                            },
                            colors: [KTUtil.getCssVariableValue("--bs-primary"), a],
                            grid: {
                                borderColor: o,
                                strokeDashArray: 4,
                                yaxis: {
                                    lines: {
                                        show: !0
                                    }
                                }
                            }
                        };
                    e.self = new ApexCharts(t, i), setTimeout((function() {
                        e.self.render(), e.rendered = !0
                    }), 200)
                }
            };
        return {
            init: function() {
                t(), KTThemeMode.on("kt.thememode.change", (function() {
                    e.rendered && e.self.destroy(), t()
                }))
            }
        }
    }();


    //구매몰 유형
    var KTChartsWidget18 = (function() {
        var e = {
            self: null,
            rendered: false
        };

        var t = function(e) {
            var t = document.getElementById("kt_charts_widget_18_chart");
            // 기존 차트가 있다면 제거
            if (e.self) {
                e.self.destroy();
            }

            if (t) {
                var a = parseInt(KTUtil.css(t, "height"));
                var o = {
                    series: [

                        { name: "1주", data: avg1week },
                        { name: "1개월", data: avg1month },
                        { name: "3개월", data: avg3months },
                        { name: "6개월", data: avg6months },
                        { name: "9개월", data: avg9months },
                        { name: "12개월", data: avg12months }
                    ],
                    chart: {
                        type: "bar",
                        height: a,
                        toolbar: { show: false }
                    },
                    plotOptions: {
                        bar: {
                            horizontal: false,
                            columnWidth: '90%',
                            borderRadius: 4,
                            dataLabels: {
                                position: 'top',
                            },
                        },
                    },
                    dataLabels: {
                        enabled: true
                    },
                    legend: {
                        show: true,
                        position: 'top',
                        horizontalAlign: 'right',
                    },
                    xaxis: {
                        categories: ["월요일", "화요일", "수요일", "목요일", "금요일"],
                        labels: {
                            rotate: 0
                        }
                    },
                    yaxis: {
                        title: {
                            text: '상담완료 수'
                        }
                    },
                    fill: {
                        opacity: 1
                    },
                    tooltip: {
                        y: {
                            formatter: function(val) {
                                return val + " 건"
                            }
                        }
                    }
                };
                e.self = new ApexCharts(t, o);
                e.self.render();
            }
        };

        return {
            init: function() {
                t(e);
            },
            updateChart: function(newData) {
                if(e.self) {
                    e.self.updateSeries(newData);
                } else {
                    t(e); // If the chart is not initialized, initialize it.
                }
            }
        }
    })();







});
