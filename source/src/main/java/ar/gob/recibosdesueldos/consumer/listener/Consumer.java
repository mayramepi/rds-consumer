package ar.gob.recibosdesueldos.consumer.listener;

import ar.gob.recibosdesueldos.consumer.pdf.GeneratePDF;
import ar.gob.recibosdesueldos.consumer.services.ConsumerService;
import ar.gob.recibosdesueldos.consumer.services.GeneratePDFService;
import ar.gob.recibosdesueldos.model.mappers.ReciboMapper;
import ar.gob.recibosdesueldos.model.messaging.Recibo;
import com.itextpdf.text.DocumentException;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @JmsListener(destination = "lotes.queue", concurrency = "${app.jms_concurrency}")
    public void consume(Recibo recibo) {
        ar.gob.recibosdesueldos.model.recibos.Recibo newRecibo = mapper.sourceToDestination(recibo);

        logger.debug(">> Insertando recibo {}", newRecibo);

        this.consumerService.createRecibos(newRecibo);

        FileWriter file = null;
        try {
            long start = System.nanoTime();
            this.generatePDF.createPDF(generatePDFService.generate(recibo),outDirTemp,outDir);
            long end = System.nanoTime();

            logger.info("Recibo con ID: {} demoro {}", recibo.getIdRecibo(), TimeUnit.MILLISECONDS.convert((end - start), TimeUnit.NANOSECONDS));

            this.consumerService.updateEstadoRecibo(newRecibo.getIdReciboSial(), newRecibo.getIdLote());

            this.consumerService.updateEstadoReciboSial(newRecibo.getIdReciboSial(), newRecibo.getIdLote(), "OK");
        } catch (Exception e) {
            e.printStackTrace();
            this.consumerService.updateEstadoReciboSial(newRecibo.getIdReciboSial(), newRecibo.getIdLote(), "NOK");
		} finally {
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
