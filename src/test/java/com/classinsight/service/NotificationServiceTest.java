package com.classinsight.service;

import com.classinsight.AvaliacaoResponseDTO;
import com.classinsight.Urgencia;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Test
    public void sendsEmailForCriticalAndHigh() {
        EmailSender mockSender = mock(EmailSender.class);
        when(mockSender.send(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
        NotificationService.setEmailSender(mockSender);

        AvaliacaoResponseDTO dtoCritico = new AvaliacaoResponseDTO("desc", Urgencia.CRITICO, "now");
        NotificationService.publishNotification(dtoCritico);
        verify(mockSender, atLeastOnce()).send(anyString(), anyString(), contains("CRÍTICO"), anyString());

        AvaliacaoResponseDTO dtoAlta = new AvaliacaoResponseDTO("desc", Urgencia.ALTA, "now");
        NotificationService.publishNotification(dtoAlta);
        verify(mockSender, atLeastOnce()).send(anyString(), anyString(), contains("ALTA"), anyString());
    }

    @Test
    public void doesNotSendEmailForLowOrMedium() {
        EmailSender mockSender = mock(EmailSender.class);
        NotificationService.setEmailSender(mockSender);

        AvaliacaoResponseDTO dtoMedia = new AvaliacaoResponseDTO("desc", Urgencia.MEDIA, "now");
        NotificationService.publishNotification(dtoMedia);
        AvaliacaoResponseDTO dtoBaixa = new AvaliacaoResponseDTO("desc", Urgencia.BAIXA, "now");
        NotificationService.publishNotification(dtoBaixa);

        verifyNoInteractions(mockSender);
    }
}
