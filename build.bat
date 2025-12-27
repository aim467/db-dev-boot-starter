@echo off
echo ========================================
echo Building DB Dev Spring Boot Starter
echo ========================================

echo.
echo Step 1: Clean and install parent project
call mvn clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    exit /b 1
)

echo.
echo ========================================
echo Build completed successfully!
echo ========================================
echo.
echo To run the example:
echo   cd example
echo   mvn spring-boot:run
echo.
