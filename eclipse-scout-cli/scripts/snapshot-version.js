/*
Creates a snapshot version by using the prerelease tag snapshot and appending the current timestamp to the version of the npm package.
The package.json is updated with the new version and the package is ready to be published.
After the publish you should revert the package.json to its initial state.
 */

const generateSnapshotVersion = async() => {
  const path = require('path');
  const filename = './package.json';
  const packageJson = require(path.resolve(filename));

  let timestamp = new Date().toISOString(); // UTC

  // Create a string with the pattern yyyyMMddHHmmss
  timestamp = timestamp.replace(/[-:.TZ]/g, '');
  timestamp = timestamp.substr(0, timestamp.length - 3);

  const oldVersion = packageJson.version;
  const cleanedVersion = oldVersion.replace(/-snapshot(.)*/i, '');
  const newVersion = `${cleanedVersion}-snapshot.${timestamp}`;

  console.log(`New Version: ${newVersion}`);
  packageJson.version = newVersion;

  const fsp = require('fs').promises;
  await fsp.writeFile(filename, JSON.stringify(packageJson, null, 2));
};

generateSnapshotVersion()
  .then(() => console.log('snapshot version done'))
  .catch(e => {
    console.error('snapshot version failed');
    console.error(e);
    process.exit(1);
  });
