"use strict";
var KTSigninGeneral = function() {
    var t, e, r;
    return {
        init: function() {
            t = document.querySelector("#kt_sign_in_form"), e = document.querySelector("#kt_sign_in_submit"), r = FormValidation.formValidation(t, {
                fields: {
                    userId: {
                        validators: {
                            regexp: {
                                regexp: /^[a-zA-Z0-9]/,
                                message: "영문자와 숫자만 가능합니다."
                            },
                            notEmpty: {
                                message: "아이디는 필수 입력입니다."
                            }
                        }
                    },
                    password: {
                        validators: {
                            notEmpty: {
                                message: "비밀 번호는 필수 입력입니다."
                            }
                        }
                    }
                },
                plugins: {
                    trigger: new FormValidation.plugins.Trigger,
                    bootstrap: new FormValidation.plugins.Bootstrap5({
                        rowSelector: ".fv-row",
                        eleInvalidClass: "",
                        eleValidClass: ""
                    })
                }
            }), ! function(t) {
                try {
                    return new URL(t), !0
                } catch (t) {
                    return !1
                }
            }(e.closest("form").getAttribute("action")) ? e.addEventListener("click", function(i) {
                i.preventDefault();
                r.validate().then(function(status) {
                    if (status == 'Valid') {
                        e.setAttribute("data-kt-indicator", "on");
                        e.disabled = true;

                        // Fetch 요청으로 로그인 처리
                        fetch(e.closest("form").getAttribute("action"), {
                            method: 'POST',
                            body: new FormData(t)
                        }).then(response => {
                            if (response.ok) {
                                return response.text().then(text => {
                                    try {
                                        return JSON.parse(text); // JSON으로 파싱 시도
                                    } catch (e) {
                                        return text; // JSON이 아니면 텍스트 그대로 반환
                                    }
                                });
                            } else {
                                throw new Error('아이디 또는 비밀번호를 잘못 입력했습니다.');
                            }
                        }).then(data => {
                            // 로그인 성공 후 리디렉션 또는 추가 처리
                            window.location.href ='/main';
                        }).catch(error => {
                            console.error('Error:', error);
                            Swal.fire({
                                text: error.message, // 서버로부터의 오류 메시지를 표시
                                icon: "error",
                                buttonsStyling: false,
                                confirmButtonText: "확인",
                                customClass: {
                                    confirmButton: "btn btn-primary"
                                }
                            });
                        }).finally(() => {
                            e.removeAttribute("data-kt-indicator");
                            e.disabled = false;
                        });
                    } else {
                        // 유효성 검사 실패 시 처리
                        Swal.fire({

                            text: "아이디 비밀번호 입력을 다시 해보세요",
                            icon: "error",
                            buttonsStyling: false,
                            confirmButtonText: "Ok, got it!",
                            customClass: {

                                confirmButton: "btn btn-primary"
                            }
                        });
                    }
                });
            }) : e.addEventListener("click", (function(i) {
                i.preventDefault(), r.validate().then((function(r) {
                    "Valid" == r ? (e.setAttribute("data-kt-indicator", "on"), e.disabled = !0, axios.post(e.closest("form").getAttribute("action"), new FormData(t)).then((function(e) {
                        if (e) {
                            t.reset(), Swal.fire({
                                text: "You have successfully logged in!",
                                icon: "success",
                                buttonsStyling: !1,
                                confirmButtonText: "Ok, got it!",
                                customClass: {
                                    confirmButton: "btn btn-primary"
                                }
                            });
                            const e = t.getAttribute("data-kt-redirect-url");
                            e && (location.href = e)
                        } else Swal.fire({
                            text: "111  Sorry, the email or password is incorrect, please try again.",
                            icon: "error",
                            buttonsStyling: !1,
                            confirmButtonText: "Ok, got it!",
                            customClass: {
                                confirmButton: "btn btn-primary"
                            }
                        })
                    })).catch((function(t) {
                        Swal.fire({
                            text: "222  Sorry, looks like there are some errors detected, please try again.",
                            icon: "error",
                            buttonsStyling: !1,
                            confirmButtonText: "Ok, got it!",
                            customClass: {
                                confirmButton: "btn btn-primary"
                            }
                        })
                    })).then((() => {
                        e.removeAttribute("data-kt-indicator"), e.disabled = !1
                    }))) : Swal.fire({
                        text: "333  Sorry, looks like there are some errors detected, please try again.",
                        icon: "error",
                        buttonsStyling: !1,
                        confirmButtonText: "Ok, got it!",
                        customClass: {
                            confirmButton: "btn btn-primary"
                        }
                    })
                }))
            }))
        }
    }
}();
KTUtil.onDOMContentLoaded((function() {
    KTSigninGeneral.init()
}));