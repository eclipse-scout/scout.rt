#!/usr/bin/env node

import {createRequire} from 'module';
import path from 'path';
import fs from 'fs';
import typescript from 'typescript';
import {eslintFixPlugin, jsDocPlugin} from 'ts-migrate-plugins';
import {migrate, MigrateConfig} from 'ts-migrate-server';
import renameModule from 'ts-migrate/build/commands/rename.js';
import parser from 'yargs-parser';
import convertToCRLFPlugin from '../src/convertToCRLFPlugin.js';
import memberAccessModifierPlugin from '../src/memberAccessModifierPlugin.js';
import convertToLFPlugin from '../src/convertToLFPlugin.js';
import declareMissingClassPropertiesPlugin from '../src/declareMissingClassPropertiesPlugin.js';
import methodsPlugin from '../src/methodsPlugin.js';
import countMethodsPlugin from '../src/countMethodsPlugin.js';
import printEventMapsPlugin from '../src/printEventMapsPlugin.js';
import typedObjectTypePlugin from '../src/typedObjectTypePlugin.js';
import widgetColumnMapPlugin from '../src/widgetColumnMapPlugin.js';
import menuTypesPlugin from '../src/menuTypesPlugin.js';

const rename = renameModule.default; // Default imports don't work as expected when importing from cjs modules

const yargsOptions = {
  boolean: ['count', 'rename', 'printEventMaps'],
  array: ['sources', 'migrate'],
  string: ['moduleMap', 'jsDocTypeMap', 'paramTypeMap', 'returnTypeMap', 'menuTypesMap', 'config'],
  default: {'migrate': [], 'rename': null, 'jsDocTypeMap': {}, 'count': false, 'printEventMaps': false},
  choices: ['migrate', ['ts', 'objectType', 'widgetColumnMap', 'menuTypes']]
};
const args = parser(process.argv, yargsOptions);

let sources = args.sources;
if (sources) {
  console.log('Processing sources: ' + sources);
} else if (!tsConfigContainsSources()) {
  console.warn('Please set sources argument or provide a tsconfig.json specifying the sources.');
  process.exit();
}

const rootDir = path.resolve(process.cwd());
let renameToTs = args.rename;
if (args.rename === null) {
  // Rename if migrate to ts is enabled and renaming not explicitly disabled
  renameToTs = args.migrate.includes('ts');
}
if (renameToTs) {
  rename({rootDir, sources});
}
if (!args.migrate.length && !args.count && !args.printEventMaps) {
  process.exit();
}

let config = await readConfig();
let moduleMap = Object.assign({}, config.moduleMap, args.moduleMap);
if (moduleMap) {
  for (let [key, value] of Object.entries(moduleMap)) {
    if (Array.isArray(value)) {
      moduleMap[key] = value[value.length - 1]; // If same namespace was passed multiple times, use the last provided
    }
  }
  console.log('Using moduleMap: ' + JSON.stringify(moduleMap));
}

let menuTypesMap = Object.assign({}, config.menuTypesMap, args.menuTypesMap);
if (menuTypesMap) {
  console.log('Using menuTypesMap: ' + JSON.stringify(menuTypesMap));
}

let tsConfigDir;
if (!fs.existsSync('./tsconfig.json')) {
  // https://errorsandanswers.com/do-require-resolve-for-es-modules/
  const require = createRequire(import.meta.url);
  tsConfigDir = require.resolve('@eclipse-scout/tsconfig').replace('tsconfig.json', '');
}

const migrateConfig = new MigrateConfig();
if (args.migrate.length > 0) {
  console.log('Preparing migrations ' + args.migrate);
  migrateConfig.addPlugin(convertToCRLFPlugin, {});
  if (args.migrate.includes('objectType')) {
    migrateConfig.addPlugin(typedObjectTypePlugin, {moduleMap});
  }
  if (args.migrate.includes('menuTypes')) {
    migrateConfig.addPlugin(menuTypesPlugin, {menuTypesMap, moduleMap});
  }
  if (args.migrate.includes('widgetColumnMap')) {
    migrateConfig.addPlugin(widgetColumnMapPlugin, {});
  }
  if (args.migrate.includes('ts')) {
    await configTypeScript();
  }
  migrateConfig.addPlugin(convertToLFPlugin, {});
  migrateConfig.addPlugin(eslintFixPlugin, {});
}
if (args.count) {
  migrateConfig.addPlugin(countMethodsPlugin, {});
}
if (args.printEventMaps) {
  migrateConfig.addPlugin(printEventMapsPlugin, {});
}
if (migrateConfig.plugins.length === 0) {
  console.warn('No tasks executed, please consult README.md');
} else {
  migrate({rootDir, tsConfigDir, config: migrateConfig, sources}).then(({exitCode}) => process.exit(exitCode));
}

function configTypeScript() {
  // Only consider .ts files for migration, no .less or .js files
  if (sources) {
    sources = sources.map(source => {
      source = source.replace(/(.js)$/, '.ts');
      if (!source.endsWith('.ts')) {
        source += '.ts';
      }
      return source;
    });
  }

  const jsDocTypeMap = Object.assign({
    function: {
      tsName: 'Function',
      acceptsTypeParameters: false
    },
    $: {
      tsName: 'JQuery',
      acceptsTypeParameters: false
    }
  }, config.jsDocTypeMap);
  let typeMap = config.typeMap;
  let paramTypeMap = config.paramTypeMap || typeMap;
  let returnTypeMap = config.returnTypeMap;
  let defaultReturnType = config.defaultReturnType;
  let defaultParamType = config.defaultParamType;
  migrateConfig
    .addPlugin(jsDocPlugin, {typeMap: jsDocTypeMap, annotateReturns: true})
    .addPlugin(declareMissingClassPropertiesPlugin, {moduleMap, typeMap})
    .addPlugin(memberAccessModifierPlugin, {})
    .addPlugin(methodsPlugin, {moduleMap, paramTypeMap, returnTypeMap, defaultReturnType, defaultParamType});
}

function parseTsConfig(path) {
  const content = typescript.sys.readFile(path);
  const {config, error} = typescript.parseConfigFileTextToJson(path, content);
  if (error) {
    const errorMessage = typescript.flattenDiagnosticMessageText(error.messageText, typescript.default.sys.newLine);
    throw new Error(`Parsing TypeScript config failed: ${path}\n${errorMessage}`);
  }
  return config;
}

function tsConfigContainsSources() {
  if (!fs.existsSync('./tsconfig.json')) {
    return false;
  }
  let config = parseTsConfig('./tsconfig.json');
  return config.include || config.files;
}

async function readConfig() {
  let config;
  if (args.config) {
    config = 'file://' + path.resolve(args.config);
    console.log('Using config from file ' + config);
    return import(config);
  }
  return {};
}
