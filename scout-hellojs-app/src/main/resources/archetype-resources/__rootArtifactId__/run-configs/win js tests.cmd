@echo off
:: This script starts the testserver and executes all JavaScript tests. It expects that npm install has already been executed previously.
::
:: To make this script work you need a current version of Node.js (>=20.10.0), npm (>=10.2.3) and pnpm (>=8.11.0).
:: Node.js (incl. npm) is available here: https://nodejs.org/.

:: Check if npm is available
where npm >nul 2>nul
if %errorlevel% neq 0 (
  echo npm cannot be found. Make sure Node.js is installed and the PATH variable correctly set. See the content of this script for details. 1>&2
  exit /b 1
)

:: Execute the tests
echo Running 'testserver:start'
call npm run testserver:start
if %errorlevel% neq 0 exit /b %errorlevel%
