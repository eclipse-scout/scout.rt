/*
Creates a snapshot version by using the prerelease tag snapshot and appending the current timestamp to the version of the npm package.
The package.json is updated with the new version and the package is ready to be published.
After the publish you should revert the package.json to its initial state.
 */

let version = process.env.npm_package_version;
let timestamp = new Date().toISOString();

// Create a string with the pattern yyyyMMddHHmmss
timestamp = timestamp.replace(/[-:.TZ]/g,'');
timestamp = timestamp.substr(0, timestamp.length - 3);
let newVersion = version + '-snapshot.' + timestamp;

const exec = require('child_process').exec;
exec('npm version ' + newVersion);

console.log(newVersion);