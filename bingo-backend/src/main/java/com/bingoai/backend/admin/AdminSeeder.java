package com.bingoai.backend.admin;

import com.bingoai.backend.event.Event;
import com.bingoai.backend.event.EventRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AdminSeeder implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(AdminRepository adminRepository,
                       EventRepository eventRepository,
                       PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // 1. Seed SuperAdmin if not exists
        Admin superAdmin = adminRepository.findByUsername("admin").orElseGet(() -> {
            Admin newAdmin = new Admin();
            newAdmin.setUsername("admin");
            newAdmin.setPassword(passwordEncoder.encode("admin"));
            newAdmin.setEmail("admin@bingoai.com");
            newAdmin.setRole("ROLE_SUPER_ADMIN");
            return adminRepository.save(newAdmin);
        });

        // 2. Associate existing events that do not have an owner with the SuperAdmin
        List<Event> orphanedEvents = eventRepository.findAll();
        boolean updatedAny = false;
        for (Event event : orphanedEvents) {
            // Check via reflection or since we are adding the field in Step 7, we'll implement ownership.
            // Wait, we will modify Event.java in Step 7. Since Java compilation will fail if we compile right now
            // before updating Event.java, we should write all backend files first and then compile/test.
            // Let's call event.getAdmin() which we will add in Event.java.
            if (getEventAdmin(event) == null) {
                setEventAdmin(event, superAdmin);
                eventRepository.save(event);
                updatedAny = true;
            }
        }
        if (updatedAny) {
            System.out.println("[AdminSeeder] Associated existing orphan events with default SuperAdmin.");
        }
    }

    // Helper methods to access admin property of Event (which will be added in Event.java)
    private Admin getEventAdmin(Event event) {
        try {
            // We use reflection or direct call. Direct call is cleaner since we compile them together.
            // We'll write direct call because compilation checks are run on the whole project.
            return event.getAdmin();
        } catch (Throwable t) {
            return null;
        }
    }

    private void setEventAdmin(Event event, Admin admin) {
        event.setAdmin(admin);
    }
}
