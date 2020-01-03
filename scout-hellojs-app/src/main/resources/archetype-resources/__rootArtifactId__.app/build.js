/*
 * Runs 'npm install' and 'npm run build:dev:watch'.
 * A node installation (https://nodejs.org) must be on the PATH for this script to work!
 */

const {spawn} = require('child_process');
const {run} = require('../${simpleArtifactName}.ui/run');

let suffix = process.platform === 'win32' ? '.cmd' : '';
run('npm' + suffix, ['install'])
  .then(() => run('npm' + suffix, ['run', 'build:dev:watch']))
  .catch((code) => {});
