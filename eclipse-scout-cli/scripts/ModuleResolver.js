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
const ts = require('typescript');
const TypeReferenceResolver = require('./TypeReferenceResolver');
const REGISTER_NS_PATTERN = new RegExp('\\.registerNamespace\\s*\\(\'(\\w+)\'\\s*,');
const JS_COMMENTS_PATTERN = new RegExp('\\/\\*[\\s\\S]*?\\*\\/|(?<=[^:])\\/\\/.*|^\\/\\/.*', 'g');
const TS_EXTENSIONS = new Set([ts.Extension.Ts, ts.Extension.Tsx, ts.Extension.Dts, ts.Extension.Mts, ts.Extension.Cts, ts.Extension.Dmts, ts.Extension.Dcts]);
const STAR_EXPORT_PATTERN = new RegExp('export\\s*\\*');

/**
 * Class to resolve the Scout JS namespace of a module (e.g. module 'eclipse-scout/core' uses namespace 'scout').
 * Furthermore, it resolves and parses imported modules like TypeScript does (see resolveModuleFile and getTypeReferenceResolverOfModule).
 */
module.exports = class ModuleResolver {

  constructor() {
    this._rootsByFileDir = new Map();
    this._namespaceByModuleRoot = new Map();
    this._resolvedModulesByKey = new Map();
    this._typeReferenceResolversByFilePath = new Map();
    this._typeReferenceResolversBySourceFile = new WeakMap();
    this._filesByPath = new Map();
    this.ownModuleNamespace = null;
  }

  /**
   * Resolves a module name like TypeScript does.
   * Depending on the package.json of a dependency, this is a TS source file (during development) or a declaration file (*.d.ts) of the built dependency.
   *
   * @param moduleName {string} e.g. './index' or '@eclipse-scout/core'
   * @param containingFile {string} The path of the file importing the module.
   * @param compilerOptions {ts.CompilerOptions}
   * @returns {ts.ResolvedModuleFull|null}
   */
  resolveModuleFile(moduleName, containingFile, compilerOptions) {
    const key = path.dirname(containingFile) + '@' + moduleName;
    let resolvedModule = this._resolvedModulesByKey.get(key);
    if (!resolvedModule) {
      resolvedModule = ts.resolveModuleName(moduleName, containingFile, compilerOptions || {}, ts.sys).resolvedModule;
      if (resolvedModule) {
        this._resolvedModulesByKey.set(key, resolvedModule); // unresolved modules are not remembered as they might be created later (watch mode)
      }
    }
    return resolvedModule || null;
  }

  /**
   * @param moduleName {string} e.g. './index' or '@eclipse-scout/core'
   * @param containingFile {string} The path of the file importing the module.
   * @param compilerOptions {ts.CompilerOptions}
   * @param exportName {string} Optional name the module must export. If the file can neither declare nor re-export it, it is not parsed.
   * @returns {TypeReferenceResolver|null} The resolver for the file the module resolves to.
   * null if it cannot be resolved, is no TS file (e.g. a JavaScript module without declarations) or cannot export the given name.
   */
  getTypeReferenceResolverOfModule(moduleName, containingFile, compilerOptions, exportName = null) {
    const resolvedModule = this.resolveModuleFile(moduleName, containingFile, compilerOptions);
    if (!resolvedModule || !TS_EXTENSIONS.has(resolvedModule.extension)) {
      return null;
    }
    const filePath = resolvedModule.resolvedFileName;
    const file = this._readFile(filePath);
    if (!file || (exportName && !file.text.includes(exportName) && !STAR_EXPORT_PATTERN.test(file.text))) {
      return null;
    }
    const cached = this._typeReferenceResolversByFilePath.get(filePath);
    if (cached?.file === file) {
      return cached.typeReferenceResolver;
    }
    const sourceFile = ts.createSourceFile(filePath, file.text, ts.ScriptTarget.Latest, true);
    const typeReferenceResolver = this.getTypeReferenceResolverOfSourceFile(sourceFile, compilerOptions);
    this._typeReferenceResolversByFilePath.set(filePath, {file, typeReferenceResolver});
    return typeReferenceResolver;
  }

  /**
   * Reads the given file if it changed since the last call.
   * @returns {{mtimeMs: number, text: string}|null} null if the file does not exist.
   */
  _readFile(filePath) {
    let file = this._filesByPath.get(filePath);
    const mtimeMs = fs.statSync(filePath, {throwIfNoEntry: false})?.mtimeMs;
    if (mtimeMs === undefined) {
      return null;
    }
    if (file?.mtimeMs !== mtimeMs) {
      file = {mtimeMs, text: fs.readFileSync(filePath, 'utf-8')};
      this._filesByPath.set(filePath, file);
    }
    return file;
  }

  /**
   * @param sourceFile {ts.SourceFile} A file parsed by getTypeReferenceResolverOfModule or a file provided by the compiler (e.g. the file currently compiled).
   * @param compilerOptions {ts.CompilerOptions} The options used to resolve imported modules if the resolver is created.
   * @returns {TypeReferenceResolver} The resolver of the given file. Created on first use.
   */
  getTypeReferenceResolverOfSourceFile(sourceFile, compilerOptions) {
    let typeReferenceResolver = this._typeReferenceResolversBySourceFile.get(sourceFile);
    if (!typeReferenceResolver) {
      typeReferenceResolver = new TypeReferenceResolver(sourceFile, this, compilerOptions);
      this._typeReferenceResolversBySourceFile.set(sourceFile, typeReferenceResolver);
    }
    return typeReferenceResolver;
  }

  /**
   * @param node {ts.Node}
   * @returns {ts.SourceFile|null} The file containing the given node. null for synthesized nodes not referencing a node of a file.
   */
  findSourceFile(node) {
    // synthesized type references (e.g. for new expressions) have no parent but reference the original type name node
    let current = node.parent || !node.typeName ? node : node.typeName;
    while (current && !ts.isSourceFile(current)) {
      current = current.parent;
    }
    return current || null;
  }

  /**
   * Resolves the Scout JS namespace of the module containing the given file.
   *
   * Algorithm:
   * 1. It resolves the module root of the given file (the first parent directory containing the package.json file).
   * 2. Searches in the module for a file *.js or *.ts file calling the Scout method 'registerNamespace' and uses the value passed as namespace.
   *
   * @param filePath {string} A file inside the module (e.g. a file resolved by resolveModuleFile).
   * @param ownModuleFilePath {string} A file of the module currently built. ownModuleNamespace is used for this module if set.
   * @returns {string} The namespace or undefined.
   */
  resolveNamespace(filePath, ownModuleFilePath) {
    const moduleRoot = this._getModuleRoot(path.dirname(filePath));
    let namespace = this._namespaceByModuleRoot.get(moduleRoot);
    if (!namespace) {
      if (this.ownModuleNamespace && moduleRoot === this._getModuleRoot(path.dirname(ownModuleFilePath))) {
        namespace = this.ownModuleNamespace;
      } else {
        namespace = this._resolveNamespace(moduleRoot);
      }
      this._namespaceByModuleRoot.set(moduleRoot, namespace);
    }
    return namespace;
  }

  /**
   * @param moduleRoot {string}
   * @returns {string}
   */
  _resolveNamespace(moduleRoot) {
    let searchRoot = moduleRoot;
    const srcFolder = path.join(moduleRoot, 'src');
    if (fs.existsSync(srcFolder)) {
      searchRoot = srcFolder;
      const mavenSrc = path.join(searchRoot, 'main/js');
      if (fs.existsSync(mavenSrc)) {
        searchRoot = mavenSrc;
      }
    }
    return this._parseFromRegister(searchRoot);
  }

  /**
   * @param root {string}
   * @returns {string} The namespace parsed from the 'registerNamespace' function.
   */
  _parseFromRegister(root) {
    let namespace = null;
    this._visitFiles(root, filePath => {
      const content = fs.readFileSync(filePath, 'utf-8').toString();
      const result = REGISTER_NS_PATTERN.exec(content.replaceAll(JS_COMMENTS_PATTERN, ''));
      if (result?.length === 2) {
        namespace = result[1];
        return false; // abort
      }
      return true;
    });
    return namespace;
  }

  /**
   * Breadth-first visit of all *.ts or *.ts files within the given root directory excluding node_modules sub folders.
   * @param root {string} The root directory path
   * @param callback {(string) => boolean} Callback for all *.ts or *.js file paths in given directory. Visiting is aborted as soon as the callback returns false.
   */
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

  /**
   * @param directory {string} The directory to get the contents from
   * @returns {Dirent[]} Array of child files (only *.js or *.ts) or folders (excluding node_modules)
   */
  _readDir(directory) {
    return fs.readdirSync(directory, {withFileTypes: true})
      .filter(dirent => (dirent.isDirectory() && dirent.name !== 'node_modules') || dirent.name.endsWith('.js') || dirent.name.endsWith('.ts'))
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

  /**
   *
   * @param sourceFileDir {string}
   * @returns {string}
   */
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

  /**
   * @param sourceFileDir {string}
   * @returns {string}
   */
  _findModuleRoot(sourceFileDir) {
    while (sourceFileDir && !fs.existsSync(path.join(sourceFileDir, 'package.json'))) {
      sourceFileDir = path.dirname(sourceFileDir);
    }
    return sourceFileDir.replaceAll('\\', '/');
  }
};
