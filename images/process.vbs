Dim shell: Set shell = CreateObject("WScript.Shell")
Dim fso: Set fso = CreateObject("Scripting.FileSystemObject")
Dim folder: Set folder = fso.GetFolder(".")
resPath = getResFolder(folder)
WScript.Echo "Res folder: " & resPath

Dim resFolder: Set resFolder = fso.GetFolder(resPath)


WScript.Echo "Working folder: " & folder
WScript.Echo "Res folder: " & resFolder

 
Call processFiles(folder)
Call crush()

Function getResFolder(folder)
	projectFolderPath = fso.GetParentFolderName(folder)
	Dim projectFolder: Set projectFolder = fso.getFolder(fso.GetParentFolderName(folder))
	getResFolder = projectFolderPath & "\app\src\main\res"
End Function

Function processFiles(folder)
	WScript.Echo "Image Folder: " & imageFolder
	Dim files: Set files = folder.Files
	startTime = Now()
	For each fileItem In files
		strExtension = fso.getExtensionName(fileItem)
		If InStr(UCase(fileItem.Name), ".SVG") Then 
			minuteCount = DateDiff("n", fileItem.DateLastModified, startTime) 	
			If minuteCount < 10 Then 
				WScript.Echo minuteCount & "-->"  & fileItem
				processFile(fileItem)
 			End If
		End IF
	Next
End Function

Function crush()
    For Each subfolder in resFolder.SubFolders
  ''      WScript.Echo subfolder.Path
		For Each filePath in subfolder.Files
			If InStr(UCase(filePath.Name), ".PNG") Then 
				minuteCount = DateDiff("n", filePath.DateLastModified, startTime) 	
				If minuteCount < 10 Then 
					strCommand = "pngquant.exe " &  filePath & " --force -v --ext .png"
					WScript.Echo strCommand
					Set objExecObject = shell.Exec(strCommand)
					strText = ""
					Do While Not objExecObject.StdOut.AtEndOfStream
						strText = strText & objExecObject.StdOut.ReadLine()
					Loop

					WScript.Echo strText

					strCommand = "pngout.exe " & filePath
					WScript.Echo strCommand
					Set objExecObject = shell.Exec(strCommand)
					strText = ""
					Do While Not objExecObject.StdOut.AtEndOfStream
						strText = strText & objExecObject.StdOut.ReadLine()
					Loop
					WScript.Echo strText
				End If
			End If
		Next
    Next
End Function

Function processFile(file) 
	processType = getProcessType(file)
	if (processType = "NORMAL") Then
		Dim sizes: Set sizes = getIconSizes(file)
		For Each elem in sizes
			Call process(file, elem, elem, sizes.Item(elem))
		Next 
	ElseIF (processType = "LAUNCHER") Then
		Dim launcherSizes: Set launcherSizes = getLauncherIconSizes(file)
		For Each elem in launcherSizes
			Call process(file, elem, elem, launcherSizes.Item(elem))
		Next 
	ElseIF (processType = "HALF") Then
		Dim halfSizes: Set halfSizes = getHalfIconSizes(file)
		For Each elem in halfSizes
			Call process(file, elem, elem, halfSizes.Item(elem))
		Next 
	ElseIF (processType = "QUARTER") Then
		Dim quarterSizes: Set quarterSizes = getQuarterIconSizes(file)
		For Each elem in quarterSizes
			Call process(file, elem, elem, quarterSizes.Item(elem))
		Next 
	ElseIF (processType = "FIFTH") Then
		Dim fifthSizes: Set fifthSizes = getFifthIconSizes(file)
		For Each elem in fifthSizes
			Call process(file, elem, elem, fifthSizes.Item(elem))
		Next 
	ElseIF (processType = "MENU") Then
		Dim menuSizes: Set menuSizes = getMenuIconSizes(file)
		For Each elem in menuSizes
			Call process(file, elem, elem, menuSizes.Item(elem))
		Next 
	ElseIF (processType = "SMALL") Then
		Dim smallSizes: Set smallSizes = getSmallIconSizes(file)
		For Each elem in smallSizes
			Call process(file, elem, elem, smallSizes.Item(elem))
		Next 
	ElseIF (processType = "TABLET") Then
		Dim tabletSizes: Set tabletSizes = getTabletIconSizes(file)
		For Each elem in tabletSizes
			Call process(file, elem, elem, tabletSizes.Item(elem))
		Next 
	End If
