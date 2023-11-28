@echo off

:: This script installs all the JS dependencies and builds the JavaScript and CSS bundles.
:: It also starts a watcher which triggers a rebuild of these bundles whenever JS or CSS code changes.
::
:: It has to be run once before the UI server is started.
:: You need to rerun it if you update your JS dependencies (package.json).
:: Please see the Scout documentation for details about the available run scripts: https://eclipsescout.github.io/scout-docs/24.1/technical-guide/user-interface/build-stack.html#command-line-interface-cli
::
:: To make this script work you need a current version of Node.js (>=20.10.0), npm (>=10.2.3) and pnpm (>=8.11.0).
:: Node.js (incl. npm) is available here: https://nodejs.org/.
:: pnpm is available here: https://pnpm.io/

:: Check if node is available
where node >nul 2>nul
if %errorlevel% neq 0 (
  echo node cannot be found. Make sure Node.js is installed and the PATH variable correctly set. See the content of this script for details. 1>&2
  exit /b 1
)

:: Check if pnpm is available
where pnpm >nul 2>nul
if %errorlevel% neq 0 (
  echo pnpm cannot be found. Make sure pnpm is installed. See the content of this script for details. 1>&2
  exit /b 1
)

:: Install all JavaScript dependencies => creates the node_modules folders
cd ..
echo Running 'pnpm install' in %cd%
call pnpm install --ignore-scripts
if %errorlevel% neq 0 exit /b %errorlevel%
echo pnpm install finished successfully!
echo.

:: Build the JavaScript and CSS bundles and start the watcher => creates the dist folder
cd ${rootArtifactId}.app
echo Running 'pnpm build:dev:watch'
call pnpm run build:dev:watch
if %errorlevel% neq 0 exit /b %errorlevel%
