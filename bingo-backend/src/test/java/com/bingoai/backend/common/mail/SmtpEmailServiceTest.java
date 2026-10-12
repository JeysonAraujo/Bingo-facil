package com.bingoai.backend.common.mail;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

public class SmtpEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private SmtpEmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@bingoai.com");
        ReflectionTestUtils.setField(emailService, "mailHost", "smtp.test.com");
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
    }

    @Test
    void sendPasswordResetEmail_DispatchesMimeMessage() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("user@example.com", "123456"));

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendPasswordResetEmail_WhenMailDisabled_DoesNotSend() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("user@example.com", "123456"));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
