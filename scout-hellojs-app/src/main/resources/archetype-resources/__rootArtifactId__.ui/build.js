/*
 * Runs 'npm install'.
 * A node installation (https://nodejs.org) must be on the PATH for this script to work!
 */

const {spawn} = require('child_process');
const {run} = require('./run');

let suffix = process.platform === 'win32' ? '.cmd' : '';
run('npm' + suffix, ['install'])
  .catch(code => {});
