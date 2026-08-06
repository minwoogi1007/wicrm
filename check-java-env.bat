@echo off
echo ========================================
echo Java 환경 진단 시작
echo ========================================

echo.
echo 1. 시스템 Java 버전 확인...
java -version

echo.
echo 2. Java 컴파일러 버전 확인...
javac -version

echo.
echo 3. JAVA_HOME 환경변수 확인...
echo JAVA_HOME: %JAVA_HOME%

echo.
echo 4. PATH에서 Java 경로 확인...
where java

echo.
echo 5. Gradle이 사용하는 Java 버전 확인...
gradlew --version

echo.
echo 6. 현재 디렉토리의 Java 버전 확인...
echo 현재 디렉토리: %CD%
if exist "gradlew.bat" (
    echo Gradle wrapper 존재함
) else (
    echo Gradle wrapper 없음
)

echo.
echo 7. 프로젝트 build.gradle Java 설정 확인...
findstr "sourceCompatibility" build.gradle
findstr "targetCompatibility" build.gradle

echo.
echo ========================================
echo Java 환경 진단 완료
echo ========================================
pause
