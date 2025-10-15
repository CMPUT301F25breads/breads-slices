# Object-Oriented Analysis (CRC Cards)

---

## CRC Card Template

## Class Name

### Responsibilities
- Responsibility

### Collaborators
- Collaborator

---

## Class: DBConnector
### Responsibilities
- Handle communication from the application to the database
- Provide uniform interface for all data accesses
### Collaborators
- Good question


## Class: Authenticator
### Responsibilities
- Handle authentication of different roles
### Collaborators
- DBConnector

## Class: Entrant
### Responsibilities
- Manage user info
- Join/leave events
- Track event history
- Manage notification preferences
### Collaborators
- Authenticator
- DBConnector
- Event
- Notifier
- Profile
- Organizer

## Class: Organizer
### Responsibilities
- Create events
- Update events
- Set lottery parameters
- View events
- Modify event entrants
- Export list of entrants
- Send notifications
### Collaborators
- Authenticator
- DBConnector
- Event
- Notifier
- Entrant

## Class: Administrator
### Responsibilities
- 
### Collaborators
-

## Class: Waitlist
### Responsibilities
- Store entrants
- Relate to an event
- Viewed by Entrant
- Viewed by Organizer
- Be polled by lottery
### Collaborators
- Entrant
- Event
- Organizer
- Lottery

## Class: Event
### Responsibilities
- 
### Collaborators
- 

## Class: Notifier
### Responsibilities
- Create notification object with passed parameters
- 
### Collaborators


## Class: Notification
### Responsibilities
- Represent single notification object
### Collaborators
- Notifier
- Entrant

## Class: Log
### Description
Probably an abstract for other log types
### Responsibilities

### Class: LogItem
### Description
Probably an abstract for other log item types like the entrant log, notification log, etc

## Class: Profile
### Responsibilities
- 
### Collaborators
- 

## Class: Lottery
### Responsibilities
-
### Collaborators
-

## Anticipated other classes
- 
