#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.persistence.person;

import static ${package}.persistence.JooqSqlService.jooq;
import static org.jooq.impl.DSL.noCondition;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.BEANS;
import org.jooq.Condition;
import org.jooq.Field;

import ${package}.data.person.IPersonRepository;
import ${package}.data.person.PersonDo;
import ${package}.data.person.PersonRestrictionDo;
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
  public Stream<PersonDo> list(PersonRestrictionDo restrictions, int numberOfRows) {
    Person personTab = Person.PERSON.as("p");
    Condition firstNameRestriction = StringUtility.hasText(restrictions.getFirstName())
      ? personTab.FIRST_NAME.likeIgnoreCase('%' + restrictions.getFirstName() + '%')
      : noCondition();
    Condition lastNameRestriction = StringUtility.hasText(restrictions.getLastName())
      ? personTab.LAST_NAME.likeIgnoreCase('%' + restrictions.getLastName() + '%')
      : noCondition();
    return jooq()
      .select()
      .from(personTab)
      .where(firstNameRestriction, lastNameRestriction)
      .limit(numberOfRows)
      .fetchStream()
      .map(r -> r.into(personTab))
      .map(this::recToDo);
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

  protected PersonDo recToDo(PersonRecord personRecord) {
    return fromRecordToDo(personRecord, BEANS.get(PersonDo.class));
  }

  protected PersonRecord doToRec(PersonDo person) {
    return fromDoToRecord(person, new PersonRecord());
  }

  @Override
  protected DoEntityBeanMappings<PersonDo, PersonRecord> mappings() {
    return new DoEntityBeanMappings<PersonDo, PersonRecord>().with(PersonDo::personId, PersonRecord::getPersonId)
        .with(PersonDo::lastName, PersonRecord::getLastName, PersonRecord::setLastName)
        .with(PersonDo::firstName, PersonRecord::getFirstName, PersonRecord::setFirstName)
        .with(PersonDo::salary, PersonRecord::getSalary, PersonRecord::setSalary)
        .with(PersonDo::external, PersonRecord::getExternal, PersonRecord::setExternal);
  }
}
