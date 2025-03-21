import {scout} from '@eclipse-scout/core';
import {PersonDo} from '../../../main/js';

describe('PersonDo', () => {
  it('initializes correctly from model', () => {
    let values = ['id', 'first', 'last'];
    let person = scout.create(PersonDo, {
      id: values[0],
      firstName: values[1],
      lastName: values[2],
    });

    expect(person.id).toBe(values[0]);
    expect(person.firstName).toBe(values[1]);
    expect(person.lastName).toBe(values[2]);
  });
});
