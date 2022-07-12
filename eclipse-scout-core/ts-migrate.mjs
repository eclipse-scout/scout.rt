#!/usr/bin/env node


/* eslint-disable no-await-in-loop, no-restricted-syntax */
import path from 'path';
import {
    // addConversionsPlugin,
//     declareMissingClassPropertiesPlugin,
//     eslintFixPlugin,
    explicitAnyPlugin,
    jsDocPlugin,
//     stripTSIgnorePlugin,
//     tsIgnorePlugin,
//     hoistClassStaticsPlugin,
    declareMissingClassPropertiesPlugin,
    memberAccessibilityPlugin,
    eslintFixPlugin} from 'ts-migrate-plugins';
import { migrate, MigrateConfig } from 'ts-migrate-server';

import parser from 'yargs-parser';
const args = parser(process.argv);

// const path = require("path");
// const eslintFixPlugin = require("ts-migrate-plugins/build/src/plugins/eslint-fix")
// const memberAccessibilityPlugin = require("ts-migrate-plugins/build/src/plugins/member-accessibility")
// const migrate = require("ts-migrate-server/build/src/migrate");
// const MigrateConfig = require("ts-migrate-server/build/src/migrate/MigrateConfig");

const defaultAccessibility = undefined;
const privateRegex = undefined;
const protectedRegex = "_";
const publicRegex = undefined;
const anyAlias = undefined;
const rootDir = path.resolve(process.cwd());
const sources = args.sources;
const typeMap = {
    function: {
        tsName: 'Function',
        acceptsTypeParameters: false,
    },
};

const config = new MigrateConfig()
    // .addPlugin(stripTSIgnorePlugin, {})
    // .addPlugin(hoistClassStaticsPlugin, { anyAlias })
    .addPlugin(declareMissingClassPropertiesPlugin, { anyAlias })
    // .addPlugin(memberAccessibilityPlugin, {
    //     defaultAccessibility,
    //     privateRegex,
    //     protectedRegex,
    //     publicRegex,
    // })
    .addPlugin(jsDocPlugin, { anyAlias, typeMap })
    .addPlugin(explicitAnyPlugin, { anyAlias })
    // .addPlugin(addConversionsPlugin, { anyAlias })
    // We need to run eslint-fix before ts-ignore because formatting may affect where
    // the errors are that need to get ignored.
    // .addPlugin(eslintFixPlugin, {});
    // .addPlugin(tsIgnorePlugin, {})
    // We need to run eslint-fix again after ts-ignore to fix up formatting.
    // .addPlugin(eslintFixPlugin, {});


migrate({ rootDir, config, sources }).then(exitCode => process.exit(exitCode));


