package com.classinsight;

import com.classinsight.service.AvaliacaoService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AvaliacaoServiceTest {

    @Test
    public void testUrgenciaMapping() {
        AvaliacaoRequest r1 = new AvaliacaoRequest("a", 1.0);
        AvaliacaoResponseDTO d1 = AvaliacaoService.process(r1);
        assertEquals(Urgencia.CRITICO, d1.getUrgencia());

        AvaliacaoRequest r2 = new AvaliacaoRequest("a", 3.0);
        AvaliacaoResponseDTO d2 = AvaliacaoService.process(r2);
        assertEquals(Urgencia.ALTA, d2.getUrgencia());

        AvaliacaoRequest r3 = new AvaliacaoRequest("a", 6.0);
        AvaliacaoResponseDTO d3 = AvaliacaoService.process(r3);
        assertEquals(Urgencia.MEDIA, d3.getUrgencia());

        AvaliacaoRequest r4 = new AvaliacaoRequest("a", 9.0);
        AvaliacaoResponseDTO d4 = AvaliacaoService.process(r4);
        assertEquals(Urgencia.BAIXA, d4.getUrgencia());
    }
}
