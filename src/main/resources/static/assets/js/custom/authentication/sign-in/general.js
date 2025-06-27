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

                        // 폼 데이터 추출
                        const formData = new FormData(t);
                        
                        // AJAX 헤더 설정
                        const headers = {
                            'X-Requested-With': 'XMLHttpRequest',
                            'Accept': 'application/json, text/plain, */*'
                        };
                        
                        // Fetch 요청으로 로그인 처리
                        fetch(e.closest("form").getAttribute("action"), {
                            method: 'POST',
                            headers: headers,
                            body: formData,
                            credentials: 'same-origin'
                        }).then(response => {
                            // 로그인 성공 시 메인 페이지로 리다이렉트
                            if (response.ok) {
                                window.location.href = '/main';
                                return;
                            }
                            
                            // 401 에러인 경우 - 인증 실패
                            if (response.status === 401) {
                                throw new Error("아이디 또는 비밀번호가 일치하지 않습니다.");
                            }
                            
                            // 기타 에러
                            throw new Error("로그인 처리 중 오류가 발생했습니다.");
                        }).catch(error => {
                            console.error('Error:', error);
                            Swal.fire({
                                text: error.message,
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
                            confirmButtonText: "확인",
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