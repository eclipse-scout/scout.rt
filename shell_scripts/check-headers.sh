#!/bin/bash
# Check correct copyright headers

mvn license:check -Dlicense_check -f org.eclipse.scout.rt $*
