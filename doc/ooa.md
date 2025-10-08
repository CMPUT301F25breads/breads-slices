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

## Class: Event
### Responsibilities
- 
### Collaborators
- 

## Class: Notifier
### Responsibilities
- 
### Collaborators
- 

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