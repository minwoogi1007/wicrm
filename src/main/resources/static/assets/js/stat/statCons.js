$(document).ready(function() {
    var cons = [];
    var consCount = [];

    var hiddenStartDate = document.getElementById('hidden_start_date');
    var hiddenEndDate = document.getElementById('hidden_end_date');

    var colorPalette = ['#008FFB', '#00E396', '#FEB019', '#FF4560', '#775DD0', '#546E7A', '#26a69a', '#D10CE8'];

    function fetchDailyData() {
        $.ajax({
            url: "/api/statCons/searchCons",
            type: "GET",
            data: {
                start_date: hiddenStartDate.value,
                end_date: hiddenEndDate.value
            },
            success: function(response) {
                cons = [];
                consCount = [];
                response.statCons.forEach(function(item) {
                    cons.push(item.csname);
                    consCount.push(item.cscount);
                });

                var preparedData = cons.map((name, index) => ({
                    x: name,
                    y: consCount[index],
                    fillColor: colorPalette[index % colorPalette.length]
                }));

                KTChartsWidget18.init(preparedData);
            },
            error: function(xhr, status, error) {
                console.error("Data load failed:", error);
            }
        });
    }

    var KTChartsWidget18 = function() {
        var chartInstance = null;

        return {
            init: function(data) {
                var chartElement = document.getElementById("kt_charts_widget_18_chart");
                if (chartElement) {
                    var options = {
                        series: [{
                            name: "상담 유형",
                            data: data
                        }],
                        chart: {
                            type: 'bar',
                            height: 350,
                            toolbar: {
                                show: false
                            }
                        },
                        plotOptions: {
                            bar: {
                                borderRadius: 4,
                                columnWidth: '50%',
                                dataLabels: {
                                    position: 'top', // Ensure labels are positioned at the top of the bar
                                }
                            }
                        },
                        dataLabels: {
                            enabled: true,
                            offsetY: -20, // Adjust this value to move the label up above the bar
                            style: {
                                fontSize: '12px',
                                colors: ['#333'] // Making sure the text is visible against most backgrounds
                            },
                            formatter: function(val, opts) {
                                return opts.w.config.series[opts.seriesIndex].data[opts.dataPointIndex].y + ' 건';
                            }
                        },
                        xaxis: {
                            categories: data.map(d => d.x)
                        },
                        colors: data.map(d => d.fillColor),
                        fill: {
                            opacity: 1
                        }
                    };

                    if (chartInstance) {
                        chartInstance.updateOptions(options, true); // Force a redraw
                    } else {
                        chartInstance = new ApexCharts(chartElement, options);
                        chartInstance.render();
                    }
                }
            }
        };
    }();

    fetchDailyData();
});
