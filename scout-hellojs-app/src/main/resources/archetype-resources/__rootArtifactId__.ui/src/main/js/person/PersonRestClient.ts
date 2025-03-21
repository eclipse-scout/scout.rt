import {AbstractRestClient, PersonDo, PersonRestrictionDo} from '../index';
import {systems} from '@eclipse-scout/core';

export class PersonRestClient extends AbstractRestClient {

  static DATA_TYPE = 'person';

  constructor() {
    super(PersonRestClient.DATA_TYPE, systems.getOrCreate().getEndpointUrl('persons', 'persons') + '/');
  }

  /**
   * Loads a single person.
   * @param id The id of the person to fetch. Must not be null.
   */
  load(id: string): JQuery.Promise<PersonDo> {
    return this._loadItem(id);
  }

  /**
   * Gets all persons matching the given restrictions.
   * @param restriction Filters which persons to fetch. Must not be null.
   * @returns The persons matching the restriction
   */
  list(restriction: PersonRestrictionDo): JQuery.Promise<PersonDo[]> {
    return this._listItems(restriction);
  }

  /**
   * Creates a new person.
   * @param person The person to create. Must not be null.
   * @returns the created person
   */
  create(person: PersonDo): JQuery.Promise<PersonDo> {
    return this._createItem(person);
  }

  /**
   * Updates an existing person.
   * @param person The person to update. Must not be null.
   * @returns The updated person
   */
  store(person: PersonDo): JQuery.Promise<PersonDo> {
    return this._storeItem(person.id, person);
  }

  /**
   * Deletes a person.
   * @param id The id of the person to delete. Must not be null.
   */
  remove(id: string): JQuery.Promise<void> {
    return this._removeItem(id);
  }
}
