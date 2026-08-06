@echo off
echo ========================================
echo Java 버전 문제 강제 해결 시작
echo ========================================

echo.
echo 1. Gradle 데몬 완전 중지...
gradlew --stop
timeout /t 3 /nobreak >nul

echo.
echo 2. 모든 Gradle 프로세스 강제 종료...
taskkill /f /im java.exe 2>nul
taskkill /f /im gradle.exe 2>nul
timeout /t 2 /nobreak >nul

echo.
echo 3. Gradle 캐시 완전 삭제...
if exist "%USERPROFILE%\.gradle" (
    rmdir /s /q "%USERPROFILE%\.gradle"
    echo Gradle 홈 폴더 완전 삭제 완료
) else (
    echo Gradle 홈 폴더가 존재하지 않습니다.
)

echo.
echo 4. 프로젝트 build 폴더 삭제...
if exist "build" (
    rmdir /s /q "build"
    echo 프로젝트 build 폴더 삭제 완료
) else (
    echo 프로젝트 build 폴더가 존재하지 않습니다.
)

echo.
echo 5. .gradle 폴더 삭제...
if exist ".gradle" (
    rmdir /s /q ".gradle"
    echo 로컬 .gradle 폴더 삭제 완료
) else (
    echo 로컬 .gradle 폴더가 존재하지 않습니다.
)

echo.
echo 6. JAVA_HOME 환경변수 확인 및 설정...
echo 현재 JAVA_HOME: %JAVA_HOME%
if "%JAVA_HOME%"=="" (
    echo JAVA_HOME이 설정되지 않았습니다.
    echo Java 17 경로를 찾는 중...
    if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot" (
        echo Java 17 발견: C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot
        setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
        echo JAVA_HOME 환경변수 설정 완료
    ) else (
        echo Java 17을 찾을 수 없습니다. 수동으로 설정해주세요.
    )
) else (
    echo JAVA_HOME이 이미 설정되어 있습니다.
)

echo.
echo 7. PATH에 Java 17 추가...
echo %PATH% | findstr "jdk-17" >nul
if errorlevel 1 (
    echo PATH에 Java 17 경로 추가 중...
    setx PATH "%PATH%;C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot\bin"
    echo PATH 업데이트 완료
) else (
    echo PATH에 Java 17이 이미 포함되어 있습니다.
)

echo.
echo 8. Gradle wrapper 재다운로드...
gradlew --version

echo.
echo 9. 강제 빌드 시도...
gradlew clean build --refresh-dependencies --no-daemon

echo.
echo ========================================
echo Java 버전 문제 강제 해결 완료!
echo ========================================
echo.
echo 만약 여전히 문제가 있다면:
echo 1. IDE를 완전히 재시작하세요
echo 2. 명령 프롬프트를 새로 열어주세요
echo 3. 환경변수 변경사항이 적용되었는지 확인하세요
echo.
pause