End Function

Function process(file, width, height, directory)
	strCommand = "process_files.bat " & fso.GetBaseName(file) & ", " & width & ", " & height & ", " & resFolder & "\" & directory
    WScript.Echo(strCommand)
	shell.run strCommand, 0, True
End Function

Function process2(file, width, height, directory)
	directoryPath = resPath & "\" & directory
	strCommand = """C:\Program Files\inkscape\inkscape.exe"" " & file.Path & " -z -e -w " & width & " -h " & height & " --export-dpi=1200 --export-area-drawing --export-png=" & directoryPath & "\" & file.Name
    WScript.Echo(strCommand)
	shell.run strCommand, 0, True
	strCommand = "mogrify   " & file.Path & " -background none -gravity Center -extent  " & width & "x" & height & " " & file.Path
    WScript.Echo(strCommand)
	shell.run strCommand, 0, True
End Function

Function getProcessType(fileItem) 
	processType = "NORMAL"
	strExtension = fso.getExtensionName(fileItem)
	If InStr(UCase(fileItem.Name), ".SVG") Then 	
		WScript.Echo("Normal...." & fileItem.Name)
		WScript.Echo(fileItem.Name)
	End IF
	If InStr(UCase(fileItem.Name), "IC_LAUNCHER") Then 	
		processType = "LAUNCHER"
		WScript.Echo(fileItem.Name)
	End IF
	If InStr(UCase(fileItem.Name), "_HALF.SVG") Then 	
		processType = "HALF"
		WScript.Echo("Half size file...." & fileItem.Name)
	End IF
	If InStr(UCase(fileItem.Name), "_HALF_SELECTED.SVG") Then 	
		processType = "HALF"
		WScript.Echo("Half size file...." & fileItem.Name)
	End IF
	If InStr(UCase(fileItem.Name), "_QUARTER.SVG") Then 	
		processType = "QUARTER"
		WScript.Echo("QUARTER size file...." & fileItem.Name)
	End IF
	If InStr(UCase(fileItem.Name), "_FIFTH.SVG") Then 	
		processType = "FIFTH"
		WScript.Echo("FIFTH size file...." & fileItem.Name)
	End IF
	If InStr(UCase(fileItem.Name), "_QUARTER_SELECTED.SVG") Then 	
		processType = "QUARTER"
		WScript.Echo("QUARTER size file...." & fileItem.Name)
	End IF
	If InStr(UCase(fileItem.Name), "IC_MENU_") Then 	
		processType = "MENU"
		WScript.Echo("MENU size file...." & fileItem.Name)
	End IF
	If InStr(UCase(fileItem.Name), "_SMALL.SVG") Then 	
		processType = "SMALL"
		WScript.Echo("SMALL size file...." & fileItem.Name)
	End IF
	If InStr(UCase(fileItem.Name), "_TABLET.SVG") Then 	
		processType = "TABLET"
		WScript.Echo("TABLET size file...." & fileItem.Name)
	End IF
	getProcessType = processType
End Function

Function getFullIconSizes(file)
	Dim list: Set list = CreateObject("Scripting.Dictionary")
	startSize = 320
	Dim mapping: Set mapping = scaleRatios()
	For Each elem in mapping
		WScript.Echo "Calculation: " & startSize * mapping.Item(elem) & "  " & elem	
		Call process(file, startSize * mapping.Item(elem), startSize * mapping.Item(elem), elem)
	Next 
	Set getFullIconSizes = list
End Function

Function getHalfIconSizes(file)
	Dim list: Set list = CreateObject("Scripting.Dictionary")
	startSize = 140
	Dim mapping: Set mapping = scaleRatios()
	For Each elem in mapping
		WScript.Echo "Calculation: " & startSize * mapping.Item(elem) & "  " & elem	
		Call process(file, startSize * mapping.Item(elem), startSize * mapping.Item(elem), elem)
	Next 
	Set getHalfIconSizes = list
End Function

Function getQuarterIconSizes(file)
	Dim list: Set list = CreateObject("Scripting.Dictionary")
	startSize = 75
	Dim mapping: Set mapping = scaleRatios()
	For Each elem in mapping
		WScript.Echo "Calculation: " & startSize * mapping.Item(elem) & "  " & elem	
		Call process(file, startSize * mapping.Item(elem), startSize * mapping.Item(elem), elem)
	Next 
	Set getQuarterIconSizes = list
End Function

Function getFifthIconSizes(file)
	Dim list: Set list = CreateObject("Scripting.Dictionary")
	startSize = 70
	Dim mapping: Set mapping = scaleRatios()
	For Each elem in mapping
		WScript.Echo "Calculation: " & startSize * mapping.Item(elem) & "  " & elem	
		Call process(file, startSize * mapping.Item(elem), startSize * mapping.Item(elem), elem)
	Next 
	Set getFifthIconSizes = list
End Function

Function getLauncherIconSizes(file)
	Dim list: Set list = CreateObject("Scripting.Dictionary")
	list.Add "192", "mipmap-xxxhdpi"
	list.Add "142", "mipmap-xxhdpi"
	list.Add "96", "mipmap-xhdpi"
	list.Add "72", "mipmap-hdpi"
	list.Add "48", "mipmap-mdpi"
	Set getLauncherIconSizes = list
End Function



Function getIconSizes(file)
	Dim list: Set list = CreateObject("Scripting.Dictionary")
	list.Add "192", "drawable-xxhdpi"
	list.Add "142", "drawable-xxhdpi"
	list.Add "96", "drawable-xhdpi"
	list.Add "72", "drawable-hdpi"
	list.Add "48", "drawable-mdpi"
	Set getIconSizes = list
End Function

Function getMenuIconSizes(file)
	Dim list: Set list = CreateObject("Scripting.Dictionary")
	list.Add "96", "drawable-xxhdpi"
	list.Add "72", "drawable-xxhdpi"
	list.Add "48", "drawable-xhdpi"
	list.Add "36", "drawable-hdpi"
	list.Add "24", "drawable-mdpi"
	Set getMenuIconSizes = list
End Function

Function getSmallIconSizes(file)
	Dim list: Set list = CreateObject("Scripting.Dictionary")
	list.Add "96", "drawable-xxhdpi"
	list.Add "72", "drawable-xxhdpi"
	list.Add "48", "drawable-xhdpi"
	list.Add "36", "drawable-hdpi"
	list.Add "24", "drawable-mdpi"
	Set getSmallIconSizes = list
End Function

Function scaleRatios()
	Dim list: Set list = CreateObject("Scripting.Dictionary")
	list.Add    "drawable-sw360dp-mdpi"   , 1.125
	list.Add    "drawable-sw360dp-hdpi"   , 1.6875
	list.Add    "drawable-sw360dp-xhdpi"   , 2.25
	list.Add    "drawable-sw360dp-xxhdpi"   , 3.375
	list.Add    "drawable-sw480dp-mdpi"   , 1.5
	list.Add    "drawable-sw480dp-hdpi"   , 2.25
	list.Add    "drawable-sw480dp-xhdpi"   , 3
	list.Add    "drawable-sw480dp-xxhdpi"   , 4.5
	list.Add    "drawable-sw600dp-mdpi"   , 1.875
	list.Add    "drawable-sw600dp-hdpi"   , 2.8125
	list.Add    "drawable-sw600dp-xhdpi"   , 3.75	
	list.Add    "drawable-sw600dp-xxhdpi"   , 5.625
	list.Add    "drawable-sw720dp-mdpi"   , 2.25
	list.Add    "drawable-sw720dp-hdpi"   , 3.375
	list.Add    "drawable-sw720dp-xhdpi"   , 4.5
	list.Add    "drawable-sw720dp-xxhdpi"   , 6.75
	Set scaleRatios = list
End Function

'' depreciated
Function getTabletIconSizes(file)
	Dim list: Set list = CreateObject("Scripting.Dictionary")
	list.Add  140, "drawable-sw720dp-mdpi"
	list.Add  150, "drawable-sw720dp-hdpi"
	list.Add  170, "drawable-sw720dp-xhdpi"
	list.Add  200, "drawable-sw720dp-xxhdpi"
	Set getTabletIconSizes = list
End Function

	