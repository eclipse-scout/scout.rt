/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * AI Disclosure: This file was partially AI-generated.
 * The AI-generated portions are made available under CC0-1.0
 * and not subject to the project's licence.
 *
 * SPDX-License-Identifier: EPL-2.0 and CC0-1.0
 */

import assert from 'node:assert/strict';
import {promises as fs} from 'node:fs';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {describe, it} from 'node:test';
import {VersionMetaDataComputer} from '../../../scripts/install/overrides/VersionMetaDataComputer.ts';

describe('VersionMetaDataComputer', () => {

  describe('compute', () => {

    it('returns dependency version and fix=true when declared with fix version', async () => {
      await withPackageJson(
        {
          dependencies: {lodash: '4.17.21'}
        },
        async packageJsonDirectory => {
          let versionMetaDataComputer = new VersionMetaDataComputer();
          let versionMetaData = await versionMetaDataComputer.compute(packageJsonDirectory, 'lodash', '4.17.21');
          assert.deepEqual(versionMetaData, {version: '4.17.21', fix: true});
        }
      );
    });

    it('returns dependency version and fix=false when declared with range version', async () => {
      await withPackageJson(
        {
          dependencies: {lodash: '^4.0.0'}
        },
        async packageJsonDirectory => {
          let versionMetaDataComputer = new VersionMetaDataComputer();
          let versionMetaData = await versionMetaDataComputer.compute(packageJsonDirectory, 'lodash', '4.17.21');
          assert.deepEqual(versionMetaData, {version: '4.17.21', fix: false});
        }
      );
    });

    it('returns npm alias with dependency version and fix=true when declared as npm alias with fix version', async () => {
      await withPackageJson(
        {
          dependencies: {'fancy-lodash': 'npm:lodash@4.17.21'}
        },
        async packageJsonDirectory => {
          let versionMetaDataComputer = new VersionMetaDataComputer();
          let versionMetaData = await versionMetaDataComputer.compute(packageJsonDirectory, 'fancy-lodash', '4.17.21');
          assert.deepEqual(versionMetaData, {version: 'npm:lodash@4.17.21', fix: true});
        }
      );
    });

    it('returns npm alias with dependency version and fix=false when declared as npm alias with range version', async () => {
      await withPackageJson(
        {
          dependencies: {'fancy-lodash': 'npm:lodash@^4.0.0'}
        },
        async packageJsonDirectory => {
          let versionMetaDataComputer = new VersionMetaDataComputer();
          let versionMetaData = await versionMetaDataComputer.compute(packageJsonDirectory, 'fancy-lodash', '4.17.21');
          assert.deepEqual(versionMetaData, {version: 'npm:lodash@4.17.21', fix: false});
        }
      );
    });

    it('returns dependency version and fix=false when dependency is not declared in package.json', async () => {
      await withPackageJson(
        {
          dependencies: {}
        },
        async packageJsonDirectory => {
          let versionMetaDataComputer = new VersionMetaDataComputer();
          let versionMetaData = await versionMetaDataComputer.compute(packageJsonDirectory, 'lodash', '4.17.21');
          assert.deepEqual(versionMetaData, {version: '4.17.21', fix: false});
        }
      );
    });

    it('returns dependency version and fix=false when package.json does not exist', async () => {
      let versionMetaDataComputer = new VersionMetaDataComputer();
      let versionMetaData = await versionMetaDataComputer.compute('/non/existent/path/xyz', 'lodash', '4.17.21');
      assert.deepEqual(versionMetaData, {version: '4.17.21', fix: false});
    });
  });

  describe('_splitNpmAliasSpecifier', () => {

    it('no alias', () => {
      assertNpmAlias('^11.3.0', null, '^11.3.0');
      assertNpmAlias(null, null, null);
      assertNpmAlias(undefined, null, undefined);
      assertNpmAlias('', null, '');
    });

    it('with namespace', () => {
      assertNpmAlias('npm:@eclipse-scout/core@26.2.0', '@eclipse-scout/core', '26.2.0');
      assertNpmAlias('npm:@eclipse-scout/core@^26.2.0', '@eclipse-scout/core', '^26.2.0');
      assertNpmAlias('npm:@eclipse-scout/core', '@eclipse-scout/core', null);
    });

    it('without namespace', () => {
      assertNpmAlias('npm:mylib@26.2.0', 'mylib', '26.2.0');
      assertNpmAlias('npm:mylib@^26.2.0', 'mylib', '^26.2.0');
      assertNpmAlias('npm:mylib', 'mylib', null);
    });

    function assertNpmAlias(rawSpecifier: string, expectedName: string, expectedVersion: string) {
      const {name, version} = new TestingVersionMetaDataComputer()._splitNpmAliasSpecifier(rawSpecifier);
      assert.equal(name, expectedName);
      assert.equal(version, expectedVersion);
    }
  });

  describe('_getDependencies', () => {

    it('returns null when path does not exist', async () => {
      const versionMetaDataComputer = new TestingVersionMetaDataComputer();
      const dependencies = await versionMetaDataComputer._getDependencies('/non/existent/path/xyz');
      assert.equal(dependencies, null);
    });

    it('returns dependencies from package.json', async () => {
      await withPackageJson(
        {
          dependencies: {lodash: '4.17.21'}
        },
        async packageJsonDirectory => {
          const versionMetaDataComputer = new TestingVersionMetaDataComputer();
          const dependencies = await versionMetaDataComputer._getDependencies(packageJsonDirectory);
          assert.deepEqual(dependencies, {lodash: '4.17.21'});
        }
      );
    });

    it('caches the result so _readDependencies is only called once per path', async () => {
      await withPackageJson(
        {
          dependencies: {lodash: '4.17.21'}
        },
        async packageJsonDirectory => {
          const versionMetaDataComputer = new TestingVersionMetaDataComputer();
          let readCount = 0;
          const readDependencies = versionMetaDataComputer._readDependencies;
          versionMetaDataComputer._readDependencies = (pkgJsonPath: string) => {
            readCount++;
            return readDependencies(pkgJsonPath);
          };

          await versionMetaDataComputer._getDependencies(packageJsonDirectory);
          assert.equal(readCount, 1);

          await versionMetaDataComputer._getDependencies(packageJsonDirectory);
          assert.equal(readCount, 1);
        }
      );
    });

    it('returns cached result on subsequent calls', async () => {
      await withPackageJson(
        {
          dependencies: {lodash: '4.17.21'}
        },
        async packageJsonDirectory => {
          const versionMetaDataComputer = new TestingVersionMetaDataComputer();
          const dependencies1 = await versionMetaDataComputer._getDependencies(packageJsonDirectory);
          await fs.rm(join(packageJsonDirectory, 'package.json'));
          const dependencies2 = await versionMetaDataComputer._getDependencies(packageJsonDirectory);
          assert.deepEqual(dependencies1, dependencies2);
        }
      );
    });

    it('caches null result when path does not exist', async () => {
      const versionMetaDataComputer = new TestingVersionMetaDataComputer();
      let readCount = 0;
      const readDependencies = versionMetaDataComputer._readDependencies;
      versionMetaDataComputer._readDependencies = (pkgJsonPath: string) => {
        readCount++;
        return readDependencies(pkgJsonPath);
      };

      await versionMetaDataComputer._getDependencies('/non/existent/path/xyz');
      assert.equal(readCount, 1);

      await versionMetaDataComputer._getDependencies('/non/existent/path/xyz');
      assert.equal(readCount, 1);
    });

    it('uses separate cache entries per path', async () => {
      const versionMetaDataComputer = new TestingVersionMetaDataComputer();
      let packageJsonDirectoryA: string;
      let packageJsonDirectoryB: string;
      await withPackageJson(
        {
          dependencies: {a: '1.0.0'}
        },
        async packageJsonDirectory => {
          await versionMetaDataComputer._getDependencies(packageJsonDirectory);
          packageJsonDirectoryA = packageJsonDirectory;
        }
      );
      await withPackageJson(
        {
          dependencies: {b: '2.0.0'}
        },
        async packageJsonDirectory => {
          await versionMetaDataComputer._getDependencies(packageJsonDirectory);
          packageJsonDirectoryB = packageJsonDirectory;
        }
      );
      assert.deepEqual(versionMetaDataComputer._cache, new Map([
        [packageJsonDirectoryA, {a: '1.0.0'}],
        [packageJsonDirectoryB, {b: '2.0.0'}]
      ]));
    });
  });

  describe('_readDependencies', () => {

    it('returns null for falsy path', async () => {
      const versionMetaDataComputer = new TestingVersionMetaDataComputer();
      assert.equal(await versionMetaDataComputer._readDependencies(null), null);
      assert.equal(await versionMetaDataComputer._readDependencies(undefined), null);
      assert.equal(await versionMetaDataComputer._readDependencies(''), null);
    });

    it('returns null when path does not exist', async () => {
      const versionMetaDataComputer = new TestingVersionMetaDataComputer();
      assert.equal(await versionMetaDataComputer._readDependencies('/non/existent/path/xyz'), null);
    });

    it('returns empty object when package.json has no dependency sections', async () => {
      await withPackageJson(
        {
          name: 'test-pkg',
          version: '1.0.0'
        },
        async packageJsonDirectory => {
          const versionMetaDataComputer = new TestingVersionMetaDataComputer();
          assert.deepEqual(await versionMetaDataComputer._readDependencies(packageJsonDirectory), {});
        }
      );
    });

    it('merges all dependency types', async () => {
      await withPackageJson(
        {
          dependencies: {a: '4.0.0'},
          devDependencies: {b: '3.0.0'},
          optionalDependencies: {c: '2.0.0'},
          peerDependencies: {d: '1.0.0'}
        },
        async packageJsonDirectory => {
          const versionMetaDataComputer = new TestingVersionMetaDataComputer();
          const dependencies = await versionMetaDataComputer._readDependencies(packageJsonDirectory);
          assert.deepEqual(
            dependencies,
            {
              a: '4.0.0',
              b: '3.0.0',
              c: '2.0.0',
              d: '1.0.0'
            }
          );
        }
      );
    });

    it('optionalDependencies takes precedence over dependencies, devDependencies and optionalDependencies', async () => {
      await withPackageJson(
        {
          optionalDependencies: {a: '2.0.0'},
          dependencies: {a: '1.0.0'}
        },
        async packageJsonDirectory => {
          const versionMetaDataComputer = new TestingVersionMetaDataComputer();
          const dependencies = await versionMetaDataComputer._readDependencies(packageJsonDirectory);
          assert.equal(dependencies.a, '2.0.0');
        }
      );

      await withPackageJson(
        {
          optionalDependencies: {a: '2.0.0'},
          devDependencies: {a: '1.0.0'}
        },
        async packageJsonDirectory => {
          const versionMetaDataComputer = new TestingVersionMetaDataComputer();
          const dependencies = await versionMetaDataComputer._readDependencies(packageJsonDirectory);
          assert.equal(dependencies.a, '2.0.0');
        }
      );

      await withPackageJson(
        {
          optionalDependencies: {a: '2.0.0'},
          peerDependencies: {a: '1.0.0'}
        },
        async packageJsonDirectory => {
          const versionMetaDataComputer = new TestingVersionMetaDataComputer();
          const dependencies = await versionMetaDataComputer._readDependencies(packageJsonDirectory);
          assert.equal(dependencies.a, '2.0.0');
        }
      );
    });

    it('dependencies takes precedence over devDependencies and peerDependencies', async () => {
      await withPackageJson(
        {
          dependencies: {a: '2.0.0'},
          devDependencies: {a: '1.0.0'}
        },
        async packageJsonDirectory => {
          const versionMetaDataComputer = new TestingVersionMetaDataComputer();
          const dependencies = await versionMetaDataComputer._readDependencies(packageJsonDirectory);
          assert.equal(dependencies.a, '2.0.0');
        }
      );

      await withPackageJson(
        {
          dependencies: {a: '2.0.0'},
          peerDependencies: {a: '1.0.0'}
        },
        async packageJsonDirectory => {
          const versionMetaDataComputer = new TestingVersionMetaDataComputer();
          const dependencies = await versionMetaDataComputer._readDependencies(packageJsonDirectory);
          assert.equal(dependencies.a, '2.0.0');
        }
      );
    });

    it('devDependencies takes precedence over peerDependencies', async () => {
      await withPackageJson(
        {
          devDependencies: {a: '2.0.0'},
          peerDependencies: {a: '1.0.0'}
        },
        async packageJsonDirectory => {
          const versionMetaDataComputer = new TestingVersionMetaDataComputer();
          const dependencies = await versionMetaDataComputer._readDependencies(packageJsonDirectory);
          assert.equal(dependencies.a, '2.0.0');
        }
      );
    });
  });
});

async function withPackageJson(packageJson: object, callback: (packageJsonDirectory: string) => Promise<void>): Promise<void> {
  const dir = await fs.mkdtemp(join(tmpdir(), 'vmdc-test-'));
  try {
    await fs.writeFile(join(dir, 'package.json'), JSON.stringify(packageJson));
    await callback(dir);
  } finally {
    await fs.rm(dir, {recursive: true});
  }
}

class TestingVersionMetaDataComputer extends VersionMetaDataComputer {

  declare _cache: Map<string, Record<string, string>>;

  override _splitNpmAliasSpecifier(specifier: string): { name?: string; version: string } {
    return super._splitNpmAliasSpecifier(specifier);
  }

  override _getDependencies(pkgJsonPath: string): Promise<Record<string, string>> {
    return super._getDependencies(pkgJsonPath);
  }

  override _readDependencies(pkgJsonPath: string): Promise<Record<string, string>> {
    return super._readDependencies(pkgJsonPath);
  }
}
