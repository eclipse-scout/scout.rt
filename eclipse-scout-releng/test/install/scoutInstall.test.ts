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
import {before, beforeEach, describe, it, mock} from 'node:test';
import type {DependencyConvergenceLogLevel} from '../../scripts/install/overrides/OverridesComputer.ts';

// In order to run this test, Node.js must be started with the `--experimental-test-module-mocks` command-line flag.
// See https://nodejs.org/docs/latest-v24.x/api/cli.html#--experimental-test-module-mocks for more information.
describe('scoutInstall', () => {

  const pnpmForkMock = mock.fn(async (workingDir: string, ...args: string[]) => {
  });
  const updateAllScoutOverridesMock = mock.fn(async (lockfileDir: string, convergenceLogLevel?: DependencyConvergenceLogLevel) => {
  });
  let scoutInstall: (dir: string, options?) => Promise<void>;
  let updateMode: {
    readonly REQUIRED: 'required';
    readonly ALL: 'all';
    readonly SNAPSHOTS: 'snapshots';
  };

  before(async () => {
    mock.module('../../scripts/util/pnpm.ts', {
      defaultExport: {fork: pnpmForkMock}
    });

    mock.module('../../scripts/install/overrides/updateScoutOverrides.ts', {
      namedExports: {
        updateAllScoutOverrides: updateAllScoutOverridesMock
      }
    });

    const scoutInstallModule = await import('../../scripts/install/scoutInstall.ts');
    scoutInstall = scoutInstallModule.scoutInstall;
    updateMode = scoutInstallModule.updateMode;
  });

  beforeEach(() => {
    pnpmForkMock.mock.mockImplementation(async (workingDir: string, ...args: string[]) => {
    });
    pnpmForkMock.mock.resetCalls();
    updateAllScoutOverridesMock.mock.mockImplementation(async (lockfileDir: string, convergenceLogLevel?: DependencyConvergenceLogLevel) => {
    });
    updateAllScoutOverridesMock.mock.resetCalls();
  });

  describe('scoutInstall', () => {

    it('does not disable scout overrides before pnpm update in REQUIRED mode', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        let yamlAtUpdateCall: string;
        pnpmForkMock.mock.mockImplementation(async (workingDir: string, ...args: string[]) => {
          if (args[0] === 'update') {
            yamlAtUpdateCall = await fs.readFile(join(workingDir, 'pnpm-workspace.yaml'), 'utf8');
          }
        });

        await scoutInstall(dir, {updateMode: updateMode.REQUIRED});

        assert.equal(
          yamlAtUpdateCall,
          'packages:\n' +
          '  - pkg-a\n' +
          'scout:\n' +
          '  overrides: &scout-overrides\n' +
          '    lodash: 4.17.21\n' +
          'overrides:\n' +
          '  <<: *scout-overrides\n'
        );
      });
    });

    it('disables scout overrides before pnpm update in ALL mode', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        let yamlAtUpdateCall: string;
        pnpmForkMock.mock.mockImplementation(async (workingDir: string, ...args: string[]) => {
          if (args[0] === 'update') {
            yamlAtUpdateCall = await fs.readFile(join(workingDir, 'pnpm-workspace.yaml'), 'utf8');
          }
        });

        await scoutInstall(dir, {updateMode: updateMode.ALL});

        assert.equal(
          yamlAtUpdateCall,
          'packages:\n' +
          '  - pkg-a\n' +
          'scout:\n' +
          '  overrides: &scout-overrides\n' +
          '    lodash: 4.17.21\n' +
          'overrides: {}\n'
        );
      });
    });

    it('does not disable scout overrides before pnpm update in SNAPSHOTS mode', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        let yamlAtUpdateCall: string;
        pnpmForkMock.mock.mockImplementation(async (workingDir: string, ...args: string[]) => {
          if (args[0] === 'update') {
            yamlAtUpdateCall = await fs.readFile(join(workingDir, 'pnpm-workspace.yaml'), 'utf8');
          }
        });

        await scoutInstall(dir, {updateMode: updateMode.SNAPSHOTS});

        assert.equal(
          yamlAtUpdateCall,
          'packages:\n' +
          '  - pkg-a\n' +
          'scout:\n' +
          '  overrides: &scout-overrides\n' +
          '    lodash: 4.17.21\n' +
          'overrides:\n' +
          '  <<: *scout-overrides\n'
        );
      });
    });

    it('calls pnpm update first with common arguments in REQUIRED mode', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        await scoutInstall(dir, {updateMode: updateMode.REQUIRED});

        assert.ok(pnpmForkMock.mock.callCount() > 0);
        assert.deepEqual(pnpmForkMock.mock.calls[0].arguments, [dir, 'update', '--no-save', '--recursive', '--no-lockfile', '--ignore-scripts', '--config.link-workspace-packages=true', '--config.prefer-workspace-packages=true']);
      });
    });

    it('calls pnpm update first with common arguments in ALL mode', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        await scoutInstall(dir, {updateMode: updateMode.ALL});

        assert.ok(pnpmForkMock.mock.callCount() > 0);
        assert.deepEqual(pnpmForkMock.mock.calls[0].arguments, [dir, 'update', '--no-save', '--recursive', '--no-lockfile', '--ignore-scripts', '--config.link-workspace-packages=true', '--config.prefer-workspace-packages=true']);
      });
    });

    it('calls pnpm update first with common arguments in SNAPSHOTS mode', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        await scoutInstall(dir, {updateMode: updateMode.SNAPSHOTS});

        assert.ok(pnpmForkMock.mock.callCount() > 0);
        assert.deepEqual(pnpmForkMock.mock.calls[0].arguments, [dir, 'update', '--no-save', '--recursive', '--no-lockfile', '--ignore-scripts', '--config.link-workspace-packages=true', '--config.prefer-workspace-packages=true']);
      });
    });

    it('calls pnpm install with disabled scout overrides in REQUIRED mode', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        let yamlAtInstallCall: string;
        pnpmForkMock.mock.mockImplementation(async (workingDir: string, ...args: string[]) => {
          if (args[0] === 'install') {
            yamlAtInstallCall = await fs.readFile(join(workingDir, 'pnpm-workspace.yaml'), 'utf8');
          }
        });

        await scoutInstall(dir, {updateMode: updateMode.REQUIRED});

        assert.equal(
          yamlAtInstallCall,
          'packages:\n' +
          '  - pkg-a\n' +
          'scout:\n' +
          '  overrides: &scout-overrides\n' +
          '    lodash: 4.17.21\n' +
          'overrides: {}\n'
        );

        assert.ok(pnpmForkMock.mock.callCount() > 1);
        assert.deepEqual(pnpmForkMock.mock.calls[1].arguments, [dir, 'install', '--recursive', '--no-lockfile', '--ignore-scripts', '--config.link-workspace-packages=true', '--config.prefer-workspace-packages=true']);
      });
    });

    it('does not call pnpm install in ALL mode', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        await scoutInstall(dir, {updateMode: updateMode.ALL});

        assert.equal(pnpmForkMock.mock.callCount(), 1);
        assert.notEqual(pnpmForkMock.mock.calls[0].arguments[1], 'install');
      });
    });

    it('does not call pnpm install in SNAPSHOTS mode', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        await scoutInstall(dir, {updateMode: updateMode.SNAPSHOTS});

        assert.equal(pnpmForkMock.mock.callCount(), 1);
        assert.notEqual(pnpmForkMock.mock.calls[0].arguments[1], 'install');
      });
    });

    it('updates scout overrides after all pnpm calls in REQUIRED mode', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        updateAllScoutOverridesMock.mock.mockImplementation(async (lockfileDir: string, convergenceLogLevel?: DependencyConvergenceLogLevel) => {
          assert.equal(pnpmForkMock.mock.callCount(), 2);
        });

        await scoutInstall(dir, {updateMode: updateMode.REQUIRED});

        assert.equal(pnpmForkMock.mock.callCount(), 2);
        assert.equal(updateAllScoutOverridesMock.mock.callCount(), 1);
      });
    });

    it('updates scout overrides after all pnpm calls in ALL mode', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        updateAllScoutOverridesMock.mock.mockImplementation(async (lockfileDir: string, convergenceLogLevel?: DependencyConvergenceLogLevel) => {
          assert.equal(pnpmForkMock.mock.callCount(), 1);
        });

        await scoutInstall(dir, {updateMode: updateMode.ALL});

        assert.equal(pnpmForkMock.mock.callCount(), 1);
        assert.equal(updateAllScoutOverridesMock.mock.callCount(), 1);
      });
    });

    it('does not update scout overrides in SNAPSHOTS mode', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        await scoutInstall(dir, {updateMode: updateMode.SNAPSHOTS});

        assert.equal(updateAllScoutOverridesMock.mock.callCount(), 0);
      });
    });

    it('uses SNAPSHOTS mode when called with default options', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        let yamlAtUpdateCall: string;
        pnpmForkMock.mock.mockImplementation(async (workingDir: string, ...args: string[]) => {
          if (args[0] === 'update') {
            yamlAtUpdateCall = await fs.readFile(join(workingDir, 'pnpm-workspace.yaml'), 'utf8');
          }
        });

        await scoutInstall(dir, {updateMode: updateMode.SNAPSHOTS});

        assert.equal(
          yamlAtUpdateCall,
          'packages:\n' +
          '  - pkg-a\n' +
          'scout:\n' +
          '  overrides: &scout-overrides\n' +
          '    lodash: 4.17.21\n' +
          'overrides:\n' +
          '  <<: *scout-overrides\n'
        );
        assert.equal(pnpmForkMock.mock.callCount(), 1);
        assert.equal(pnpmForkMock.mock.calls[0].arguments[1], 'update');
        assert.equal(updateAllScoutOverridesMock.mock.callCount(), 0);
      });
    });

    it('ensures scout overrides are re-enabled in REQUIRED mode if pnpm update fails', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        pnpmForkMock.mock.mockImplementation(async (workingDir: string, ...args: string[]) => {
          if (args[0] === 'update') {
            throw 'Pnpm update fails';
          }
        });

        try {
          await scoutInstall(dir, {updateMode: updateMode.REQUIRED});
        } catch (e) {
          // nop
        }

        assert.equal(
          await fs.readFile(join(dir, 'pnpm-workspace.yaml'), 'utf8'),
          'packages:\n' +
          '  - pkg-a\n' +
          'scout:\n' +
          '  overrides: &scout-overrides\n' +
          '    lodash: 4.17.21\n' +
          'overrides:\n' +
          '  <<: *scout-overrides\n'
        );
      });
    });

    it('ensures scout overrides are re-enabled in ALL mode if pnpm update fails', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        pnpmForkMock.mock.mockImplementation(async (workingDir: string, ...args: string[]) => {
          if (args[0] === 'update') {
            throw 'Pnpm update fails';
          }
        });

        try {
          await scoutInstall(dir, {updateMode: updateMode.ALL});
        } catch (e) {
          // nop
        }

        assert.equal(
          await fs.readFile(join(dir, 'pnpm-workspace.yaml'), 'utf8'),
          'packages:\n' +
          '  - pkg-a\n' +
          'scout:\n' +
          '  overrides: &scout-overrides\n' +
          '    lodash: 4.17.21\n' +
          'overrides:\n' +
          '  <<: *scout-overrides\n'
        );
      });
    });

    it('ensures scout overrides are re-enabled in SNAPSHOTS mode if pnpm update fails', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        pnpmForkMock.mock.mockImplementation(async (workingDir: string, ...args: string[]) => {
          if (args[0] === 'update') {
            throw 'Pnpm update fails';
          }
        });

        try {
          await scoutInstall(dir, {updateMode: updateMode.SNAPSHOTS});
        } catch (e) {
          // nop
        }

        assert.equal(
          await fs.readFile(join(dir, 'pnpm-workspace.yaml'), 'utf8'),
          'packages:\n' +
          '  - pkg-a\n' +
          'scout:\n' +
          '  overrides: &scout-overrides\n' +
          '    lodash: 4.17.21\n' +
          'overrides:\n' +
          '  <<: *scout-overrides\n'
        );
      });
    });

    it('ensures scout overrides are re-enabled in REQUIRED mode if pnpm install fails', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        pnpmForkMock.mock.mockImplementation(async (workingDir: string, ...args: string[]) => {
          if (args[0] === 'install') {
            throw 'Pnpm install fails';
          }
        });

        try {
          await scoutInstall(dir, {updateMode: updateMode.REQUIRED});
        } catch (e) {
          // nop
        }

        assert.equal(
          await fs.readFile(join(dir, 'pnpm-workspace.yaml'), 'utf8'),
          'packages:\n' +
          '  - pkg-a\n' +
          'scout:\n' +
          '  overrides: &scout-overrides\n' +
          '    lodash: 4.17.21\n' +
          'overrides:\n' +
          '  <<: *scout-overrides\n'
        );
      });
    });

    it('ensures scout overrides are re-enabled in REQUIRED mode if updating overrides fails', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        updateAllScoutOverridesMock.mock.mockImplementation(async (lockfileDir: string, convergenceLogLevel?: DependencyConvergenceLogLevel) => {
          throw 'Updating overrides fails';
        });

        try {
          await scoutInstall(dir, {updateMode: updateMode.REQUIRED});
        } catch (e) {
          // nop
        }

        assert.equal(
          await fs.readFile(join(dir, 'pnpm-workspace.yaml'), 'utf8'),
          'packages:\n' +
          '  - pkg-a\n' +
          'scout:\n' +
          '  overrides: &scout-overrides\n' +
          '    lodash: 4.17.21\n' +
          'overrides:\n' +
          '  <<: *scout-overrides\n'
        );
      });
    });

    it('ensures scout overrides are re-enabled in ALL mode if updating overrides fails', async () => {
      await withPnpmWorkspaceYaml(async dir => {
        updateAllScoutOverridesMock.mock.mockImplementation(async (lockfileDir: string, convergenceLogLevel?: DependencyConvergenceLogLevel) => {
          throw 'Updating overrides fails';
        });

        try {
          await scoutInstall(dir, {updateMode: updateMode.ALL});
        } catch (e) {
          // nop
        }

        assert.equal(
          await fs.readFile(join(dir, 'pnpm-workspace.yaml'), 'utf8'),
          'packages:\n' +
          '  - pkg-a\n' +
          'scout:\n' +
          '  overrides: &scout-overrides\n' +
          '    lodash: 4.17.21\n' +
          'overrides:\n' +
          '  <<: *scout-overrides\n'
        );
      });
    });
  });
});

async function withPnpmWorkspaceYaml(callback: (dir: string) => Promise<void>): Promise<void> {
  const dir = await fs.mkdtemp(join(tmpdir(), 'scout-install-test-'));
  try {
    await fs.writeFile(
      join(dir, 'pnpm-workspace.yaml'),
      'packages:\n' +
      '  - pkg-a\n' +
      'scout:\n' +
      '  overrides: &scout-overrides\n' +
      '    lodash: 4.17.21\n' +
      'overrides:\n' +
      '  <<: *scout-overrides\n'
    );
    await callback(dir);
  } finally {
    await fs.rm(dir, {recursive: true});
  }
}
