#!/bin/bash

# Below are the config options used by the script.
PRG=$(basename "${0}")

function processError {
	SED_OUT=$?
	if [ $SED_OUT -ne 0 ]; then
		echo "EXIT"$SED_OUT
		exit $SED_OUT
	fi
}
