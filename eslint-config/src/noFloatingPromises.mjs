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
import tseslint from 'typescript-eslint';

const baseRule = tseslint.plugin.rules['no-floating-promises'];

/**
 * Methods whose promise does not need to be awaited in most cases, restricted to the class (or a subclass) declaring them.
 * Entries configured with the option `allowForKnownSafeMethods` are added to these.
 */
export const defaultKnownSafeMethods = [
  {className: 'ValueField', methods: ['setValue']}, // ValueField.setValue() returns Promise<void> | void
  {className: 'ErrorHandler', methods: ['handle']}, // resolves when the message box is closed
  {className: 'Form', methods: ['open']} // resolves when the form is opened
];

const knownSafeMethodsSchema = {
  type: 'array',
  items: {
    type: 'object',
    additionalProperties: false,
    properties: {
      className: {type: 'string'},
      methods: {type: 'array', items: {type: 'string'}, minItems: 1, uniqueItems: true}
    },
    required: ['className', 'methods']
  }
};

/**
 * Same as @typescript-eslint/no-floating-promises (all its options are supported), but
 * - ignores promises in event handlers: nobody waits for an event handler, so a floating promise is fine there (errors still reach the global unhandled rejection handler).
 *   An event handler is recognized by Scout's naming convention (a method or property named '_onXyz', e.g. _onValueFieldValueChange)
 *   or by being a function passed to on()/one() directly.
 * - ignores calls of known safe methods, see {@link defaultKnownSafeMethods}. Projects can add their own with the option `allowForKnownSafeMethods`:
 *   ```
 *   '@eclipse-scout/no-floating-promises': ['warn', {allowForKnownSafeMethods: [{className: 'MyForm', methods: ['reload']}]}]
 *   ```
 *   The option `allowForKnownSafeCalls` of the original rule can't be used for this: it only matches the method name, regardless of the class
 *   (its 'package' variant requires declaration files in node_modules, which is not the case for modules referenced as sources).
 */
export default {
  meta: {
    ...baseRule.meta,
    docs: {
      ...baseRule.meta.docs,
      description: 'Require Promise-like statements to be handled appropriately, except in event handlers and for known safe methods'
    },
    schema: [{
      ...baseRule.meta.schema[0],
      properties: {
        ...baseRule.meta.schema[0].properties,
        allowForKnownSafeMethods: knownSafeMethodsSchema
      }
    }]
  },
  defaultOptions: [{
    ...baseRule.defaultOptions[0],
    allowForKnownSafeMethods: []
  }],
  create(context) {
    const {allowForKnownSafeMethods, ...baseOptions} = context.options[0] || {};
    const knownSafeMethods = [...defaultKnownSafeMethods, ...(allowForKnownSafeMethods || [])];

    // Delegate to the original rule (without the additional option) but drop some of its reports.
    // ESLint freezes the context, so create a derived object instead of modifying it.
    const filteringContext = Object.create(context, {
      options: {
        value: [baseOptions]
      },
      report: {
        value: descriptor => {
          if (isInEventHandler(descriptor.node) || isKnownSafeCall(descriptor.node, knownSafeMethods, context)) {
            return;
          }
          context.report(descriptor);
        }
      }
    });
    return baseRule.create(filteringContext);
  }
};

function isInEventHandler(node) {
  for (let current = node; current; current = current.parent) {
    if (!isFunction(current)) {
      continue;
    }
    // method or property: _onXyz() {...}, _onXyz = () => {...}
    let parent = current.parent;
    if ((parent?.type === 'MethodDefinition' || parent?.type === 'PropertyDefinition' || parent?.type === 'Property') && isEventHandlerName(parent.key)) {
      return true;
    }
    // callback passed directly to on()/one(): widget.on('propertyChange:value', event => {...})
    if (parent?.type === 'CallExpression' && parent.arguments.includes(current) && isEventRegistration(parent.callee)) {
      return true;
    }
  }
  return false;
}

function isFunction(node) {
  return node.type === 'FunctionExpression' || node.type === 'ArrowFunctionExpression' || node.type === 'FunctionDeclaration';
}

function isEventHandlerName(key) {
  return key?.type === 'Identifier' && /^_?on[A-Z]/.test(key.name);
}

function isEventRegistration(callee) {
  return callee.type === 'MemberExpression' && callee.property.type === 'Identifier' && ['on', 'one'].includes(callee.property.name);
}

/**
 * @param node the reported ExpressionStatement
 */
function isKnownSafeCall(node, knownSafeMethods, context) {
  const expression = node.type === 'ExpressionStatement' ? node.expression : node;
  const services = context.sourceCode.parserServices;
  if (!services?.program) {
    return false;
  }
  return isKnownSafeExpression(expression, knownSafeMethods, services);
}

function isKnownSafeExpression(expression, knownSafeMethods, services) {
  switch (expression?.type) {
    case 'ChainExpression':
      return isKnownSafeExpression(expression.expression, knownSafeMethods, services);
    case 'LogicalExpression':
      // e.g. error && errorHandler.handle(error) -> only the right side can produce the promise
      return isKnownSafeExpression(expression.right, knownSafeMethods, services);
    case 'ConditionalExpression':
      return isKnownSafeExpression(expression.consequent, knownSafeMethods, services) && isKnownSafeExpression(expression.alternate, knownSafeMethods, services);
    case 'CallExpression':
      return isKnownSafeMethodCall(expression, knownSafeMethods, services);
    default:
      return false;
  }
}

function isKnownSafeMethodCall(callExpression, knownSafeMethods, services) {
  const callee = callExpression.callee;
  if (callee.type !== 'MemberExpression' || callee.property.type !== 'Identifier') {
    return false;
  }
  const methodName = callee.property.name;
  const candidates = knownSafeMethods.filter(entry => entry.methods.includes(methodName));
  if (!candidates.length) {
    return false;
  }
  const checker = services.program.getTypeChecker();
  const tsNode = services.esTreeNodeToTSNodeMap.get(callee.object);
  const type = checker.getTypeAtLocation(tsNode);
  return candidates.some(entry => isInstanceOf(type, entry.className, checker));
}

/**
 * @returns true if the given type is the class with the given name or a subclass of it (all members, if the type is a union)
 */
function isInstanceOf(type, className, checker) {
  if (type.isUnion()) {
    return type.types.every(t => isInstanceOf(t, className, checker));
  }
  const queue = [type];
  const visited = new Set();
  while (queue.length) {
    let current = queue.shift();
    if (!current || visited.has(current)) {
      continue;
    }
    visited.add(current);
    if (current.isTypeParameter()) {
      // e.g. polymorphic 'this' inside a class
      queue.push(checker.getBaseConstraintOfType(current));
      continue;
    }
    if (current.getSymbol()?.getName() === className) {
      return true;
    }
    const target = current.target ?? current; // generic instantiation, e.g. ValueField<string> -> ValueField
    if (target.isClassOrInterface()) {
      queue.push(...checker.getBaseTypes(target));
    }
  }
  return false;
}
