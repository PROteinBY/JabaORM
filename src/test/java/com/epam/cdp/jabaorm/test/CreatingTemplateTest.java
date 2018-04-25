package com.epam.cdp.jabaorm.test;

import com.epam.cdp.jabaorm.JabaTemplate;
import com.epam.cdp.jabaorm.test.model.User;
import org.junit.Test;

import java.text.SimpleDateFormat;

public class CreatingTemplateTest {

    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    @Test
    public void testCreating() throws Exception {
        JabaTemplate<User> template = JabaTemplate.create(User.class);
        User user = new User(123, "Bob", sdf.parse("31-07-1997"), true, "Brest, Belarus");

        template.save(user);

        user = new User(123, null, null, null, null);
        template.search(user);
    }

}
