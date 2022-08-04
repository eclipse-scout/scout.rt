import {scout} from '@eclipse-scout/core';
import {Person, PersonForm} from '../../../main/js';

describe('PersonForm', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        // Bench is not visible by default for sandbox sessions.
        // It is required here because forms with display style view are opened
        benchVisible: true
      }
    });
  });

  describe('open with person', () => {

    it('shows firstName and LastName', done => {
      let personForm = scout.create(PersonForm, {
        parent: session.desktop
      });

      let person = scout.create(Person, {
        firstName: 'first',
        lastName: 'last'
      });
      personForm.setData(person);
      personForm.open()
        .then(() => {
          expect(personForm.firstNameField.rendered).toBe(true);
          expect(personForm.lastNameField.rendered).toBe(true);
          expect(personForm.firstNameField.value).toBe(person.firstName);
          expect(personForm.lastNameField.value).toBe(person.lastName);
          personForm.close();
        })
        .catch(fail)
        .always(done);
    });
  });
});
