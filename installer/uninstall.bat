@ECHO OFF

SET RMS_GUI=false
IF "%1" == "" SET RMS_GUI=true
IF "%1" == "-g" SET RMS_GUI=true
IF "%RMS_GUI%" == "true" (
	call %0\..\bin\setup.bat uninstall -g
	pause
) ELSE (
	IF "%1" == "-s" (
		call %0\..\bin\setup.bat uninstall
	) ELSE (
		IF NOT "%1" == "-h" (
			echo Invalid argument.
		)
		echo.
		echo Description: Uninstalls Rights Management Server
		echo Usage: uninstall [flag]
		echo.
		echo -s	Run the uninstaller silently
		echo -g	Run the uninstaller with a GUI (default^)
		echo -h	Display help
    )
)