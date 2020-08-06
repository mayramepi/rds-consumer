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
import ar.gob.recibosdesueldos.commons.dto.request.GetAllPlantillasDto;
import ar.gob.recibosdesueldos.commons.dto.response.PlantillaDto;
import ar.gob.recibosdesueldos.commons.exception.CustomException;
import ar.gob.recibosdesueldos.commons.exception.CustomServiceException;
import ar.gob.recibosdesueldos.commons.model.Plantilla;
import ar.gob.recibosdesueldos.commons.service.GrupoService;
import ar.gob.recibosdesueldos.consumer.services.TemplateService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
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
import springfox.documentation.annotations.ApiIgnore;

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

    private FileUtils fileUtils;

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

    @PostMapping(value = "/borrarCacheTemplates")
    public RestResponse<Boolean> borrarCacheTemplates(){
        generatePDF.borrarCacheTemplates();
        return new RestResponse<>(HttpStatus.OK,true);
    }
    @PostMapping(value = "/activarTemplate")
    public RestResponse<Plantilla> activarTemplate(@RequestParam("idPlantilla") long idPlantilla) throws CustomException {
        return new RestResponse<>(HttpStatus.OK,templateService.activarTemplate(idPlantilla));
    }

    @PostMapping(value = "/previsualizarPDF" )
    //@ResponseStatus(value = HttpStatus.OK)
  //  @PreAuthorize("hasRole('ROLE_ADMIN')")

    public ResponseEntity<?> previsualizarPDF(
                                            @RequestParam("codigoGrupo") String codigoGrupo,
                                            @RequestParam("maxDetalles") int maxDetalles,
                                            @RequestParam("template") MultipartFile template,
                                            @RequestParam("header") MultipartFile header,
                                            @RequestParam("signature") MultipartFile signature,
                                            @RequestParam("watermark") MultipartFile watermark) throws IOException, DocumentException, CustomException {




        try {
            templateService.uploadTempFilesTemplate(codigoGrupo,template,header,signature,watermark);
        } catch (IOException e) {
            e.printStackTrace();
        }
    	this.generatePDF.previsualizarPdf(codigoGrupo, tempDir, previewDir, imgDir+"tmp/", cssDir,maxDetalles);

        String filePath = previewDir+"/8-20266221488-20266221488_2020null_0.pdf";

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
    @PostMapping(value = "/previsualizarPDFByIdPlantilla" )
    //@ResponseStatus(value = HttpStatus.OK)
    //  @PreAuthorize("hasRole('ROLE_ADMIN')")

    public ResponseEntity<?> previsualizarPDFByIdPlantilla(
            @RequestParam("maxDetalles") int maxDetalles,
            @RequestParam("idPlantilla") long idPlantilla) throws IOException, DocumentException, CustomException {




        Plantilla plantilla=templateService.copiarTemplatByIdPlantillaAndCodGrupo(idPlantilla,imgDir+"tmp/","_");
        String codigoGrupo=plantilla.getGrupos().get(0).getGrupo().getCodGrupo();
        this.generatePDF.previsualizarPdf(codigoGrupo, tempDir, previewDir, imgDir+"tmp/", cssDir,maxDetalles);

        String filePath = previewDir+"/8-20266221488-20266221488_2020null_0.pdf";

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

    @PostMapping(value = "/dercargarImagenesByIdPlantilla" )
    public ResponseEntity<?> dercargarImagenesByIdPlantilla(
            @RequestParam("idPlantilla") long idPlantilla) throws  CustomServiceException {

        byte[] bFile=templateService.getTemplateImagesZipByIdPlantilla(idPlantilla);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=imagenesPlantilla"+idPlantilla+".zip");

        return (ResponseEntity<byte[]>) ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bFile);
    }
    @PostMapping(value = "/dercargarArchivosTemplateByIdPlantilla" )
    public ResponseEntity<?> dercargarArchivosTemplateByIdPlantilla(
            @RequestParam("idPlantilla") long idPlantilla) throws  CustomServiceException {

        byte[] bFile=templateService.getTemplateZipByIdPlantilla(idPlantilla);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=archivosPlantilla"+idPlantilla+".zip");

        return (ResponseEntity<byte[]>) ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bFile);
    }

//    @ApiOperation(value = "getAllPlanillas")
//    @GetMapping("/getAllPlanillas")
//  //  @PreAuthorize("hasPermission('','BUSCAR_USURIO_FILTRO')")
//    public ResponseEntity<RestResponse<Page<PlantillasDto>>> getAllPlanillas(Pageable pageable, GetAllPlantillasDto request,
//                                                                         @ApiIgnore PagedResourcesAssembler assembler) throws CustomException {
//
//        Page<PlantillasDto> result = templateService.getAllPlantillas(pageable,request);
//
//        PagedModel pagedResources = assembler.toModel(result);
//        return new ResponseEntity<>(new RestResponse<>(HttpStatus.OK, result), HttpStatus.OK);
//
//    }

    @ApiOperation(value = "getAllPlantillasByGrupoId")
    @GetMapping("/getAllPlantillasByGrupoId")
    //  @PreAuthorize("hasPermission('','BUSCAR_USURIO_FILTRO')")
    public ResponseEntity<RestResponse<List<PlantillaDto>>> getAllPlantillasByGrupoId(@RequestParam("idGrupo") long idGrupo) throws CustomException {

        List<PlantillaDto> result = templateService.getAllPlantillasByGrupoId(idGrupo);

    //    PagedModel pagedResources = assembler.toModel(result);
        return new ResponseEntity<>(new RestResponse<>(HttpStatus.OK, result), HttpStatus.OK);

    }
}
