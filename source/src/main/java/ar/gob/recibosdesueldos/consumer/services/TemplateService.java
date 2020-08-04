package ar.gob.recibosdesueldos.consumer.services;

import ar.gob.recibosdesueldos.commons.exception.CustomException;
import ar.gob.recibosdesueldos.commons.exception.CustomServiceException;
import ar.gob.recibosdesueldos.commons.model.Plantilla;
import ar.gob.recibosdesueldos.commons.service.GrupoService;
import ar.gob.recibosdesueldos.commons.service.PlantillaService;
import ar.gob.recibosdesueldos.consumer.dao.ConsumerDao;
import ar.gob.recibosdesueldos.consumer.pdf.GeneratePDF;
import ar.gob.recibosdesueldos.model.recibos.Recibo;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service("templateService")
@Transactional(propagation = Propagation.REQUIRED)
public class TemplateService extends PlantillaService {
    @Value("${app.preview_dir}")
    private String pathPreview;

    @Value("${resources.css}")
    private String pathCss;

    @Value("${resources.img}")
    private String pathImg;

    @Value("${resources.templates}")
    private String pathTemplates;
    @Value("${app.preview_dir}")
    private String previewDir;

    @Autowired
    @Qualifier("grupoService")
    private GrupoService grupoService;
//    @Autowired
//    @Qualifier("plantillaService")
//    private PlantillaService plantillaService;

    @Autowired
    @Qualifier("generatePDF")
    private GeneratePDF generatePDF;

    @Value("${app.out_dir_temp}")
    private String tempDir;

    public Plantilla generateTemplate(String grupo,
                                 String descripcionPlantilla,
                                 MultipartFile template,
                                 MultipartFile header,
                                 MultipartFile signature,
                                 MultipartFile watermark) throws IOException, CustomException {
        Plantilla plantilla = create(grupo, descripcionPlantilla);
        //	uploadFilesTemplate( grupo,pathImg,pathTemplates,plantilla.getId().toString()+"_",template,header,signature,watermark);
        uploadFilesTemplate(grupo, pathImg, pathTemplates, "", plantilla.getId(), template, header, signature, watermark);
        return plantilla;
    }
    public void activarTemplate(Long idPlatinlla) throws CustomServiceException {
        activarPlantilla(idPlatinlla);
        //todo
    }
    public void uploadTempFilesTemplate(String grupo,
                                        MultipartFile template,
                                        MultipartFile header,
                                        MultipartFile signature,
                                        MultipartFile watermark) throws IOException, CustomException {

        File imDir = new File(pathImg + "tmp");
        if (!imDir.exists()) {
            imDir.mkdirs();
        }
        uploadFilesTemplate(grupo, pathImg + "tmp/", pathTemplates, "_", null, template, header, signature, watermark);
    }

