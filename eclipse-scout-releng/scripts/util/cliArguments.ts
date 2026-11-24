/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {parseArgs} from 'node:util';

/**
 * Option types. `object` is an option with nested values given using dots (e.g. `--mapping.0.regex x`).
 */
export type OptionType = 'string' | 'boolean' | 'number' | 'object';
export type OptionDefinitions = Record<string, { type: OptionType; default?: unknown }>;
export type ParsedArguments = { positionals: string[]; values: Record<string, any> };

/**
 * Parses command line arguments.
 *
 * - Options may be given as `--name value` or `--name=value`.
 * - Boolean options may be given as `--name`, `--name=false` or `--name false`.
 * - Number options are converted to numbers. A value that is not a number fails.
 * - Option names containing dots create nested objects of an `object` option, e.g. `--mapping.0.regex x` results in `{mapping: {0: {regex: 'x'}}}`.
 *   They take the following argument as value, if there is one, `true` otherwise.
 * - Options not contained in the given definitions take a value given as `--name=value`, `true` otherwise. The following argument is no value but a positional.
 * - Default values of the given definitions are not applied.
 *
 * @param args the arguments without the node executable and script, e.g. `process.argv.slice(2)`.
 */
export function parseArguments(args: string[], definitions: OptionDefinitions): ParsedArguments {
  const parseArgsOptions = Object.fromEntries(Object.entries(definitions)
    .filter(([, {type}]) => type !== 'object')
    .map(([name, {type}]) => [name, {type: type === 'boolean' ? 'boolean' as const : 'string' as const}]));
  const {tokens} = parseArgs({args, options: parseArgsOptions, strict: false, allowPositionals: true, tokens: true});

  const positionals: string[] = [];
  const values: Record<string, any> = {};
  for (let i = 0; i < tokens.length; i++) {
    const token = tokens[i];
    if (token.kind === 'positional') {
      positionals.push(token.value);
      continue;
    }
    if (token.kind !== 'option') {
      continue;
    }

    const type = definitions[token.name]?.type;
    const next = tokens[i + 1];
    let value: unknown = token.value;
    if (type === 'boolean') {
      if (value === undefined && next?.kind === 'positional' && (next.value === 'true' || next.value === 'false')) {
        value = next.value;
        i++;
      }
      value = value !== 'false';
    } else if (type === 'number') {
      value = Number(token.value);
      if (!token.value?.trim() || Number.isNaN(value)) {
        throw new Error(`Option --${token.name} requires a number but got '${token.value ?? ''}'.`);
      }
    } else if (type === 'string') {
      value ??= '';
    } else if (value === undefined && next?.kind === 'positional' && definitions[token.name.split('.')[0]]?.type === 'object') {
      value = next.value;
      i++;
    } else {
      value ??= true;
    }
    setValue(values, token.name, value);
  }
  return {positionals, values};
}

/**
 * @returns the names of the given values (see {@link parseArguments}) that are not contained in the given definitions.
 */
export function getUnknownOptions(values: Record<string, unknown>, definitions: OptionDefinitions): string[] {
  return Object.keys(values).filter(name => !(name in definitions));
}

/**
 * @returns the default values of the given definitions.
 */
export function getDefaultValues(definitions: OptionDefinitions): Record<string, unknown> {
  return Object.fromEntries(Object.entries(definitions)
    .filter(([, definition]) => 'default' in definition)
    .map(([name, definition]) => [name, definition.default]));
}

/**
 * Sets the given value in the given object. A name containing dots (e.g. `mapping.0.regex`) creates nested objects.
 */
function setValue(values: Record<string, any>, name: string, value: unknown) {
  const path = name.split('.');
  const last = path.pop();
  let target = values;
  for (const key of path) {
    if (typeof target[key] !== 'object' || target[key] === null) {
      target[key] = {};
    }
    target = target[key];
  }
  target[last] = value;
}
