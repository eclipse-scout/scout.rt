/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import path from 'node:path';
import {promises as fs} from 'node:fs';
import YAML from 'yaml';
import {WORKSPACE_MANIFEST_FILENAME} from '@pnpm/constants';
import files from './files.ts';

/**
 * Regex to find all `pnpm-workspace.yaml`-like files
 */
const PNPM_WORKSPACE_REGEX = /^pnpm-workspace(-.*)?\.yaml$/;
const PNPM_WORKSPACE_FILE_EXTENSION = '.yaml';

/**
 * This class contains all information of a `pnpm-workspace.yaml`.
 **/
export class PnpmWorkspaceYaml {

  /**
   * The absolute path to this `pnpm-workspace.yaml`.
   */
  path: string;
  /**
   * The absolute path to the directory containing this `pnpm-workspace.yaml`.
   */
  dir: string;
  /**
   * A parsed {@link YAML} containing the content of this `pnpm-workspace.yaml`.
   */
  doc: YAML.Document<YAML.YAMLMap>;

  constructor(path: string, dir: string, doc: YAML.Document<YAML.YAMLMap>) {
    this.path = path;
    this.dir = dir;
    this.doc = doc;
  }

  /**
   * Parses a {@link PnpmWorkspaceYaml} from a `pnpm-workspace.yaml` file in the given directory.
   */
  static async parse(pnpmWorkspaceFileOrDirectory: string): Promise<PnpmWorkspaceYaml> {
    // read file and parse YAML
    const pnpmWorkspaceYamlPath = pnpmWorkspaceFileOrDirectory.endsWith(PNPM_WORKSPACE_FILE_EXTENSION)
      ? pnpmWorkspaceFileOrDirectory
      : path.resolve(pnpmWorkspaceFileOrDirectory, WORKSPACE_MANIFEST_FILENAME);
    const content = await fs.readFile(pnpmWorkspaceYamlPath, 'utf8');
    const doc = YAML.parseDocument<YAML.YAMLMap>(content);

    // log errors and warnings
    if (doc.errors?.length) {
      let hasError = false;
      doc?.errors?.forEach(err => {
        if (err.name === 'YAMLParseError') {
          hasError = true;
          console.error(`Error parsing yaml '${pnpmWorkspaceYamlPath}': ${err.message} (code ${err.code}) at ${err.pos}.`);
        } else {
          console.warn(`Warning parsing yaml '${pnpmWorkspaceYamlPath}': ${err.message} (code ${err.code}) at ${err.pos}.`);
        }
      });

      // exit process if there are errors
      if (hasError) {
        process.exitCode = 1;
        throw new Error('Yaml parse errors.');
      }
    }

    // create PnpmWorkspaceYaml instance using parsed YAML
    return new PnpmWorkspaceYaml(pnpmWorkspaceYamlPath, path.dirname(pnpmWorkspaceYamlPath), doc);
  }

  /**
   * Gets the directory closest to the file-system root that contains a `pnpm-workspace.yaml` file.
   * The search starts at the given start directory stepping up the parent directories.
   */
  static async findWorkspaceFileDir(dir: string): Promise<string> {
    // no directory given -> nothing to look for
    if (!dir) {
      return;
    }

    let pnpmWorkspaceDir: string;
    let currentDir: string;
    let nextDir = dir;
    // process until current and next directory are equal, which is the case when the root folder is reached
    while (currentDir !== nextDir) {
      // update current and next directory, next is the parent directory of the current
      currentDir = nextDir;
      nextDir = path.join(currentDir, '..');

      // look for a pnpm-workspace.yaml file
      const candidate = path.join(currentDir, WORKSPACE_MANIFEST_FILENAME);
      if (await files.exists(candidate)) {
        pnpmWorkspaceDir = currentDir;
      }
    }
    return pnpmWorkspaceDir;
  }

  /**
   * @param root The starting directory. It searches two levels deep starting from this directory. The root directory is included in the search.
   * @returns absolute paths of `pnpm-workspace.yaml` files.
   */
  static async findPnpmWorkspaceFiles(root: string): Promise<string[]> {
    // find all pnpm-workspace.yaml files
    return await files.list(root, PNPM_WORKSPACE_REGEX, {
      folderExcludes: ['src', 'node_modules', 'target', '.git'],
      maxDepth: 2
    });
  }

