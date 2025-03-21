import {typeName} from '@eclipse-scout/core';
import {AbstractItemResponse, PersonDo} from '../index';

@typeName('${simpleArtifactName}.PersonResponse')
export class PersonResponse extends AbstractItemResponse<PersonDo> {
}
