package com.mypill.domain.email.service;

import com.mypill.domain.email.entity.SendEmailLog;
import com.mypill.domain.email.repository.SendEmailLogRepository;
import com.mypill.domain.emailSender.service.EmailSenderService;
import com.mypill.global.AppConfig;
import com.mypill.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailService {
    private final SendEmailLogRepository emailLogRepository;
    private final EmailSenderService emailSenderService;
    @Value("${custom.mail.username}")
    private String adminEmail;

    @Transactional
    public RsData<Long> sendEmail(String email, String subject, String body) {
        SendEmailLog sendEmailLog = SendEmailLog
                .builder()
                .email(email)
                .subject(subject)
                .body(body)
                .build();

        emailLogRepository.save(sendEmailLog);

        RsData trySendRs = trySend(email, subject, body);

        setCompleted(sendEmailLog, trySendRs.getResultCode(), trySendRs.getMsg());

        return RsData.of("S-1", "메일이 발송되었습니다.", sendEmailLog.getId());
    }

    private RsData trySend(String email, String title, String body) {
        if (AppConfig.isNotProd() && email.equals(adminEmail) == false) {
            return RsData.of("S-0", "메일이 발송되었습니다.");
        }

        try {
            emailSenderService.send(email, "no-reply@no-reply.com", title, body);

            return RsData.of("S-1", "메일이 발송되었습니다.");
        } catch (MailException e) {
            return RsData.of("F-1", e.getMessage());
        }
    }

    @Transactional
    public void setCompleted(SendEmailLog sendEmailLog, String resultCode, String message) {
        if (resultCode.startsWith("S-")) {
            sendEmailLog.setSendEndDate(LocalDateTime.now());
        } else {
            sendEmailLog.setFailDate(LocalDateTime.now());
        }

        sendEmailLog.setResultCode(resultCode);
        sendEmailLog.setMessage(message);

        emailLogRepository.save(sendEmailLog);
    }
}