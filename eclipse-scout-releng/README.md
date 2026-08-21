<p align="center">
  <a href="https://eclipse.dev/scout/" target="_blank" rel="noopener noreferrer"><img src="https://eclipsescout.github.io/assets/img/eclipse-scout-logo.svg"></a>
</p>

<p align="center">
  <a href="https://ci.eclipse.org/scout/job/scout-integration-27.1-RT-nightly_pipeline/" target="_blank" rel="noopener noreferrer"><img alt="Jenkins" src="https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.eclipse.org%2Fscout%2Fjob%2Fscout-integration-27.1-RT-nightly_pipeline%2F"></a>
  <a href="https://ci.eclipse.org/scout/job/scout-integration-27.1-RT-nightly_pipeline/" target="_blank" rel="noopener noreferrer"><img alt="Jenkins tests" src="https://img.shields.io/jenkins/tests?jobUrl=https%3A%2F%2Fci.eclipse.org%2Fscout%2Fjob%2Fscout-integration-27.1-RT-nightly_pipeline%2F"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/releng" target="_blank" rel="noopener noreferrer"><img alt="npm" src="https://img.shields.io/npm/dm/@eclipse-scout/releng"></a>
  <a href="https://www.eclipse.org/legal/epl-2.0/" target="_blank" rel="noopener noreferrer"><img alt="NPM" src="https://img.shields.io/npm/l/@eclipse-scout/releng"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/releng" target="_blank" rel="noopener noreferrer"><img alt="npm (scoped)" src="https://img.shields.io/npm/v/@eclipse-scout/releng"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/releng" target="_blank" rel="noopener noreferrer"><img alt="node" src="https://img.shields.io/node/v/@eclipse-scout/releng"></a>
  <a href="https://eclipse.dev/scout/" target="_blank" rel="noopener noreferrer"><img alt="Website" src="https://img.shields.io/website?url=https%3A%2F%2Feclipse.dev%2Fscout%2F"></a>
</p>

# Eclipse Scout - Releng

Contains release engineering scripts for the [Eclipse Scout](https://www.npmjs.com/package/@eclipse-scout/core) framework.

## Deploy

1. Check that there are no *.js files in the module. If there are old build artifacts: remove them first.
2. Update version in `package.json`: set to a unique new version without `-snapshot` suffix.
3. Run tests using npm script `npm run test`.
4. Build using npm script `npm run build`.
5. Deploy to a local registry to test the new package: `npm publish --registry http://127.0.0.1:4873/`.
6. If all is fine: Deploy to real registry: `npm publish` (requires corresponding access token in your `.npmrc`).

## Version Update

1. Align the following dependencies with the ones of the new pnpm version (check version by git checkout of the corresponding tag):
   1. `@pnpm/*`
   2. `realpath-missing` as specified by pnpm
   3. `normalize-path` as specified by pnpm
2. Use the newest compatible version of `typanion` (check version constraint of `@pnpm/npm-lifecycle > @yarnpkg/shell > clipanion` declaring the peer)
3. Update the `engines` in the package.json
4. In this module search for `Inspired by https://github.com/pnpm/pnpm` and check the history of the declared files for changes since the last version. Merge them as necessary.
