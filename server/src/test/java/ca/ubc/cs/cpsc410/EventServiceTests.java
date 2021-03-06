package ca.ubc.cs.cpsc410;

import ca.ubc.cs.cpsc410.data.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WesyncApplication.class)
@WebAppConfiguration
public class EventServiceTests {

    @Autowired
    private UserRepository userRepository;
    private EventService service;
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Before
    public void setUp() throws Exception {
        service = new EventServiceImpl(eventRepository, userRepository, javaMailSender);
    }

    @Test
    public void createEventWithNoEventName() {
        try {
            service.createEvent(createEventParams(null, "dud", new Date(20500815), 1, true, new HashSet<String>(), new HashSet<String>()));
        } catch (RuntimeException re) {
            assertTrue(re.getMessage().contains("Error creating event: There is no event name specified!"));
        }
    }

    @Test
    public void createEventWithNoStartDate() {
        try {
            service.createEvent(createEventParams("dudName", "dud", null, 1, true, new HashSet<String>(), new HashSet<String>()));
        } catch (RuntimeException re) {
            assertTrue(re.getMessage().contains("does not have a start date!"));
        }
    }

    @Test
    public void createEventWithNoDuration() {
        try {
            service.createEvent(createEventParams("doodoo", "dud", new Date(20500816), 0, true, new HashSet<String>(), new HashSet<String>()));
        } catch (RuntimeException re) {
            assertTrue(re.getMessage().contains("does not have a duration!"));
        }
    }

    @Test
    public void createEventWithNonExistingHost() {
        try {
            service.createEvent(createEventParams("yuh", "NoHostCalledThis", new Date(20500817), 1, true, new HashSet<String>(), new HashSet<String>()));
        } catch (RuntimeException re) {
            assertTrue(re.getMessage().contains("does not match an existing user!"));
        }
    }

    @Test
    public void getInviteesOfNonExistantEvent() {
        try {
            Event nonExistingEvent = createEventParams("Doesn't Exist", "NoHostCalledThis", new Date(20500817), 1, true, new HashSet<String>(), new HashSet<String>());
            nonExistingEvent.setId(999999999);
            service.getInvitees(nonExistingEvent);
        } catch (RuntimeException re) {
            assertTrue(re.getMessage().contains("Error: Event id 999999999 cannot be found!"));
        }
    }

    @Test
    public void getConfirmedInviteesOfNonExistantEvent() {
        try {
            Event nonExistingEvent = createEventParams("Doesn't Exist", "NoHostCalledThis", new Date(20500817), 1, true, new HashSet<String>(), new HashSet<String>());
            nonExistingEvent.setId(999999999);
            service.getConfirmedInvitees(nonExistingEvent);
        } catch (RuntimeException re) {
            assertTrue(re.getMessage().contains("Error: Event id 999999999 cannot be found!"));
        }
    }

    private Event createEventParams(String name, String host, Date startDate, int duration, boolean isFinalized, Set<String> invitees, Set<String> confirmedInvitees) {
        Event mockEventParams = new Event();
        mockEventParams.setName(name);
        mockEventParams.setHost(host);
        mockEventParams.setStartDate(startDate);
        mockEventParams.setDuration(duration);
        mockEventParams.setIsFinalized(isFinalized);
        mockEventParams.setInvitees(invitees);
        mockEventParams.setConfirmedInvitees(confirmedInvitees);
        return mockEventParams;
    }
}
