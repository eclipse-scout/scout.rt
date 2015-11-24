#!/usr/local/bin/bash
BASEDIR=$(dirname $0)
. $BASEDIR/_functions.sh

GIT_USERNAME=
RELEASE="TEST_RELEASE"
TAG=

function usage {
  cat << EOF

	${PRG} [-h] --git_username <EGerritUser> --release <RELEASE> --tag <TAG>

	-h                                - Usage info
	-u | --git_username <EGerritUser> - Eclipse Gerrit Username of Commiter, SSH Key is used for authorisation
	-r | --release <RELEASE>          - <RELEASE> name (Optional / Default: TEST_RELEASE)
	-t | --tag <TAG>                  - <TAG> name (Optional / Default: Project Version)

	Example: ${PRG} -u sleicht -r NIGHTLY

EOF
}

function get_options {
	# Loop until all parameters are used up
	while [ "$1" != "" ]; do
		case $1 in
			-u | --git_username )		shift
										GIT_USERNAME=$1
										;;
			-r | --release )			shift
										RELEASE=$1
										;;
			-t | --tag )				shift
										TAG=$1
										;;
			-h | --help )				usage
										exit 7
										;;
			* )							break;;
		esac
		shift
	done
	_MAVEN_OPTS="$_MAVEN_OPTS $@"
}
get_options $*

if [[ -z  "$GIT_USERNAME" ]]; then
	echo "[ERROR]:       <EGerritUser> missing"
	usage
	exit 7
fi
if [[ "$TAG" ]]; then
	_MAVEN_OPTS="$_MAVEN_OPTS -Dmaster_release_tagName=$TAG"
fi
_MAVEN_OPTS="$_MAVEN_OPTS -e -B"

# Parallel executions of maven modules and tests.
# Half of CPU core are used in to keep other half for OS and other programs.
mvn -Prelease.setversion -Dmaster_release_milestoneVersion=$RELEASE -f org.eclipse.scout.rt -N $_MAVEN_OPTS
processError

$BASEDIR/build.sh -Dmaster_unitTest_failureIgnore=false $_MAVEN_OPTS
processError

mvn -Prelease.checkin -Declipse_gerrit_username=$GIT_USERNAME -f org.eclipse.scout.rt $_MAVEN_OPTS
processError

mvn -Prelease.tag -Declipse_gerrit_username=$GIT_USERNAME -Dmaster_release_pushChanges=true -f org.eclipse.scout.rt $_MAVEN_OPTS
processError

git reset HEAD~1 --hard