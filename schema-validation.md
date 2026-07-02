# Schema Validation

## Apprentice

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "created_by",
      "created_date",
      "grade",
      "last_modified_by",
      "last_modified_date",
      "state_academic",
      "student"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "grade": {
        "bsonType": "dbPointer"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "state_academic": {
        "bsonType": "string"
      },
      "student": {
        "bsonType": "dbPointer"
      }
    }
  }
}
```

## Attendance

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "auditLogs",
      "classSection",
      "created_by",
      "created_date",
      "date",
      "last_modified_by",
      "last_modified_date",
      "state_attendance",
      "student"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "auditLogs": {
        "bsonType": "array"
      },
      "classSection": {
        "bsonType": "dbPointer"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "date": {
        "bsonType": "date"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "state_attendance": {
        "bsonType": "string"
      },
      "student": {
        "bsonType": "dbPointer"
      }
    }
  }
}
```

## Authority

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class"
    ],
    "properties": {
      "_id": {
        "bsonType": "string"
      },
      "_class": {
        "bsonType": "string"
      }
    }
  }
}
```

## ClassException

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "classSection",
      "created_by",
      "created_date",
      "date",
      "last_modified_by",
      "last_modified_date",
      "reason"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "classSection": {
        "bsonType": "dbPointer"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "date": {
        "bsonType": "date"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "reason": {
        "bsonType": "string"
      }
    }
  }
}
```

## ClassSchedule

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "classSection",
      "created_by",
      "created_date",
      "day_of_week",
      "end_time",
      "last_modified_by",
      "last_modified_date",
      "start_time",
      "trimester"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "classSection": {
        "bsonType": "dbPointer"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "day_of_week": {
        "bsonType": "string"
      },
      "end_time": {
        "bsonType": "date"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "start_time": {
        "bsonType": "date"
      },
      "trimester": {
        "bsonType": "dbPointer"
      }
    }
  }
}
```

## ClassSection

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "created_by",
      "created_date",
      "exceptions",
      "grade",
      "instructor",
      "is_active",
      "last_modified_by",
      "last_modified_date",
      "schedules",
      "subject_name"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "exceptions": {
        "bsonType": "array"
      },
      "grade": {
        "bsonType": "dbPointer"
      },
      "instructor": {
        "bsonType": "dbPointer"
      },
      "is_active": {
        "bsonType": "bool"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "schedules": {
        "bsonType": "array"
      },
      "subject_name": {
        "bsonType": "string"
      }
    }
  }
}
```

## DocumentType

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "created_by",
      "created_date",
      "initials",
      "last_modified_by",
      "last_modified_date",
      "name"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "initials": {
        "bsonType": "string"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "name": {
        "bsonType": "string"
      }
    }
  }
}
```

## GlobalConfiguration

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "created_by",
      "created_date",
      "instructor_response_days",
      "last_modified_by",
      "last_modified_date",
      "late_arrivals_to_fail",
      "max_postponement_justifications",
      "standard_trimester_months",
      "student_justification_days"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "instructor_response_days": {
        "bsonType": "int"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "late_arrivals_to_fail": {
        "bsonType": "int"
      },
      "max_postponement_justifications": {
        "bsonType": "int"
      },
      "standard_trimester_months": {
        "bsonType": "int"
      },
      "student_justification_days": {
        "bsonType": "int"
      }
    }
  }
}
```

## Grade

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "code",
      "created_by",
      "created_date",
      "end_date",
      "last_modified_by",
      "last_modified_date",
      "modality",
      "program",
      "start_date",
      "state",
      "timeSlot"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "code": {
        "bsonType": "string"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "end_date": {
        "bsonType": "date"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "modality": {
        "bsonType": "dbPointer"
      },
      "program": {
        "bsonType": "dbPointer"
      },
      "start_date": {
        "bsonType": "date"
      },
      "state": {
        "bsonType": "string"
      },
      "timeSlot": {
        "bsonType": "dbPointer"
      }
    }
  }
}
```

## Justification

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "created_by",
      "created_date",
      "description",
      "details",
      "end_date",
      "evidence",
      "evidence_content_type",
      "justificationType",
      "last_modified_by",
      "last_modified_date",
      "start_date",
      "student"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "description": {
        "bsonType": "string"
      },
      "details": {
        "bsonType": "array"
      },
      "end_date": {
        "bsonType": "date"
      },
      "evidence": {
        "bsonType": "binData"
      },
      "evidence_content_type": {
        "bsonType": "string"
      },
      "justificationType": {
        "bsonType": "dbPointer"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "start_date": {
        "bsonType": "date"
      },
      "student": {
        "bsonType": "dbPointer"
      }
    }
  }
}
```

