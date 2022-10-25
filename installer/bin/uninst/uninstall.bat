@ECHO OFF
for %%i in ("%~dp0") do SET "START_DIR=%%~fi"

echo.
echo Extracting the uninstaller. This may take a few minutes ...
xcopy /s /y /q "%START_DIR%\.uninst" "%TEMP%\RMS_TMP\"

start call "%TEMP%\RMS_TMP\uninstall.bat" %1