package ar.gob.recibosdesueldos.consumer.listener;

import ar.gob.recibosdesueldos.commons.exception.CustomException;
import ar.gob.recibosdesueldos.commons.service.ReciboSialService;
import ar.gob.recibosdesueldos.commons.service.RecibosService;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Queue;
import java.io.FileInputStream;
import java.nio.file.Path;
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

    @Autowired
    @Qualifier("recibosServiceCommons")
    private RecibosService recibosService;


    @Autowired
    @Qualifier("reciboSialService")
    private ReciboSialService reciboSialService;

    @Autowired
    private Queue queue;
    @Autowired
    private JmsTemplate jmsTemplate;

    private ReciboMapper mapper = Mappers.getMapper(ReciboMapper.class);

    @JmsListener(destination = "${app.jms_queue}", concurrency = "${app.jms_concurrency}")
    public void consume(Recibo recibo) {
        ar.gob.recibosdesueldos.model.recibos.Recibo newRecibo = mapper.sourceToDestination(recibo);
        MDC.put("idLote", newRecibo.getIdLote());
        MDC.put("idRecibo", newRecibo.getIdRecibo().toString());

        logger.debug(">> Generando recibo {}", newRecibo);
        logger.info(">> Generando recibo {}", newRecibo.getIdRecibo());


        FileWriter file = null;
        try {
            long start = System.nanoTime();
            Path patchfinal= this.generatePDF.createPDF(generatePDFService.generate(recibo),outDirTemp,outDir);
            long end = System.nanoTime();

            logger.info("Recibo con ID: {} demoro {}", recibo.getIdRecibo(), TimeUnit.MILLISECONDS.convert((end - start), TimeUnit.NANOSECONDS));

            String archivoASubir=patchfinal.getFileName().toString().replaceFirst("-", "/").replaceFirst("-", "/");
            try {
                if (!recibosService.fileExist(archivoASubir)) {
                    logger.info(">> subiendo a S3: {}", archivoASubir);
                    recibosService.uploadInputStream(archivoASubir, new FileInputStream(patchfinal.toFile()));
                    reciboSialService.setEstadoSubidoS3ByIdReciboSial(recibo.getIdLote(),recibo.getIdRecibo(),true);
                    logger.info(">>> subido a S3: {}", archivoASubir);
                }
            }catch (Exception eS3){
                logger.error(">> Error al subir S3: {}", archivoASubir);
                try {
                    reciboSialService.setEstadoSubidoS3ByIdReciboSial(recibo.getIdLote(),recibo.getIdRecibo(),false);
                    this.jmsTemplate.convertAndSend(queue, recibo);
                    logger.info(">> Reenviando a la cola recibo: {}", archivoASubir);
                } catch (Exception e) {
                    logger.error("Error al enviar a la cola nuevamente {}");
                    throw e;
                }finally {
                    patchfinal.toFile().delete();
                }
                throw new CustomException("Error Subiendo al S3");
            }finally {
                patchfinal.toFile().delete();
            }

            //Persiste registro en la db
            this.consumerService.createRecibos(newRecibo);
            //    logger.info("Actualizando el estado del recibo a: 'GENERADO' ");
            this.consumerService.updateEstadoRecibo(newRecibo.getIdReciboSial(), newRecibo.getIdLote());

        //    logger.info("Actualizando el estado del reciboSial a: 'OK'");
            this.consumerService.updateEstadoReciboSial(newRecibo.getIdReciboSial(), newRecibo.getIdLote(), "OK");
            MDC.remove("idLote");

        } catch (CustomException e) {
            logger.error(">> Error al subir S3");
        }catch (Exception e){
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
