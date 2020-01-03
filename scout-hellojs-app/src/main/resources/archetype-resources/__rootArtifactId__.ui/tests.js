/*
 * Runs 'npm install' and 'npm run testserver:start'.
 * A node installation (https://nodejs.org) must be on the PATH for this script to work!
 */

const {spawn} = require('child_process');
const {run} = require('./run');

let suffix = process.platform === 'win32' ? '.cmd' : '';
run('npm' + suffix, ['install'])
  .then(() => run('npm' + suffix, ['run', 'testserver:start']))
  .catch(code => {});
