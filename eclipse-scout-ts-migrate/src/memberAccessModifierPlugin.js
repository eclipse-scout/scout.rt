import jscodeshift from 'jscodeshift';
import {defaultRecastOptions} from './common.js';

const j = jscodeshift.withParser('ts');

/**
 * @type import('ts-migrate-server').Plugin<unknown>
 */
const memberAccessModifierPlugin = {
  name: 'member-access-modifier-plugin',

  async run({text}) {
    const root = j(text);
    // Declaration is base type for ClassMethod and ClassProperty according to https://github.com/benjamn/ast-types/tree/master/def
    return root.find(j.Declaration)
      .filter(path => path.node.type === j.ClassMethod.name || path.node.type === j.ClassProperty.name)
      .forEach(expression => {
        if (expression.node.accessibility) {
          return;
        }
        let name = expression.node.key.name;
        if (name && name.startsWith('_')) {
          expression.node.accessibility = 'protected';
        }
      }).toSource(defaultRecastOptions);
  }
};

export default memberAccessModifierPlugin;
