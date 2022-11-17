#!/bin/sh

#
# Copyright (c) 2010-2022 BSI Business Systems Integration AG.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     BSI Business Systems Integration AG - initial API and implementation
#
# This script starts the testserver and executes all JavaScript tests. It expects that npm install has already been executed previously.
#
# To make this script work you need a current version of Node.js (>=18.12.1), npm (>=9.1.1) and pnpm (>=7.16.0).
# Node.js (incl. npm) is available here: https://nodejs.org/.

# Abort the script if any command fails
set -e

# Specify the path to the node and npm binaries
PATH=$PATH:/usr/local/bin

# Check if node is available
command -v node >/dev/null 2>&1 || { echo >&2 "node cannot be found. Make sure Node.js is installed and the PATH variable correctly set. See the content of this script for details."; exit 1; }

# Execute the tests
echo "Running 'npm testserver:start'"
npm run testserver:start
echo "npm testserver:start finished successfully!"
