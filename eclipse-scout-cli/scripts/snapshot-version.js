/*
Creates a snapshot version by using the prerelease tag snapshot and appending the current timestamp to the version of the npm package.
The package.json is updated with the new version and the package is ready to be published.
After the publish you should revert the package.json to its initial state.
 */
const path = require('path');
const packageJson = require(path.resolve('./package.json'));

let timestamp = new Date().toISOString(); // UTC

// Create a string with the pattern yyyyMMddHHmmss
timestamp = timestamp.replace(/[-:.TZ]/g, '');
timestamp = timestamp.substr(0, timestamp.length - 3);

const oldVersion = packageJson.version;
const cleanedVersion = oldVersion.replace(/-snapshot(.)*/i, '');
const newVersion = `${cleanedVersion}-snapshot.${timestamp}`;

const npmlog = require('npm/node_modules/npmlog');
const npm = require('npm');

npmlog.on('log', msg => {
  const {level} = msg;
  if (level === 'info' || level === 'error' || level === 'warn') {
    console.log({msg});
  }
});

npm.load({
  'loaded': false,
  'progress': false,
  'no-audit': true
}, err => {
  // handle errors
  if (err) {
    console.error(err);
  }

  // install module
  npm.commands.version([newVersion], (er, data) => {
    if (err) {
      console.error(err);
    }
    // log errors or data
    if (data) {
      console.log('done');
    }
  });

  npm.on('log', message => {
    // log installation progress
    console.log(message);
  });
});
console.log(`New Version: ${newVersion}`);
