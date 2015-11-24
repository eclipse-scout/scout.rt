#!/bin/bash
# Check correct copyright headers

mvn license:check -Dlicense_check $*
