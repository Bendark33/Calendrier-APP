# Version Windows (PowerShell) : copie les fichiers du site dans "www".
if (Test-Path www) { Remove-Item -Recurse -Force www }
New-Item -ItemType Directory www | Out-Null
Copy-Item ..\index.html, ..\manifest.webmanifest, ..\sw.js, ..\icon-192.png, ..\icon-512.png www\
