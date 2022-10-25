:::
:::  ____  ___ ____ _   _ _____ ____     __  __    _    _   _    _    ____ _____ __  __ _____ _   _ _____    ____  _____ ______     _______ ____  
::: |  _ \|_ _/ ___| | | |_   _/ ___|   |  \/  |  / \  | \ | |  / \  / ___| ____|  \/  | ____| \ | |_   _|  / ___|| ____|  _ \ \   / | ____|  _ \ 
::: | |_) || | |  _| |_| | | | \___ \   | |\/| | / _ \ |  \| | / _ \| |  _|  _| | |\/| |  _| |  \| | | |    \___ \|  _| | |_) \ \ / /|  _| | |_) |
::: |  _ < | | |_| |  _  | | |  ___) |  | |  | |/ ___ \| |\  |/ ___ | |_| | |___| |  | | |___| |\  | | |     ___) | |___|  _ < \ V / | |___|  _ < 
::: |_| \_|___\____|_| |_| |_| |____/   |_|  |_/_/   \_|_| \_/_/   \_\____|_____|_|  |_|_____|_| \_| |_|    |____/|_____|_| \_\ \_/  |_____|_| \_\
:::                                                                                                                                                                                                                                  	     
rem for /f "delims=: tokens=*" %%A in ('findstr /b ::: "%~f0"') do @echo(%%A

@ECHO OFF

REM  --> Check for permissions
:check_Permissions
net session >nul 2>&1
IF NOT %errorLevel% == 0 (
	echo Administrative permissions are required to %1 Rights Management Server. 
	echo The %1ation cannot continue.
	exit /B
)

IF "%1" == "install" (
	TITLE Installing Rights Management Server
) ELSE (
	TITLE Uninstalling Rights Management Server
)

for %%i in ("%~dp0..") do SET "START_DIR=%%~fi"
for /f %%x in ('wmic path win32_utctime get /format:list ^| findstr "="') do set %%x
set RMS_CUR_TIME=%Day%%Month%%Year%_%Hour%%Minute%%Second%
set LOG_DIR=RMS_Installer_%RMS_CUR_TIME%.log
set UI_LOG_DIR=RMS_Installer_UI_%RMS_CUR_TIME%.log
set GEM_HOME=%START_DIR%\engine\gems
if "%2" == "-g" (
	echo Starting GUI %1er ...
	call "%START_DIR%\engine\chef\embedded\bin\ruby.exe" "%START_DIR%\bin\ui\app.rb" %1 >"%TEMP%\%UI_LOG_DIR%" 2>&1
)else (
	echo Starting the %1er ...
	call "%START_DIR%\engine\chef\bin\chef-client.bat" --format null --config "%START_DIR%\bin\client.rb" -A -o RMS::%1
)
echo.

echo Removing temporary files ...
if exist "%TEMP%\local-mode-cache" rmdir "%TEMP%\local-mode-cache" /S /Q
echo Removed temporary files.
if exist "%TEMP%\%LOG_DIR%" echo Setup completed. See the %1ation log at %TEMP%\%LOG_DIR% for the details.