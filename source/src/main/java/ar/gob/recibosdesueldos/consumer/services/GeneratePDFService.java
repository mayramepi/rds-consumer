package ar.gob.recibosdesueldos.consumer.services;

import org.springframework.stereotype.Service;

import ar.gob.recibosdesueldos.model.messaging.Recibo;
import ar.gob.recibosdesueldos.consumer.pdf.PlantillaPDF;

@Service
public class GeneratePDFService {

    public PlantillaPDF generate(Recibo recibo) {
    	PlantillaPDF plantillaPDF = new PlantillaPDF();

        plantillaPDF.setPlantillaNombre("name");
        plantillaPDF.setHtmlEncabezado("header");
        plantillaPDF.setHtmlPieDePagina("footer");
        plantillaPDF.setRecibo(recibo);

        return plantillaPDF;
    }

}
