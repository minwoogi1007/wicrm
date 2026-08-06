@echo off
echo ========================================
echo IntelliJ IDEA Java 버전 문제 강제 해결
echo ========================================

echo.
echo 1. IntelliJ IDEA 완전 종료...
taskkill /f /im idea64.exe 2>nul
taskkill /f /im idea.exe 2>nul
timeout /t 3 /nobreak >nul

echo.
echo 2. IntelliJ IDEA 캐시 완전 삭제...
if exist "%APPDATA%\JetBrains" (
    echo JetBrains 캐시 폴더 발견
    if exist "%APPDATA%\JetBrains\IntelliJIdea*" (
        for /d %%i in ("%APPDATA%\JetBrains\IntelliJIdea*") do (
            echo IntelliJ IDEA 캐시 삭제 중: %%i
            rmdir /s /q "%%i\caches" 2>nul
            rmdir /s /q "%%i\index" 2>nul
            rmdir /s /q "%%i\local" 2>nul
            echo %%i 캐시 삭제 완료
        )
    )
) else (
    echo JetBrains 폴더가 존재하지 않습니다.
)

echo.
echo 3. 프로젝트 .idea 폴더 백업 및 재생성...
if exist ".idea" (
    echo .idea 폴더 백업 중...
    xcopy ".idea" ".idea.backup" /e /i /h /y >nul
    echo .idea 폴더 백업 완료
    
    echo .idea 폴더 삭제 중...
    rmdir /s /q ".idea"
    echo .idea 폴더 삭제 완료
) else (
    echo .idea 폴더가 존재하지 않습니다.
)

echo.
echo 4. Gradle 캐시 완전 정리...
if exist "%USERPROFILE%\.gradle" (
    rmdir /s /q "%USERPROFILE%\.gradle"
    echo Gradle 홈 폴더 완전 삭제 완료
) else (
    echo Gradle 홈 폴더가 존재하지 않습니다.
)

echo.
echo 5. 프로젝트 build 폴더 삭제...
if exist "build" (
    rmdir /s /q "build"
    echo 프로젝트 build 폴더 삭제 완료
) else (
    echo 프로젝트 build 폴더가 존재하지 않습니다.
)

echo.
echo 6. .gradle 폴더 삭제...
if exist ".gradle" (
    rmdir /s /q ".gradle"
    echo 로컬 .gradle 폴더 삭제 완료
) else (
    echo 로컬 .gradle 폴더가 존재하지 않습니다.
)

echo.
echo 7. JAVA_HOME 환경변수 강제 설정...
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot" /M
echo JAVA_HOME 시스템 환경변수 설정 완료

echo.
echo 8. PATH에 Java 17 강제 추가...
setx PATH "%PATH%;C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot\bin" /M
echo PATH 시스템 환경변수 업데이트 완료

echo.
echo 9. Gradle wrapper 재다운로드...
gradlew --version

echo.
echo ========================================
echo IntelliJ IDEA Java 버전 문제 해결 완료!
echo ========================================
echo.
echo 다음 단계를 수행하세요:
echo.
echo 1. 명령 프롬프티를 새로 열어주세요 (환경변수 적용)
echo 2. IntelliJ IDEA를 새로 실행하세요
echo 3. 프로젝트를 다시 열어주세요
echo 4. Gradle 프로젝트 동기화를 수행하세요
echo.
echo 만약 여전히 문제가 있다면:
echo - IntelliJ IDEA 설정에서 Project SDK를 Java 17로 강제 설정
echo - Gradle JVM을 Java 17로 강제 설정
echo.
pause
