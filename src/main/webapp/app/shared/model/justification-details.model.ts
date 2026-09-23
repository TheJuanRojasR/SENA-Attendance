import dayjs from 'dayjs';

import { IClassSection } from 'app/shared/model/class-section.model';
import { StateJustification } from 'app/shared/model/enumerations/state-justification.model';
import { IJustification } from 'app/shared/model/justification.model';

export interface IJustificationDetails {
  id?: string;
  stateJustification?: keyof typeof StateJustification | null;
  rejectionReason?: string;
  correctionText?: string;
  correctionFileUrlContentType?: string;
  correctionFileUrl?: string;
  responseDate?: dayjs.Dayjs;
  requestDate?: dayjs.Dayjs;
  outOfTimeReason?: string;
  lateDecision?: boolean;
  classSection?: IClassSection;
  justification?: IJustification;
}

export const defaultValue: Readonly<IJustificationDetails> = {};