## JustificationType

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "created_by",
      "created_date",
      "last_modified_by",
      "last_modified_date",
      "limit_per_trimester",
      "name",
      "state"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "limit_per_trimester": {
        "bsonType": "int"
      },
      "name": {
        "bsonType": "string"
      },
      "state": {
        "bsonType": "string"
      }
    }
  }
}
```

## Modality

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "created_by",
      "created_date",
      "is_active",
      "last_modified_by",
      "last_modified_date",
      "name"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "is_active": {
        "bsonType": "bool"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "name": {
        "bsonType": "string"
      }
    }
  }
}
```

## Program

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "code",
      "created_by",
      "created_date",
      "initials",
      "last_modified_by",
      "last_modified_date",
      "name",
      "trimesters"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "code": {
        "bsonType": "string"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "initials": {
        "bsonType": "string"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "name": {
        "bsonType": "string"
      },
      "trimesters": {
        "bsonType": "int"
      }
    }
  }
}
```

## TimeSlot

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "created_by",
      "created_date",
      "end_time",
      "is_active",
      "last_modified_by",
      "last_modified_date",
      "name",
      "start_time"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "end_time": {
        "bsonType": "date"
      },
      "is_active": {
        "bsonType": "bool"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "name": {
        "bsonType": "string"
      },
      "start_time": {
        "bsonType": "date"
      }
    }
  }
}
```

## Trimester

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "created_by",
      "created_date",
      "end_date",
      "last_modified_by",
      "last_modified_date",
      "name",
      "start_date",
      "state"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "end_date": {
        "bsonType": "date"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "name": {
        "bsonType": "string"
      },
      "start_date": {
        "bsonType": "date"
      },
      "state": {
        "bsonType": "string"
      }
    }
  }
}
```

## User

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "activated",
      "authorities",
      "created_by",
      "created_date",
      "email",
      "lang_key",
      "last_modified_by",
      "last_modified_date",
      "login",
      "password"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "activated": {
        "bsonType": "bool"
      },
      "authorities": {
        "bsonType": "array",
        "items": {
          "bsonType": "object",
          "properties": {
            "_id": {
              "bsonType": "string"
            }
          },
          "required": [
            "_id"
          ]
        }
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "email": {
        "bsonType": "string"
      },
      "first_name": {
        "bsonType": "string"
      },
      "lang_key": {
        "bsonType": "string"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "last_name": {
        "bsonType": "string"
      },
      "login": {
        "bsonType": "string"
      },
      "password": {
        "bsonType": "string"
      }
    }
  }
}
```

## UserProfile

```
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": [
      "_id",
      "_class",
      "created_by",
      "created_date",
      "document_number",
      "documentType",
      "first_last_name",
      "first_name",
      "last_modified_by",
      "last_modified_date",
      "middle_name",
      "phone_number",
      "second_last_name",
      "user"
    ],
    "properties": {
      "_id": {
        "bsonType": "objectId"
      },
      "_class": {
        "bsonType": "string"
      },
      "created_by": {
        "bsonType": "string"
      },
      "created_date": {
        "bsonType": "date"
      },
      "document_number": {
        "bsonType": "string"
      },
      "documentType": {
        "bsonType": "dbPointer"
      },
      "first_last_name": {
        "bsonType": "string"
      },
      "first_name": {
        "bsonType": "string"
      },
      "last_modified_by": {
        "bsonType": "string"
      },
      "last_modified_date": {
        "bsonType": "date"
      },
      "middle_name": {
        "bsonType": "string"
      },
      "phone_number": {
        "bsonType": "string"
      },
      "second_last_name": {
        "bsonType": "string"
      },
      "user": {
        "bsonType": "dbPointer"
      }
    }
  }
}
```
