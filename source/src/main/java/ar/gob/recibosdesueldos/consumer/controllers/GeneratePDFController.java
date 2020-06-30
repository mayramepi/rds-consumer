package ar.gob.recibosdesueldos.consumer.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.text.DocumentException;

import ar.gob.recibosdesueldos.consumer.pdf.GeneratePDF;
import ar.gob.recibosdesueldos.consumer.services.GeneratePDFService;
import ar.gob.recibosdesueldos.model.messaging.Recibo;

@RestController
@RequestMapping("/api")
public class GeneratePDFController {
    
	@Autowired
    private GeneratePDFService generatePDFService;
    
	@Autowired
    private GeneratePDF generatePDF;

	@Value("${app.out_dir}")
    private String pathPdf;
	
    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<byte[]> generate(@RequestBody Recibo recibo) throws IOException, DocumentException {
    	this.generatePDF.createPDF(generatePDFService.generate(recibo), pathPdf, pathPdf);

        return new ResponseEntity<>(HttpStatus.OK);	
    }
    
    @PostMapping(value = "/generateTemplate/{codGrupo}")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> generateTemplate(@PathVariable String codGrupo) throws Exception {
		this.generatePDF.generateTemplate(
				codGrupo.toUpperCase(),
				"template.html",
				"header.gif",
				"signature.jpg",
				"watermark.gif"
		);
		
		return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/previsualizarPDF/{codigoGrupo}", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<byte[]> generate2(@PathVariable String codigoGrupo) throws IOException, DocumentException {
    	
    	String dirFinal="c:\\resources2\\dirFinal";
    	String pathImg1="c:\\resources2\\img\\";
    	String dirTemp="c:\\resources2\\pdf";
    	String pathCss1="c:\\resources2\\css\\style.css";
    	
    	this.generatePDF.previsualizarPdf(codigoGrupo, dirTemp, dirFinal, pathImg1, pathCss1);
        
        return new ResponseEntity<>(HttpStatus.OK);	
    }
    
}