    public void uploadFilesTemplate(String grupo,
                                    String pathImg,
                                    String pathTemplates,
                                    String prefijoTemplate,
                                    Long idPlantilla,
                                    MultipartFile template,
                                    MultipartFile header,
                                    MultipartFile signature,
                                    MultipartFile watermark) throws IOException, CustomException {
        if (!grupoService.existeByCodGrupo(grupo)) {
            throw new CustomException("No exite el grupo:'" + grupo + "'", HttpStatus.BAD_REQUEST);
        }

        if (!template.getContentType().equals("text/html")) {
            throw new CustomException("El archivo de template " + template.getOriginalFilename() + " no es del tipo text/html'", HttpStatus.BAD_REQUEST);
        }
        if (!header.getContentType().equals("image/gif") && !signature.getContentType().equals("image/jpeg")) {
            throw new CustomException("El archivo del encabezado " + header.getOriginalFilename() + " no es del tipo image/gif o image/jpeg'", HttpStatus.BAD_REQUEST);
        }
        if (!signature.getContentType().equals("image/gif") && !signature.getContentType().equals("image/jpeg")) {
            throw new CustomException("El archivo de la firma" + signature.getOriginalFilename() + " no es del tipo image/gif o image/jpeg'", HttpStatus.BAD_REQUEST);
        }
        if (!watermark.getContentType().equals("image/gif")) {
            throw new CustomException("El el archivo de la marca de agua " + watermark.getOriginalFilename() + " no es del tipo image/gif '", HttpStatus.BAD_REQUEST);
        }

        File imDir = new File(pathImg + grupo);
        if (!imDir.exists()) {
            imDir.mkdirs();
        }

        final Path rootTemplate = Paths.get(pathTemplates);
        final Path rootImg = Paths.get(pathImg);
        if (template != null && !template.isEmpty())
            Files.copy(template.getInputStream(), rootTemplate.resolve(prefijoTemplate + "recibo_" + grupo.toUpperCase() + ".html"), REPLACE_EXISTING);
        if (header != null && !header.isEmpty())
            Files.copy(header.getInputStream(), rootImg.resolve(grupo + "/" + header.getOriginalFilename()), REPLACE_EXISTING);
        if (signature != null && !signature.isEmpty())
            Files.copy(signature.getInputStream(), rootImg.resolve(grupo + "/" + signature.getOriginalFilename()), REPLACE_EXISTING);
        if (watermark != null && !watermark.isEmpty())
            Files.copy(watermark.getInputStream(), rootImg.resolve(grupo + "/" + "marca_agua_" + grupo.toUpperCase() + "." + FilenameUtils.getExtension(watermark.getOriginalFilename())), REPLACE_EXISTING);
        if (idPlantilla != null) {
            imDir = new File(pathImg + grupo + "/" + idPlantilla);
            if (!imDir.exists()) {
                imDir.mkdirs();
            }
            if (template != null && !template.isEmpty())
                Files.copy(template.getInputStream(), rootTemplate.resolve(idPlantilla + "_recibo_" + grupo.toUpperCase() + ".html"), REPLACE_EXISTING);
            if (header != null && !header.isEmpty())
                Files.copy(header.getInputStream(), rootImg.resolve(grupo + "/" + idPlantilla + "/" + header.getOriginalFilename()), REPLACE_EXISTING);
            if (signature != null && !signature.isEmpty())
                Files.copy(signature.getInputStream(), rootImg.resolve(grupo + "/" + idPlantilla + "/" + signature.getOriginalFilename()), REPLACE_EXISTING);
            if (watermark != null && !watermark.isEmpty())
                Files.copy(watermark.getInputStream(), rootImg.resolve(grupo + "/" + idPlantilla + "/" + "marca_agua_" + grupo.toUpperCase() + "." + FilenameUtils.getExtension(watermark.getOriginalFilename())), REPLACE_EXISTING);

        }
    }

    public void borraImagesTemplateFiles(Long idTemplate) throws IOException {
        final Path TEMP_DIRECTORY = Paths.get(pathImg);

//		Path pathToBeDeleted = TEMP_DIRECTORY.resolve(idTemplate.);
//
//		Files.walk(pathToBeDeleted)
//				.sorted(Comparator.reverseOrder())
//				.map(Path::toFile)
//				.forEach(File::delete);
    }

    public void borraTempTemplatesFiles(String grupo) throws IOException {
        final Path TEMP_DIRECTORY = Paths.get(pathImg);

        Path pathToBeDeleted = TEMP_DIRECTORY.resolve("tmp");

        Files.walk(pathToBeDeleted)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        File templateTempFile = new File(pathTemplates + "/_recibo_" + grupo.toUpperCase() + ".html");
        if (templateTempFile.exists()) {
            templateTempFile.delete();
        }
        String filePath = previewDir + "/8-20266221488-20266221488_2020null_0.pdf";
        File prevPDF = new File(filePath);
        if (prevPDF.exists()) {
            prevPDF.delete();
        }
    }
}
