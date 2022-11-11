#!/usr/bin/env node

import path from 'path';
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

const rename = renameModule.default; // Default imports don't work as expected when importing from cjs modules

const yargsOptions = {
  boolean: ['count', 'printEventMaps', 'rename', 'widgetColumnMap'],
  array: ['sources'],
  string: ['migrate', 'moduleMap', 'jsDocTypeMap', 'paramTypeMap', 'returnTypeMap'],
  default: {'migrate': '', 'rename': null, 'jsDocTypeMap': {}, 'count': false, 'printEventMaps': false, 'widgetColumnMap': false},
  choices: ['migrate', ['ts', 'objectType']]
};
const args = parser(process.argv, yargsOptions);

const rootDir = path.resolve(process.cwd());
let sources = args.sources;
let renameToTs = args.rename;
if (args.rename === null) {
  // Rename if migrate to ts is enabled and renaming not explicitly disabled
  renameToTs = args.migrate === 'ts';
}
if (renameToTs) {
  rename({rootDir, sources});
}
if (!args.migrate && !args.count && !args.printEventMaps && !args.widgetColumnMap) {
  process.exit(-1);
}

let moduleMap = args.moduleMap;
if (moduleMap) {
  console.log('Using moduleMap: ' + JSON.stringify(moduleMap));
}

const config = new MigrateConfig();
if (args.migrate === 'objectType') {
  configObjectType();
}
if (args.migrate === 'ts') {
  configTypeScript();
}
if (args.count) {
  config.addPlugin(countMethodsPlugin, {});
}
if (args.printEventMaps) {
  config.addPlugin(printEventMapsPlugin, {});
}
if (args.widgetColumnMap) {
  config
    .addPlugin(convertToCRLFPlugin, {})
    .addPlugin(widgetColumnMapPlugin, {})
    .addPlugin(convertToLFPlugin, {});
}
migrate({rootDir, config, sources}).then(exitCode => process.exit(exitCode));

function configObjectType() {
  config
    .addPlugin(convertToCRLFPlugin, {})
    .addPlugin(typedObjectTypePlugin, {moduleMap})
    .addPlugin(convertToLFPlugin, {})
    .addPlugin(eslintFixPlugin, {});
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
  }, args.jsDocTypeMap);
  const paramTypeMap = args.paramTypeMap;
  const returnTypeMap = args.returnTypeMap;

  config
    .addPlugin(convertToCRLFPlugin, {})
    .addPlugin(jsDocPlugin, {typeMap: jsDocTypeMap, annotateReturns: true})
    .addPlugin(declareMissingClassPropertiesPlugin, {moduleMap})
    .addPlugin(memberAccessModifierPlugin, {})
    .addPlugin(methodsPlugin, {moduleMap, paramTypeMap, returnTypeMap})
    .addPlugin(convertToLFPlugin, {})
    .addPlugin(eslintFixPlugin, {});
}
