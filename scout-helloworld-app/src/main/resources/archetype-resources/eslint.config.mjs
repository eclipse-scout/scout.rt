import {defineConfig, globalIgnores} from 'eslint/config';
import scoutConfig from '@eclipse-scout/eslint-config';

export default defineConfig([
  scoutConfig,
  globalIgnores([
    '.git',
    '.idea',
    '*/dist',
    '*/target',
    '*/src/main/resources',
    '*/src/test/resources',
    '*/src/main/java',
    '*/src/test/java'
  ]),
  {
    rules: {
      'linebreak-style': 'off'
    }
  }
]);
