@echo off
echo ========================================
echo Gradle 캐시 완전 정리 시작
echo ========================================

echo.
echo 1. Gradle 데몬 중지...
gradlew --stop

echo.
echo 2. Gradle 캐시 폴더 삭제...
if exist "%USERPROFILE%\.gradle\caches" (
    rmdir /s /q "%USERPROFILE%\.gradle\caches"
    echo Gradle caches 폴더 삭제 완료
) else (
    echo Gradle caches 폴더가 존재하지 않습니다.
)

echo.
echo 3. Gradle 데몬 폴더 삭제...
if exist "%USERPROFILE%\.gradle\daemon" (
    rmdir /s /q "%USERPROFILE%\.gradle\daemon"
    echo Gradle daemon 폴더 삭제 완료
) else (
    echo Gradle daemon 폴더가 존재하지 않습니다.
)

echo.
echo 4. Gradle wrapper dists 폴더 삭제...
if exist "%USERPROFILE%\.gradle\wrapper\dists" (
    rmdir /s /q "%USERPROFILE%\.gradle\wrapper\dists"
    echo Gradle wrapper dists 폴더 삭제 완료
) else (
    echo Gradle wrapper dists 폴더가 존재하지 않습니다.
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
echo 6. Gradle wrapper 재다운로드...
gradlew --version

echo.
echo ========================================
echo Gradle 캐시 정리 완료!
echo ========================================
echo.
echo 이제 다음 명령어로 빌드를 시도해보세요:
echo gradlew clean build
echo.
pause
