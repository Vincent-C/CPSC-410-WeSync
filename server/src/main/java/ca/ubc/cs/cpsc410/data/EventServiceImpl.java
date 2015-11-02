package ca.ubc.cs.cpsc410.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ryan on 28/10/15.
 * <p>
 * Implementation of EventService interface
 */

@Service
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public EventServiceImpl(final EventRepository eventRepository, final UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Event createEvent(Event event) {
        if (event.getName() == null) {
            throw new RuntimeException("Error creating event: There is no event name specified!");
        }
        // These checks are unnecessary
        /*Date currentDate = new Date();
        if (event.getStartDate() == 0 || new Date(event.getStartDate()).before(currentDate)) {
            throw new RuntimeException("Error creating event: The start date has already passed or doesn't exist!");
        }
        if (event.getEndDate() == 0 || new Date(event.getEndDate()).before(new Date(event.getStartDate()))) {
            throw new RuntimeException("Error creating event: The event ends before it starts or doesn't exist!");
        }*/
        event.setType("wesync");
        List<User> existingUsers = userRepository.findAll();
        for (User existingUser : existingUsers) {
            if (existingUser.getUsername().equals(event.getHost())) {
                Event newEvent = eventRepository.save(event);
                existingUser.getEvents().add(newEvent.getId());
                userRepository.save(existingUser);
                return newEvent;
            }
        }
        throw new RuntimeException(String.format("Error creating event: Host %s does not match an existing user!", event.getHost()));
    }

    @Override
    public void cancelEvent(Event event) {
        List<User> existingUsers = userRepository.findAll();
        List<Event> existingEvents = eventRepository.findAll();
        for (Event existingEvent : existingEvents) {
            if (existingEvent.getId() == event.getId()) {
                for (User existingUser : existingUsers) {
                    if (existingEvent.getInvitees().contains(existingUser.getUsername())) {
                        int indexToRemove = existingUser.getPendingEvents().indexOf(existingEvent.getId());
                        existingUser.getPendingEvents().remove(indexToRemove);
                    }
                    if (existingUser.getEvents().contains(existingEvent.getId())) {
                        int indexToRemove = existingUser.getEvents().indexOf(existingEvent.getId());
                        existingUser.getEvents().remove(indexToRemove);
                    }
                    userRepository.save(existingUser);
                }
                existingEvent.setInvitees(null);
                eventRepository.delete(existingEvent);
                return;
            }
        }

        throw new RuntimeException(String.format(
                "Error: Event %d could not be found!", event.getId()));
    }

    @Override
    public Event finalizeEvent(Event event) {
        List<User> existingUsers = userRepository.findAll();
        List<Event> existingEvents = eventRepository.findAll();
        for (Event existingEvent : existingEvents) {
            if (existingEvent.getId() == event.getId()) {
                for (User existingUser : existingUsers) {
                    if (existingEvent.getInvitees().contains(existingUser.getUsername())) {
                        int indexToRemove = existingUser.getPendingEvents().indexOf(existingEvent.getId());
                        existingUser.getPendingEvents().remove(indexToRemove);
                        userRepository.save(existingUser);
                    }
                }
                existingEvent.setInvitees(null);            
                existingEvent.setIsFinalized(true);
                return eventRepository.save(existingEvent);
            }
        }
        throw new RuntimeException(String.format(
                "Error: Event %d could not be found!", event.getId()));
        
    }

    @Override
    public Event getEvent(Event event) {
        List<Event> existingEvents = eventRepository.findAll();
        for (Event existingEvent : existingEvents) {
            if (existingEvent.getId() == event.getId()) {
                return existingEvent;
            }
        }
        throw new RuntimeException(String.format("Error getting event: Event %s does not exist!", event.getId()));
    }

    @Override
    public Event addInvitees(List<Guest> guests) {
        // TODO: security check!!
        List<User> existingUsers = userRepository.findAll();
        List<Event> existingEvents = eventRepository.findAll();
        int eventId = guests.get(0).getEventId();
        // TODO: Fail if the eventsIds aren't the same
        Event eventToModify = null;
        for (Event existingEvent : existingEvents) {
            if (existingEvent.getId() == eventId) {
                eventToModify = existingEvent;
                break;
            }
        }
        if (eventToModify == null) {
            throw new RuntimeException(String.format(
                    "Error: Event %d does not exist!", eventId));
        }
        for (Guest guest : guests) {
           for (User existingUser : existingUsers) {
               if (!eventToModify.getInvitees().contains(guest.getUsername())) {
                   if (existingUser.getUsername().equals(guest.getUsername())) {
                       existingUser.getPendingEvents().add(guest.getEventId());
                       userRepository.save(existingUser);
                       eventToModify.getInvitees().add(guest.getUsername());
                   }
               }
           }
        }      
        return eventRepository.save(eventToModify);
    }

    @Override
    public Event findTime(Event event) {
        // TODO: THIS IS MOCKED!! Write algorithm to find dates.
        List<Event> existingEvents = eventRepository.findAll();
        for (Event existingEvent : existingEvents) {
            if (existingEvent.getId() == event.getId()) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
                try {
                    event.setStartDate(format.parse("2015/11/05/10/00/00"));
                    event.setEndDate(format.parse("2015/11/05/11/00/00"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return eventRepository.save(event);
            }
        }
        throw new RuntimeException(String.format(
                "Error: Event %d could not be found!", event.getId()));
        
    }

}
