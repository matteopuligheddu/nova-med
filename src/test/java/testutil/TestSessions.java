package testutil;

import it.nova.novamed.model.Role;
import org.springframework.mock.web.MockHttpSession;

public class TestSessions {

    public static MockHttpSession patient(long id) {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute("userId", id);
        s.setAttribute("role", Role.PATIENT);
        return s;
    }

    public static MockHttpSession doctor(long id) {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute("userId", id);
        s.setAttribute("role", Role.DOCTOR);
        return s;
    }

    public static MockHttpSession admin(long id) {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute("userId", id);
        s.setAttribute("role", Role.ADMIN);
        return s;
    }
}