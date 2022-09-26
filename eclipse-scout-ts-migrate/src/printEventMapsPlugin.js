import jscodeshift from 'jscodeshift';
import {findClassProperty, findParentClassBody, getNameForType, isOneOf} from './common.js';

const j = jscodeshift.withParser('ts');

/**
 * @type import('ts-migrate-server').Plugin<{}>
 */
const printEventMapsPlugin = {
  name: 'print-event-maps-plugin',

  async run({text, fileName, options}) {
    let root = j(text);

    let eventLines = new Set();
    let propEvents = new Map();
    let className = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf('.'));

    root.find(j.CallExpression)
      .filter(path =>
        path.value.callee.type === 'MemberExpression'
        && path.value.callee.object.type === 'ThisExpression'
        && isOneOf(path.value.callee.property.name, 'setProperty', '_setProperty', 'trigger')
      )
      .forEach(expression => {
        let funcName = expression.value.callee.property.name;
        let eventName = expression.value.arguments[0]?.value;
        if (isOneOf(funcName, 'setProperty', '_setProperty')) {
          let type = '?';
          let property = findClassProperty(findParentClassBody(expression), eventName);
          if (property) {
            let typeAnnotation = property.typeAnnotation;
            type = typeAnnotation ? getNameForType(j, typeAnnotation?.typeAnnotation) || '?' : '?';
          }
          let value = propEvents.get(eventName);
          if (value && !value.includes('<?>')) {
            // Don't replace existing value if it already has a type
            return;
          }
          propEvents.set(eventName, `'propertyChange:${eventName}': PropertyChangeEvent<${type}, ${className}>;`);
        } else {
          let eventObj = expression.value.arguments[1];
          let event = '?';
          if (!eventObj) {
            event = `Event<${className}>`;
          }
          if (!eventName) {
            return;
          }
          eventLines.add(`'${eventName}': ${event};`);
        }
      });

    if (eventLines.size > 0 || propEvents.size > 0) {
      console.log(`${className}EventMap`);
      if (eventLines.size > 0) {
        console.log(Array.from(eventLines).sort().join('\n'));
      }
      if (propEvents.size > 0) {
        console.log(Array.from(propEvents.values()).sort().join('\n'));
      }
      console.log('\n');
    }

    return text;
  }
};

export default printEventMapsPlugin;
