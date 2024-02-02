<p align="center">
  <a href="https://www.eclipse.org/scout/" target="_blank" rel="noopener noreferrer"><img src="https://eclipsescout.github.io/assets/img/eclipse-scout-logo.svg"></a>
</p>

<p align="center">
  <a href="https://ci.eclipse.org/scout/view/Scout%20Nightly%20Jobs/job/scout-integration-22.0-RT-nightly_pipeline/" target="_blank" rel="noopener noreferrer"><img alt="Jenkins" src="https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.eclipse.org%2Fscout%2Fview%2FScout%2520Nightly%2520Jobs%2Fjob%2Fscout-integration-22.0-RT-nightly_pipeline%2F"></a>
  <a href="https://ci.eclipse.org/scout/view/Scout%20Nightly%20Jobs/job/scout-integration-22.0-RT-nightly_pipeline/" target="_blank" rel="noopener noreferrer"><img alt="Jenkins tests" src="https://img.shields.io/jenkins/tests?compact_message&jobUrl=https%3A%2F%2Fci.eclipse.org%2Fscout%2Fview%2FScout%2520Nightly%2520Jobs%2Fjob%2Fscout-integration-22.0-RT-nightly_pipeline%2F"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/migrate" target="_blank" rel="noopener noreferrer"><img alt="npm" src="https://img.shields.io/npm/dm/@eclipse-scout/migrate"></a>
  <a href="https://www.eclipse.org/legal/epl-2.0/" target="_blank" rel="noopener noreferrer"><img alt="NPM" src="https://img.shields.io/npm/l/@eclipse-scout/migrate"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/migrate" target="_blank" rel="noopener noreferrer"><img alt="npm (scoped)" src="https://img.shields.io/npm/v/@eclipse-scout/migrate"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/migrate" target="_blank" rel="noopener noreferrer"><img alt="node" src="https://img.shields.io/node/v/@eclipse-scout/migrate"></a>
  <a href="https://www.eclipse.org/scout/" target="_blank" rel="noopener noreferrer"><img alt="Website" src="https://img.shields.io/website?url=https%3A%2F%2Fwww.eclipse.org%2Fscout%2F"></a>
</p>

# Eclipse Scout - Migrate

Migration tool to help migrating the JavaScript/TypeScript code from one Scout release to another.

## Usage

To set up the tool, do the following:

- Open the `package.json` of your ui module (e.g. `your.project.ui.html`).
- Add a devDependency to `@eclipse-scout/migrate`.
- Run `pnpm install` to install the new dependency.
- Do the required migration as described in the following chapters.
- When you are finished, remove the added dependency and migration script from you `package.json`.

```json
{
  "devDependencies": {
    "@eclipse-scout/migrate": "~24.2.0"
  }
}
```

### TSConfig

Some migrations require a `tsconfig.json`. You can add it as follows (do it only if stated in the migration description):

- Add a devDependency to `@eclipse-scout/tsconfig`.
- Run `pnpm install` to install the new dependency.
- Create a `tsconfig.json` beside your `package.json` according to the `Readme.md` of the `@eclipse-scout/tsconfig` module.

```json
{
  "devDependencies": {
    "@eclipse-scout/tsconfig": "~24.2.0"
  }
}
```

### Module Map

The module map is used by certain migration tasks to automatically add imports.
If a migration task requires a module map, do the following:

- Adjust the `moduleMap` argument by replacing `yourNamespace` with the namespace in your `index.js` file.
  The moduleMap entry is used to add imports to your `index.js` automatically. If you omit it, you need to add the imports manually.
- If you have dependencies to other Scout based modules, you add them to the `moduleMap` to have the imports to these modules created automatically.
  Add the namespace of the other module and the name of the npm package as follows: `--moduleMap.otherNamespace @other/npm-module`

## ObjectType and MenuType Migration

In order to migrate string literals to class references and menu type constants, do the following:

