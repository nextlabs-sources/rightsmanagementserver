@ECHO OFF

SETLOCAL EnableDelayedExpansion
SET RMS_GUI=true

:loop
IF NOT "%1"=="" (
	set RMS_VALID_ARGS=false
	IF "%1"=="-g" (
		SET RMS_GUI=true
		SET RMS_VALID_ARGS=true
	)

	IF "%1"=="-s" (
		SET RMS_GUI=false
		SET RMS_VALID_ARGS=true
	)
	IF "%1"=="-h" (
		SET RMS_VALID_ARGS=true
		GOTO :help
	)
	IF NOT "!RMS_VALID_ARGS!" == "true" (
		echo Invalid argument.
		GOTO :help
	)
	SHIFT
	GOTO :loop
)



IF "%RMS_GUI%" == "true" (
	call %0\..\bin\setup.bat install -g
	pause
	GOTO :end
) ELSE (
	call %0\..\bin\setup.bat install
	GOTO :end
)

:help
echo.
echo Description: Installs Rights Management Server
echo Usage: install [flag]
echo.
echo -s	Run the installer silently
echo -g	Run the installer with a GUI (default^)
echo -h	Display help
echo.		

:end
