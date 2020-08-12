package ar.gob.recibosdesueldos.consumer.scheduler;

import ar.gob.recibosdesueldos.consumer.pdf.GeneratePDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Tarea programada para ejecutar integración evaluación de desempeño con Meta4
 * @author Armando Guzman
 *
 */
@Component
public class ScheduledTasks {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
    @Autowired
    @Qualifier("generatePDF")
    private GeneratePDF generatePDF;

    @Scheduled(cron = "${chequeoBorradoTemplates:0 0/15 * * * ?}")
    private void chequeoBorradoTemplates() {
        LOGGER.info("Iniciando ScheduledTasks: chequeoBorradoTemplates a las:"+new Date().toString());
        generatePDF.borrarCacheTemplates();
    }
}