- Add a script in your `package.json` as described below.
- Adjust the module map as described in the chapter [Module Map](#module-map)
- Run the `migrate` script by pressing the play button next to it in IntelliJ or using the command line: `npm run migrate`
- Review the changes and add the missing imports, if there are any.

```json
{
  "scripts": {
    "migrate": "scout-migrate --migrate objectType menuTypes --sources src/main/js/**/*.js --moduleMap.yourNamespace path:src/main/js/index.js"
  }
}
```

## Widget and Column Map Migration

In order to create widget and column maps, do the following:

- Create a `tsconfig.json` as described [here](#tsconfig)
- Add a rename and a migrate script in your `package.json` as described below.
- Run the `rename` script by pressing the play button next to it in IntelliJ or using the command line: `npm run rename`.
  Commit the changes to ensure Git can track the renames correctly.
- Run the `migrate` script.
- Review and commit the changes.

```json
{
  "scripts": {
    "rename": "scout-migrate --rename --sources src/main/js/**/*Model.js",
    "migrate": "scout-migrate --migrate widgetColumnMap --sources src/main/js/**/*Model.ts"
  }
}
```

## Migrate to TypeScript

If you plan to migrate your Scout JS code base to TypeScript, use the migration tool for the initial migration.
From there, you need to add the missing types, fix errors and clean up your code.

**Important**: if you have dependencies to other Scout based modules, make sure these modules are migrated to TypeScript first.
We don't recommend migrating to TypeScript unless all dependencies are migrated.

The automatic migration includes:

- Renaming the files to `*.ts`.
- Declaring class properties
- Declaring method parameters and return types based on JsDoc and naming conventions
- Removing types from JsDoc
- Adding imports for the types based on module maps
- Adding method accessors

In order to run the TypeScript migration, do the following:

- Ensure you have a clean Git working tree (no uncommitted files).
- Create a `tsconfig.json` as described [here](#tsconfig)
- Add scripts in your `package.json` as described below.
- Add an `override` in your `package.json` to fix the TypeScript version to 5.3.2 (the migrate tool will skip some migrations and print a warning about the wrong version otherwise). This is only needed for the migration and has to be
  removed afterwards.
- Adjust the module map as described in the chapter [Module Map](#module-map)
- Add custom type maps if you have a large code base and would like to automate a little more, see [Type Map](#type-map)
- Run the `rename` script by pressing the play button next to it in IntelliJ or using the command line: `npm run rename`.
  - Adjust the paths in `package.json` (to `index.ts`), in `webpack.config.js` (to your entry point file) and in `karma.conf.js` (to `test-index.ts`).
  - Open `index.ts` and replace `import * as self from './index.js';` with `import * as self from './index'`;
  - Commit the changes to ensure Git can track the renames correctly.
- Run the `migrate` script.
- Review every file:
  - Verify / add the types of the class properties.
  - Verify / add the types of the method parameters and return values.
- If you have custom widgets, you should create a `Model` and an `EventMap` for each widget.
  Please use existing widgets as template, e.g. `Menu.ts`, `MenuModel.ts`, `MenuEventMap.ts`.
  - The model needs to be extracted manually.
    Copy the relevant class properties to a separate model file.
    In your widget, implement the model interface and declare a model variable.
  - To create the event maps, you can run the `event-maps` script from below and copy the result into a separate event maps file.
- If you use third party libraries, you may want to check if they provide types and add them to the `devDependencies` of your `package.json` (e.g. @types/jquery).
  See also [Project Setup for TypeScript](https://eclipsescout.github.io/scout-docs/stable/technical-guide/user-interface/typescript.html#project-setup-for-typescript)
- Remove the added `scripts`, `overrides` and dependency to `@eclipse-scout/migrate`.

```json
{
  "scripts": {
    "rename": "scout-migrate --rename --sources src/main/js/**/*.js",
    "migrate": "scout-migrate --rename false --migrate ts --sources src/main/js/**/*.ts --moduleMap.yourNamespace path:src/main/js/index.ts",
    "event-maps": "scout-migrate --print-event-maps --sources src/main/js/**/*.ts"
  },
  "pnpm": {
    "overrides": {
      "typescript": "5.3.2"
    }
  }
}
```

### Type Map

Type maps are used to add types automatically based on naming rules.
Scout provides many rules out of the box that should detect many types.
If you have a large code base with custom types, you can add custom type maps.

To do so, create a config file ending with `.mjs`, e.g. `migrate-config.mjs` and configure your type maps like in the following example:

```js
// The key has no special meaning
// The `name` is the name of the member variable or method parameter.
// If the predicate returns true, the type is used.
// The namespace in the type is used to create the import. To make it work, the module map has to contain an entry for that namespace.
// Example: doSomething(opts) -> doSomething(opts: Options)
const typeMap = {
  Options: {
    predicate: name => name === 'opts',
    type: 'yourNamespace.Options'
  }
};

// Compared to the typeMap above, the returnTypeMap is only used to determine the return types of functions.
// The 'name' parameter is the method name
// Example: createOptions() -> createOptions(): Options
const returnTypeMap = {
  Options: {
    predicate: name => name === 'createOptions',
    type: 'yourNamespace.Options'
  }
};

// Necessary if the imports should be created automatically.
const moduleMap = {
  yourNamespace: '@your-module/core'
};

// The default type that will be used if the parameter has no type.
// Can for example be set to 'any' after the migration to check whether all parameters have been typed.
let defaultParamType;

// The default type that will be used if the function has no return type.
// Can for example be set to 'void' after the migration to check whether all parameters have been typed.
let defaultReturnType;

export {typeMap, returnTypeMap, moduleMap, defaultParamType, defaultReturnType};
```

You can add as many mappings as you like, or none at all, they are completely optional.
To activate your config, you need to pass the config object to the migrate script by using the `--config` option.
For the TypeScript migration, you can do it as follows:

```json
{
  "scripts": {
    "migrate": "scout-migrate --rename false --migrate ts --sources src/main/js/**/*.ts --config migrate-config.mjs"
  }
}
```

**Note**: you can either define the module map in the config file or pass it ass arguments to the script.
If you do both, they will be merged. If they contain the same keys, the value passed via argument wins.
