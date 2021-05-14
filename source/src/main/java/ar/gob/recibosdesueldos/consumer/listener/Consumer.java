package ar.gob.recibosdesueldos.consumer.listener;

import ar.gob.recibosdesueldos.consumer.pdf.GeneratePDF;
import ar.gob.recibosdesueldos.consumer.services.ConsumerService;
import ar.gob.recibosdesueldos.consumer.services.GeneratePDFService;
import ar.gob.recibosdesueldos.model.mappers.ReciboMapper;
import ar.gob.recibosdesueldos.model.messaging.Recibo;
import com.itextpdf.text.DocumentException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class Consumer {
    private static Logger logger = LoggerFactory.getLogger(Consumer.class);

    @Value("${app.out_dir}")
    private String outDir;
    @Value("${app.out_dir_temp}")
    private String outDirTemp;
	@Autowired
    private GeneratePDFService generatePDFService;
	@Autowired
    private GeneratePDF generatePDF;
    @Autowired
    private ConsumerService consumerService;

    private ReciboMapper mapper = Mappers.getMapper(ReciboMapper.class);

    @JmsListener(destination = "${app.jms_queue}", concurrency = "${app.jms_concurrency}")
    public void consume(Recibo recibo) {
        ar.gob.recibosdesueldos.model.recibos.Recibo newRecibo = mapper.sourceToDestination(recibo);
        MDC.put("idLote", newRecibo.getIdLote());
        MDC.put("idRecibo", newRecibo.getIdRecibo().toString());

        logger.debug(">> Generando recibo {}", newRecibo);
        logger.info(">> Generando recibo {}", newRecibo.getIdRecibo());

        this.consumerService.createRecibos(newRecibo);

        FileWriter file = null;
        try {
            long start = System.nanoTime();
            this.generatePDF.createPDF(generatePDFService.generate(recibo),outDirTemp,outDir);
            long end = System.nanoTime();

            logger.info("Recibo con ID: {} demoro {}", recibo.getIdRecibo(), TimeUnit.MILLISECONDS.convert((end - start), TimeUnit.NANOSECONDS));

        //    logger.info("Actualizando el estado del recibo a: 'GENERADO' ");
            this.consumerService.updateEstadoRecibo(newRecibo.getIdReciboSial(), newRecibo.getIdLote());

        //    logger.info("Actualizando el estado del reciboSial a: 'OK'");
            this.consumerService.updateEstadoReciboSial(newRecibo.getIdReciboSial(), newRecibo.getIdLote(), "OK");
            MDC.remove("idLote");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(">> recibo con error: {}", newRecibo);
            String stacktrace = ExceptionUtils.getStackTrace(e);
            logger.error(">> Error: {}",stacktrace);
           // logger.info("Actualizando el estado del reciboSial a: 'NOK'");
            this.consumerService.updateEstadoReciboSial(newRecibo.getIdReciboSial(), newRecibo.getIdLote(), "NOK");
        } finally {
            MDC.remove("idRecibo");
            MDC.remove("idLote");
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
