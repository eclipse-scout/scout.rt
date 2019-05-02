/*
Creates a snapshot version by using the prerelease tag snapshot and appending the current timestamp to the version of the npm package.
The package.json is updated with the new version and the package is ready to be published.
After the publish you should revert the package.json to its initial state.
 */
const path = require('path');
const packageJson = require('../package');
const exec = require('child_process').exec;

let nodePath = process.argv[2]; // optional input: folder containing node executable
let timestamp = new Date().toISOString();

// Create a string with the pattern yyyyMMddHHmmss
timestamp = timestamp.replace(/[-:.TZ]/g, '');
timestamp = timestamp.substr(0, timestamp.length - 3);
let newVersion = packageJson.version + '-snapshot.' + timestamp;
console.log(`New Version: ${newVersion}`);

let cmd = '';
if (nodePath) {
  console.log(`running npm version with node executable in '${nodePath}'.`);
  cmd = '"' + path.resolve(nodePath, 'node') + '" "' + path.resolve(nodePath, 'node_modules', 'npm', 'bin', 'npm-cli.js') + '" version ' + newVersion;
} else {
  console.log(`running npm version with npm executable on PATH`);
  cmd = 'npm version ' + newVersion;
}
console.log(`COMMAND TO BE EXECUTED: ${cmd}`);

exec(cmd, (error, stdout, stderr) => {
  if (error) {
    throw error;
  }
  console.log(stdout);
  console.error(stderr);
});

