/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, Person, PersonRestriction, Repository} from '../index';
import {App as ScoutApp, scout} from '@eclipse-scout/core';

let repository: PersonRepository;
export class PersonRepository extends Repository {
  constructor() {
    super();

    this.entityType = Person.ENTITY_TYPE;
    let app = ScoutApp.get() as App;
    this.targetUrl = app.apiUrl + 'persons/';
  }

  /**
   * Loads a single person.
   * @param personId The id of the person to fetch. Must not be null.
   */
  load(personId: string): JQuery.Promise<Person> {
    return this._load(this.targetUrl + personId);
  }

  /**
   * Gets all persons.
   * @param restriction Restriction which persons to fetch
   * @returns The persons matching the restriction
   */
  list(restriction: PersonRestriction): JQuery.Promise<Person[]> {
    return this._list(restriction);
  }

  /**
   * Updates an existing person.
   * @param person The person to update
   * @returns The updated person
   */
  store(person: Person): JQuery.Promise<Person> {
    return this._store(person, this.targetUrl + person.personId);
  }

  /**
   * Deletes a person.
   * @param personId The id of the person to delete.
   */
  remove(personId: string): JQuery.Promise<void> {
    return this._remove(personId);
  }

  /**
   * Creates a new person.
   * @param person The person to create
   * @returns the created person
   */
  create(person: Person): JQuery.Promise<Person> {
    return this._create(person);
  }

  static get(): PersonRepository {
    if (!repository) {
      repository = scout.create(PersonRepository);
    }
    return repository;
  }
}
