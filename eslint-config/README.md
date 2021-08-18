<p align="center">
  <a href="https://www.eclipse.org/scout/" target="_blank" rel="noopener noreferrer"><img src="https://eclipsescout.github.io/assets/img/eclipse-scout-logo.svg"></a>
</p>

<p align="center">
  <a href="https://ci.eclipse.org/scout/view/Scout%20Nightly%20Jobs/job/scout-integration-22.0-RT-nightly_pipeline/" target="_blank" rel="noopener noreferrer"><img alt="Jenkins" src="https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.eclipse.org%2Fscout%2Fview%2FScout%2520Nightly%2520Jobs%2Fjob%2Fscout-integration-22.0-RT-nightly_pipeline%2F"></a>
  <a href="https://ci.eclipse.org/scout/view/Scout%20Nightly%20Jobs/job/scout-integration-22.0-RT-nightly_pipeline/" target="_blank" rel="noopener noreferrer"><img alt="Jenkins tests" src="https://img.shields.io/jenkins/tests?compact_message&jobUrl=https%3A%2F%2Fci.eclipse.org%2Fscout%2Fview%2FScout%2520Nightly%2520Jobs%2Fjob%2Fscout-integration-22.0-RT-nightly_pipeline%2F"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/eslint-config" target="_blank" rel="noopener noreferrer"><img alt="npm" src="https://img.shields.io/npm/dm/@eclipse-scout/eslint-config"></a>
  <a href="https://www.eclipse.org/legal/epl-v10.html" target="_blank" rel="noopener noreferrer"><img alt="NPM" src="https://img.shields.io/npm/l/@eclipse-scout/eslint-config"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/eslint-config" target="_blank" rel="noopener noreferrer"><img alt="npm (scoped)" src="https://img.shields.io/npm/v/@eclipse-scout/eslint-config"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/eslint-config" target="_blank" rel="noopener noreferrer"><img alt="node" src="https://img.shields.io/node/v/@eclipse-scout/eslint-config"></a>
  <a href="https://www.eclipse.org/scout/" target="_blank" rel="noopener noreferrer"><img alt="Website" src="https://img.shields.io/website?url=https%3A%2F%2Fwww.eclipse.org%2Fscout%2F"></a>
</p>

# Eclipse Scout - ESLint

ESLint [shareable config](https://eslint.org/docs/developer-guide/shareable-configs.html) for the [Eclipse Scout](https://www.npmjs.com/package/@eclipse-scout/core) JavaScript style.

## Usage

Once the `@eclipse-scout/eslint-config` package is installed, you can use it by specifying it in the [`extends` section](https://eslint.org/docs/user-guide/configuring#extending-configuration-files) of
your [ESLint configuration](https://eslint.org/docs/user-guide/configuring).

```js
module.exports = {
  extends: '@eclipse-scout',
  rules: {
    // Additional, per-project rules...
  }
}
```

## License

[Eclipse Public License (EPL) v1.0](https://www.eclipse.org/legal/epl-v10.html)
