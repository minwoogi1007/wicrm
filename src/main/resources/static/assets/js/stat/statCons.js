
$(document).ready(function() {
    var KTChartsWidget18 = function() {
        var e = {
                self: null,
                rendered: !1
            },
            t = function(e) {
                var t = document.getElementById("kt_charts_widget_18_chart");
                if (t) {
                    var a = parseInt(KTUtil.css(t, "height")),
                        l = KTUtil.getCssVariableValue("--bs-gray-900"),
                        r = KTUtil.getCssVariableValue("--bs-border-dashed-color"),
                        o = {
                            series: [{
                                name: "Spent time",
                                data: [54, 42, 75, 110, 23, 87, 50]
                            }],
                            chart: {
                                fontFamily: "inherit",
                                type: "bar",
                                height: a,
                                toolbar: {
                                    show: !1
                                }
                            },
                            plotOptions: {
                                bar: {
                                    horizontal: !1,
                                    columnWidth: ["28%"],
                                    borderRadius: 5,
                                    dataLabels: {
                                        position: "top"
                                    },
                                    startingShape: "flat"
                                }
                            },
                            legend: {
                                show: !1
                            },
                            dataLabels: {
                                enabled: !0,
                                offsetY: -28,
                                style: {
                                    fontSize: "13px",
                                    colors: [l]
                                },
                                formatter: function(e) {
                                    return e
                                }
                            },
                            stroke: {
                                show: !0,
                                width: 2,
                                colors: ["transparent"]
                            },
                            xaxis: {
                                categories: ["QA Analysis", "Marketing", "Web Dev", "Maths", "Front-end Dev", "Physics", "Phylosophy"],
                                axisBorder: {
                                    show: !1
                                },
                                axisTicks: {
                                    show: !1
                                },
                                labels: {
                                    style: {
                                        colors: KTUtil.getCssVariableValue("--bs-gray-500"),
                                        fontSize: "13px"
                                    }
                                },
                                crosshairs: {
                                    fill: {
                                        gradient: {
                                            opacityFrom: 0,
                                            opacityTo: 0
                                        }
                                    }
                                }
                            },
                            yaxis: {
                                labels: {
                                    style: {
                                        colors: KTUtil.getCssVariableValue("--bs-gray-500"),
                                        fontSize: "13px"
                                    },
                                    formatter: function(e) {
                                        return e + "H"
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
                                    fontSize: "12px"
                                },
                                y: {
                                    formatter: function(e) {
                                        return +e + " hours"
                                    }
                                }
                            },
                            colors: [KTUtil.getCssVariableValue("--bs-primary"), KTUtil.getCssVariableValue("--bs-primary-light")],
                            grid: {
                                borderColor: r,
                                strokeDashArray: 4,
                                yaxis: {
                                    lines: {
                                        show: !0
                                    }
                                }
                            }
                        };
                    e.self = new ApexCharts(t, o), setTimeout((function() {
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
    "undefined" != typeof module && (module.exports = KTChartsWidget18), KTUtil.onDOMContentLoaded((function() {
        KTChartsWidget18.init()
    }));


});