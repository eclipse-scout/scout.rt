/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

const fs = require('fs');
const path = require('path');
const REGISTER_NS_PATTERN = new RegExp('\\.registerNamespace\\s*\\(\'(\\w+)\'\\s*,');
const JS_COMMENTS_PATTERN = new RegExp('\\/\\*[\\s\\S]*?\\*\\/|(?<=[^:])\\/\\/.*|^\\/\\/.*', 'g');

/**
 * Class to resolve the Scout JS namespace of a module (e.g. module 'eclipse-scout/core' uses namespace 'scout').
 */
module.exports = class ModuleNamespaceResolver {

  constructor() {
    this._rootsByFileDir = new Map();
    this._namespaceByModuleRoot = new Map();
    this._dependencyRootsByModuleRoot = new Set();
    this.ownModuleNamespace = null;
  }

  /**
   * Resolves the Scout JS namespace for the given module name.
   *
   * Algorithm:
   * 1. It resolves the module root of the sourceFilePath given (the first parent directory containing the package.json file).
   * 2. If a moduleName is given searches for a dependency in node_modules with given moduleName and uses its module root.
   * 3. Searches in the module for a file *.js or *.ts file calling the Scout method 'registerNamespace' and uses the value passed as namespace.
   *
   * @param moduleName {string} The name of an external dependency module (e.g. '@eclipse-scout/core') or null if the namespace of the module containing the sourceFilePath should be returned.
   * @param sourceFilePath {string} A file path inside a module that has a dependency to the moduleName (if available).
   * @returns {string} The namespace or undefined.
   */
  resolveNamespace(moduleName, sourceFilePath) {
    const moduleRoot = this.resolveModuleRoot(moduleName, sourceFilePath);
    let namespace = this._namespaceByModuleRoot.get(moduleRoot);
    if (!namespace) {
      if (!moduleName && this.ownModuleNamespace) {
        // use given namespace for own module if known
        namespace = this.ownModuleNamespace;
      } else {
        namespace = this._resolveNamespace(moduleRoot);
      }
      this._namespaceByModuleRoot.set(moduleRoot, namespace);
    }
    return namespace;
  }

  _resolveNamespace(moduleRoot) {
    let src = path.join(moduleRoot, 'src');
    const mavenSrc = path.join(src, 'main/js');
    if (fs.existsSync(mavenSrc)) {
      src = mavenSrc;
    }
    return this._parseFromRegister(src);
  }

  _parseFromRegister(root) {
    let namespace = null;
    this._visitFiles(root, filePath => {
      const content = fs.readFileSync(filePath, 'utf-8');
      const result = REGISTER_NS_PATTERN.exec(content.replaceAll(JS_COMMENTS_PATTERN, ''));
      if (result?.length === 2) {
        namespace = result[1];
        return false; // abort
      }
      return true;
    });
    return namespace;
  }

  _visitFiles(root, callback) {
    const buf = this._readDir(root);
    while (buf.length) {
      const dirent = buf.shift(); // remove first
      const filePath = path.join(dirent.parentPath || dirent.path, dirent.name);
      if (dirent.isFile()) {
        const cont = callback(filePath);
        if (!cont) {
          return;
        }
      } else {
        buf.push(...this._readDir(filePath));
      }
    }
  }

  _readDir(directory) {
    return fs.readdirSync(directory, {withFileTypes: true})
      .filter(dirent => dirent.isDirectory() || dirent.name.endsWith('.js') || dirent.name.endsWith('.ts'))
      .sort((a, b) => { // files first
        if (a.isDirectory() && !b.isDirectory()) {
          return 1;
        }
        if (!a.isDirectory() && b.isDirectory()) {
          return -1;
        }
        return a.name.localeCompare(b.name);
      });
  }

  resolveModuleRoot(moduleName, sourceFilePath) {
    const directory = path.dirname(sourceFilePath);
    const moduleRoot = this._getModuleRoot(directory);
    if (!moduleName) {
      return moduleRoot; // no external module name: own module
    }
    return this._resolveExternalModule(moduleRoot, moduleName);
  }

  _resolveExternalModule(moduleRoot, moduleName) {
    const dependencyRoot = path.join(moduleRoot, 'node_modules', moduleName);
    if (this._dependencyRootsByModuleRoot.has(dependencyRoot)) {
      return dependencyRoot; // existence already verified
    }

    if (!fs.existsSync(path.join(dependencyRoot, 'package.json'))) {
      throw new Error(`Dependency ${moduleName} not found in ${moduleRoot}.`);
    }
    this._dependencyRootsByModuleRoot.add(dependencyRoot);
    return dependencyRoot;
  }

  _getModuleRoot(sourceFileDir) {
    let root = this._rootsByFileDir.get(sourceFileDir);
    if (!root) {
      root = this._findModuleRoot(sourceFileDir);
      if (!root) {
        throw new Error(`${sourceFileDir} is not within any Node module.`);
      }
      this._rootsByFileDir.set(sourceFileDir, root); // remember for next files
    }
    return root;
  }

  _findModuleRoot(sourceFileDir) {
    while (sourceFileDir && !fs.existsSync(path.join(sourceFileDir, 'package.json'))) {
      sourceFileDir = path.dirname(sourceFileDir);
    }
    return sourceFileDir.replaceAll('\\', '/');
  }
};