  /**
   * @returns the absolute path of all packages in this `pnpm-workspace.yaml`.
   */
  getPackages(): string[] {
    const packages = this.doc.get('packages') as YAML.YAMLSeq<YAML.Scalar<string>>;
    if (!packages) {
      return [];
    }
    return packages.items
      .map(i => i.value)
      .map(p => path.resolve(this.dir, p));
  }

  /**
   * Removes the link to the scout overrides from the overrides.
   */
  removeScoutOverrides() {
    const existingOverrides = this.doc.get('overrides') as YAML.YAMLMap;
    if (!existingOverrides) {
      // there are no overrides -> nothing to remove
      return;
    }
    existingOverrides.delete('<<');
  }

  /**
   * Updates scout overrides in this `pnpm-workspace.yaml` and links them into the overrides.
   */
  updateScoutOverrides(newScoutOverrides: Record<string, string>) {
    // create new scout overrides block
    const scoutOverridesAnchorName = 'scout-overrides';
    const scoutOverrides = new YAML.YAMLMap();
    scoutOverrides.anchor = scoutOverridesAnchorName;
    Object.entries(newScoutOverrides).forEach(([name, override]) => scoutOverrides.set(name, override));

    // replace scout block and include overrides
    const scout = new YAML.YAMLMap();
    scout.set('overrides', scoutOverrides);
    this.doc.set('scout', scout);

    // ensure scout-overrides block is linked in overrides using an alias
    this._ensureScoutOverridesAlias(scout, scoutOverrides, scoutOverridesAnchorName);
  }

  /**
   * Ensures the scout overrides are included in the overrides.
   */
  protected _ensureScoutOverridesAlias(scout: YAML.YAMLMap, scoutOverrides: YAML.YAMLMap, scoutOverridesAnchorName: string) {
    const mergeKey = '<<';
    const existingOverrides = this.doc.get('overrides') as YAML.YAMLMap;
    if (existingOverrides?.items?.length) {
      // overrides are present -> ensure merge key
      const first = existingOverrides.items[0] as YAML.Pair<YAML.Scalar>;
      if (first?.key?.value === mergeKey && first?.value instanceof YAML.Alias) {
        // merge key is present and points to an alias
        const alias = first.value as YAML.Alias;
        // check if the alias points to the scout overrides
        if (alias?.source === scoutOverridesAnchorName) {
          return;
        }
      }

      // merge key is missing, not at the beginning or does not point to the correct alias -> add at the beginning or move to the beginning
      const existingOverridesItems = existingOverrides.items;
      const scoutOverridesMergeKeyIndex = existingOverridesItems.findIndex((pair: YAML.Pair<YAML.Scalar>) => pair.key?.value === mergeKey && pair.value instanceof YAML.Alias && pair.value.source === scoutOverridesAnchorName);
      if (scoutOverridesMergeKeyIndex > -1) {
        existingOverridesItems.splice(scoutOverridesMergeKeyIndex, 1);
      }
      existingOverrides.items = [
        new YAML.Pair(
          new YAML.Scalar(mergeKey),
          this.doc.createAlias(scoutOverrides, scoutOverridesAnchorName)
        ),
        ...existingOverrides.items
      ];

      // move overrides after scout if necessary
      const items = this.doc.contents.items;
      const overridesIndex = items.findIndex(pair => pair.value === existingOverrides);
      const scoutIndex = items.findIndex(pair => pair.value === scout);
      if (overridesIndex > -1 && overridesIndex < scoutIndex) {
        // temporarily remove overrides
        const overridesPair = items.splice(overridesIndex, 1)[0];
        // after removing the overrides scout is at scoutIndex-1 -> inserting the overrides at scoutIndex places them directly after scout
        items.splice(scoutIndex, 0, overridesPair);
      }
    } else {
      // create new overrides block including the alias
      // it is added at the end and therefore after the
      const newOverrides = {};
      newOverrides[mergeKey] = this.doc.createAlias(scoutOverrides, scoutOverridesAnchorName);
      this.doc.set('overrides', newOverrides);
    }
  }

  /**
   * Writes this `pnpm-workspace.yaml` to the disk.
   */
  async flush(): Promise<void> {
    // Do not use @pnpm/workspace.manifest-writer as it changes order and removes comments
    const content = YAML.stringify(this.doc);
    return await fs.writeFile(this.path, content, 'utf8');
  }
}
