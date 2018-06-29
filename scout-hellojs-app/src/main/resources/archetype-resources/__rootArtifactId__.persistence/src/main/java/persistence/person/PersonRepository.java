#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.persistence.person;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.BEANS;
import org.jooq.Field;

import ${package}.data.person.IPersonRepository;
import ${package}.data.person.PersonDo;
import ${package}.persistence.common.AbstractRepository;
import ${package}.persistence.common.DoEntityBeanMappings;
import ${package}.persistence.tables.Person;
import ${package}.persistence.tables.records.PersonRecord;

public class PersonRepository extends AbstractRepository<Person, PersonRecord, PersonDo> implements IPersonRepository {

  @Override
  public Person getTable() {
    return Person.PERSON;
  }

  @Override
  public Field<String> getIdColumn() {
    return Person.PERSON.PERSON_ID;
  }

  @Override
  public void store(String id, PersonDo person) {
    super.store(id, doToRec(person));
  }

  @Override
  public Stream<PersonDo> list() {
    return getAll().map(this::recToDo);
  }

  @Override
  public Optional<PersonDo> getById(String personId) {
    return get(personId).map(this::recToDo);
  }

  @Override
  public PersonDo create(PersonDo person) {
    PersonRecord newPersonRecord = newRecord();
    String newPersonId = UUID.randomUUID().toString();

    fromDoToRecord(person, newPersonRecord)
        .setPersonId(newPersonId);
    newPersonRecord.store();
    return fromRecordToDo(newPersonRecord, person);
  }

  protected PersonDo recToDo(PersonRecord PersonRecord) {
    return fromRecordToDo(PersonRecord, BEANS.get(PersonDo.class));
  }

  protected PersonRecord doToRec(PersonDo person) {
    return fromDoToRecord(person, new PersonRecord());
  }

  @Override
  protected DoEntityBeanMappings<PersonDo, PersonRecord> mappings() {
    return new DoEntityBeanMappings<PersonDo, PersonRecord>()
        .with(PersonDo::personId, PersonRecord::getPersonId) // read-only (primary key)
        .with(PersonDo::lastName, PersonRecord::getLastName, PersonRecord::setLastName)
        .with(PersonDo::firstName, PersonRecord::getFirstName, PersonRecord::setFirstName);
  }
}
