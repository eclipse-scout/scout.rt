<p align="center">
  <a href="https://www.eclipse.org/scout/" target="_blank" rel="noopener noreferrer"><img src="https://eclipsescout.github.io/assets/img/eclipse-scout-logo.svg"></a>
</p>

<p align="center">
  <a href="https://ci.eclipse.org/scout/view/Scout%20Nightly%20Jobs/job/scout-integration-22.0-RT-nightly_pipeline/" target="_blank" rel="noopener noreferrer"><img alt="Jenkins" src="https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.eclipse.org%2Fscout%2Fview%2FScout%2520Nightly%2520Jobs%2Fjob%2Fscout-integration-22.0-RT-nightly_pipeline%2F"></a>
  <a href="https://ci.eclipse.org/scout/view/Scout%20Nightly%20Jobs/job/scout-integration-22.0-RT-nightly_pipeline/" target="_blank" rel="noopener noreferrer"><img alt="Jenkins tests" src="https://img.shields.io/jenkins/tests?compact_message&jobUrl=https%3A%2F%2Fci.eclipse.org%2Fscout%2Fview%2FScout%2520Nightly%2520Jobs%2Fjob%2Fscout-integration-22.0-RT-nightly_pipeline%2F"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/tsconfig" target="_blank" rel="noopener noreferrer"><img alt="npm" src="https://img.shields.io/npm/dm/@eclipse-scout/tsconfig"></a>
  <a href="https://www.eclipse.org/legal/epl-2.0/" target="_blank" rel="noopener noreferrer"><img alt="NPM" src="https://img.shields.io/npm/l/@eclipse-scout/tsconfig"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/tsconfig" target="_blank" rel="noopener noreferrer"><img alt="npm (scoped)" src="https://img.shields.io/npm/v/@eclipse-scout/tsconfig"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/tsconfig" target="_blank" rel="noopener noreferrer"><img alt="node" src="https://img.shields.io/node/v/@eclipse-scout/tsconfig"></a>
  <a href="https://www.eclipse.org/scout/" target="_blank" rel="noopener noreferrer"><img alt="Website" src="https://img.shields.io/website?url=https%3A%2F%2Fwww.eclipse.org%2Fscout%2F"></a>
</p>

# Eclipse Scout - TSConfig

Base config for TypeScript based Scout projects.

## Usage

Once the `@eclipse-scout/tsconfig` package is installed, you can use it in your `tsconfig.json` by setting the `extends` attribute accordingly.

```json
{
  "extends": "@eclipse-scout/tsconfig/tsconfig.json",
  "compilerOptions": {
    "outDir": "target/dist/d.ts"
  },
  "include": [
    "./src/main/js/**/*"
  ]
}

```
