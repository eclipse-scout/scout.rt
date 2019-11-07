/*
 * Runs 'npm install' and 'npm run build:dev'.
 * A node installation (https://nodejs.org) must be on the PATH for this script to work!
 */

const { spawn } = require('child_process');

function run(cmd, args) {
  const promise = new Promise((resolve, reject) => {
    const child = spawn(cmd, args);
    child.stdout.setEncoding('utf8');
    child.stdout.on('data', chunk => console.log(chunk.trim()));
    child.stderr.setEncoding('utf8');
    child.stderr.on('data', chunk => console.log(chunk.trim()));
    child.on('close', code => {
    	console.log(`child process exited with code ${code}`);
    	code === 0 ? resolve(0) : reject(code);
    });
    return child;
  });
  return promise;
}

function installAndBuild() {
  let suffix = process.platform === 'win32' ? '.cmd' : '';
  run('npm' + suffix, ['install'])
  .then(() => run('npm' + suffix, ['run', 'build:dev']));
}

installAndBuild();
