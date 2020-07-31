package ar.gob.recibosdesueldos.consumer.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ar.gob.recibosdesueldos.commons.dto.generic.RestErrorResponse;
import ar.gob.recibosdesueldos.commons.dto.generic.RestResponse;
import ar.gob.recibosdesueldos.commons.exception.CustomException;
import ar.gob.recibosdesueldos.commons.exception.CustomServiceException;
import ar.gob.recibosdesueldos.commons.model.Plantilla;
import ar.gob.recibosdesueldos.commons.service.GrupoService;
import ar.gob.recibosdesueldos.consumer.services.TemplateService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.itextpdf.text.DocumentException;

import ar.gob.recibosdesueldos.consumer.pdf.GeneratePDF;
import ar.gob.recibosdesueldos.consumer.services.GeneratePDFService;
import ar.gob.recibosdesueldos.model.messaging.Recibo;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.HEAD})

public class GeneratePDFController {
    
	@Autowired
    private GeneratePDFService generatePDFService;

	@Autowired
    @Qualifier("generatePDF")
    private GeneratePDF generatePDF;

    @Autowired
    @Qualifier("templateService")
    private TemplateService templateService;

	@Value("${app.out_dir}")
    private String pathPdf;

    @Value("${app.preview_dir}")
    private String previewDir;

    @Value("${resources.templates}")
    private String templatesDir;

    @Value("${resources.img}")
    private String imgDir;

    @Value("${resources.css}")
    private String cssDir;

    @Value("${app.out_dir_temp}")
    private String tempDir;


    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<byte[]> generate(@RequestBody Recibo recibo) throws IOException, DocumentException {
    	this.generatePDF.createPDF(generatePDFService.generate(recibo), pathPdf, pathPdf+"\\");

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/generateTemplate")
    public RestResponse<Plantilla> generateTemplate(@RequestParam("codigoGrupo") String codigoGrupo,
                                              @RequestParam("descripcionPlantilla") String descripcionPlantilla,
                                              @RequestParam("template") MultipartFile template,
                                              @RequestParam("header") MultipartFile header,
                                              @RequestParam("signature") MultipartFile signature,
                                              @RequestParam("watermark") MultipartFile watermark) throws IOException, DocumentException, CustomException {
        Plantilla plantilla =templateService.generateTemplate(
                codigoGrupo.toUpperCase(),
                descripcionPlantilla,
                template,
                header,
                signature,
                watermark
		);
        return new RestResponse<>(HttpStatus.OK,plantilla);

    }

    @PostMapping(value = "/previsualizarPDF" )
    //@ResponseStatus(value = HttpStatus.OK)
  //  @PreAuthorize("hasRole('ROLE_ADMIN')")

    public ResponseEntity<?> generate2(
                                            @RequestParam("codigoGrupo") String codigoGrupo,
                                            @RequestParam("template") MultipartFile template,
                                            @RequestParam("header") MultipartFile header,
                                            @RequestParam("signature") MultipartFile signature,
                                            @RequestParam("watermark") MultipartFile watermark) throws IOException, DocumentException, CustomException {




        try {
            templateService.uploadTempFilesTemplate(codigoGrupo,template,header,signature,watermark);
        } catch (IOException e) {
            e.printStackTrace();
        }
    	this.generatePDF.previsualizarPdf(codigoGrupo, tempDir, previewDir, imgDir+"tmp\\", cssDir);

        String filePath = previewDir+"/x-xx-xx_0null_0.pdf";

        byte[] bFile = Files.readAllBytes(Paths.get(filePath));
        templateService.borraTempTemplatesFiles(codigoGrupo);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + codigoGrupo+".pdf");

        return (ResponseEntity<byte[]>) ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(bFile);
    }

}
