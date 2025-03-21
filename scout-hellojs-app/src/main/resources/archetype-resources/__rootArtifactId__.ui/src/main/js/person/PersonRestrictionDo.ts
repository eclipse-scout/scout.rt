import {BaseDoEntity, typeName} from '@eclipse-scout/core';

@typeName('${simpleArtifactName}.PersonRestriction')
export class PersonRestrictionDo extends BaseDoEntity {
  firstName: string;
  lastName: string;
}
