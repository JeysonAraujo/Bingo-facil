package com.bingoai.backend.common.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@bingoai.com}")
    private String fromEmail;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Override
    public void sendPasswordResetEmail(String toEmail, String code) {
        if (!mailEnabled || mailSender == null) {
            log.warn("[EMAIL SERVICE] E-mail sending is disabled or JavaMailSender is not configured. Reset email was NOT dispatched to {}", toEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Código de Recuperação de Senha - Bingo-AI");

            String htmlContent = buildResetPasswordTemplate(code);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("[EMAIL SERVICE] Password reset email successfully sent to {}", toEmail);
        } catch (MessagingException | MailException e) {
            log.error("[EMAIL SERVICE] Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível enviar o e-mail de recuperação. Verifique as configurações de e-mail do servidor."
            );
        }
    }

    private String buildResetPasswordTemplate(String code) {
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
              <meta charset="UTF-8">
              <title>Recuperação de Senha - Bingo-AI</title>
            </head>
            <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f9; margin: 0; padding: 24px;">
              <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 560px; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);">
                <tr>
                  <td style="background: linear-gradient(135deg, #1a237e 0%%, #311b92 100%%); padding: 32px 24px; text-align: center;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 26px; letter-spacing: 1px;">Bingo-AI</h1>
                    <p style="color: #c5cae9; margin: 6px 0 0 0; font-size: 14px;">Plataforma de Bingo em Tempo Real</p>
                  </td>
                </tr>
                <tr>
                  <td style="padding: 32px 24px;">
                    <h2 style="color: #1a237e; font-size: 20px; margin-top: 0;">Recuperação de Senha</h2>
                    <p style="color: #424242; font-size: 15px; line-height: 1.6;">
                      Recebemos uma solicitação para redefinir a senha da sua conta de administrador. Utilize o código de verificação abaixo:
                    </p>
                    <div style="text-align: center; margin: 28px 0;">
                      <div style="display: inline-block; background-color: #e8eaf6; border: 2px dashed #3949ab; border-radius: 8px; padding: 14px 28px;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #1a237e;">%s</span>
                      </div>
                    </div>
                    <p style="color: #616161; font-size: 14px; line-height: 1.5;">
                      ⏰ Este código expira em <strong>15 minutos</strong>.<br>
                      🔒 Por motivos de segurança, nunca compartilhe este código com ninguém.
                    </p>
                    <p style="color: #9e9e9e; font-size: 13px; line-height: 1.4; margin-top: 24px; border-top: 1px solid #eeeeee; padding-top: 16px;">
                      Se você não solicitou a redefinição de senha, desconsidere este e-mail. Sua conta permanece segura.
                    </p>
                  </td>
                </tr>
                <tr>
                  <td style="background-color: #fafafa; padding: 16px 24px; text-align: center; border-top: 1px solid #eeeeee;">
                    <p style="color: #9e9e9e; font-size: 12px; margin: 0;">
                      &copy; Bingo-AI Platform. Todos os direitos reservados.
                    </p>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(code);
    }
}
