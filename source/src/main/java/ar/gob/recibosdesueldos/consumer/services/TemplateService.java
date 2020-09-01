package ar.gob.recibosdesueldos.consumer.services;

import ar.gob.recibosdesueldos.commons.exception.CustomException;
import ar.gob.recibosdesueldos.commons.exception.CustomServiceException;
import ar.gob.recibosdesueldos.commons.model.Plantilla;
import ar.gob.recibosdesueldos.commons.service.GrupoService;
import ar.gob.recibosdesueldos.commons.service.LoteService;
import ar.gob.recibosdesueldos.commons.service.PlantillaService;
import ar.gob.recibosdesueldos.consumer.dao.ConsumerDao;
import ar.gob.recibosdesueldos.consumer.pdf.GeneratePDF;
import ar.gob.recibosdesueldos.model.recibos.Recibo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    @Autowired
    @Qualifier("loteService")
    private LoteService loteService;

    @Value("${app.out_dir_temp}")
    private String tempDir;

    public Plantilla generateTemplate(String grupo,
                                 String descripcionPlantilla,
                                 MultipartFile template,
                                 MultipartFile header,
                                 MultipartFile signature,
                                 MultipartFile watermark) throws IOException, CustomException {

        if(loteService.existLoteGenerandoPdf())
            throw new CustomServiceException("No se puede crear un template mientras exista una ejecución en proceso.",HttpStatus.CONFLICT);

        Plantilla plantilla = create(grupo, descripcionPlantilla);

        uploadFilesTemplate(grupo, pathImg, pathTemplates, "", plantilla.getId(), template, header, signature, watermark);
        return plantilla;
    }
    public Plantilla activarTemplate(Long idPlatinlla) throws CustomServiceException {
        if(loteService.existLoteGenerandoPdf())
            throw new CustomServiceException("No se puede activar un template mientras exista una ejecución en proceso.",HttpStatus.CONFLICT);
        Plantilla plantilla =activarPlantilla(idPlatinlla);
        String codGrupo=plantilla.getGrupos().get(0).getGrupo().getCodGrupo();
        // antes de copiar borro las images que exitan
        ar.gob.recibosdesueldos.commons.utils.FileUtils fileUtils= new ar.gob.recibosdesueldos.commons.utils.FileUtils();
        fileUtils.deleteDirectoryOnlyFiles(new File(pathImg+codGrupo+"/"));
        return copiarTemplatByIdPlantillaAndCodGrupo(idPlatinlla,pathImg,"");
    }
    public Plantilla copiarTemplatByIdPlantillaAndCodGrupo(Long idPlatinlla,String destino,String prefijo) throws CustomServiceException {
        try {
            Plantilla plantilla =getById(idPlatinlla);
            if(plantilla==null)
                throw new CustomServiceException("No existe la plantila con id:"+idPlatinlla,HttpStatus.BAD_REQUEST);

            String codGrupo=plantilla.getGrupos().get(0).getGrupo().getCodGrupo();
            //copio todas las images
            File source = new File(pathImg+codGrupo+"/"+idPlatinlla+"/");
            File dest = new File(destino+codGrupo+"/");
            FileUtils.copyDirectory(source, dest);
            //copio el archivo del template html
            File templateHtml = new File(pathTemplates+idPlatinlla+"_recibo_"+ codGrupo + ".html");
            final Path rootTemplate = Paths.get(pathTemplates);
            Files.copy(templateHtml.toPath(), rootTemplate.resolve(prefijo+"recibo_" + codGrupo + ".html"), REPLACE_EXISTING);
            return plantilla;
        } catch (IOException e) {
            throw new CustomServiceException("Error al copiar los archivos del template en : copiarTemplatByIdPlantillaAndCodGrupo", e.getCause());
        }
    }
    public byte[] getTemplateImagesZipByIdPlantilla(Long idPlatinlla) throws CustomServiceException {
        Path rootTemplate=null;
        Plantilla plantilla = getById(idPlatinlla);
        if (plantilla == null)
                throw new CustomServiceException("No existe la plantila con id:" + idPlatinlla, HttpStatus.BAD_REQUEST);
        try {    String codGrupo = plantilla.getGrupos().get(0).getGrupo().getCodGrupo();
            rootTemplate = Paths.get(pathImg + codGrupo + "/" + idPlatinlla + "/");
        } catch (Exception e) {
            throw new CustomServiceException("Error interno en getTemplateImagesZipByIdPlantilla.", e.getCause());
        }
        try {
            return ar.gob.recibosdesueldos.commons.utils.FileUtils.comprimirEnMemoria(rootTemplate);
        } catch (Exception e) {
            throw new CustomServiceException("Error al comprimir los archivos en getTemplateImagesZipByIdPlantilla.", e.getCause());
        }
    }
    public byte[] getTemplateZipByIdPlantilla(Long idPlatinlla) throws CustomServiceException {
        Path rootTemplate=null;
        Plantilla plantilla = getById(idPlatinlla);
        if (plantilla == null)
            throw new CustomServiceException("No existe la plantila con id:" + idPlatinlla, HttpStatus.BAD_REQUEST);
        try {    String codGrupo = plantilla.getGrupos().get(0).getGrupo().getCodGrupo();
            //busco archivos
            rootTemplate = Paths.get(pathImg + codGrupo + "/" + idPlatinlla + "/");
            List<File> archivos=ar.gob.recibosdesueldos.commons.utils.FileUtils.getListFilesFromDirectory(rootTemplate);
            archivos.add(new File(pathTemplates+idPlatinlla+"_recibo_"+ codGrupo + ".html"));
            archivos.add(new File(pathCss));

            //Los comprimo
            Path zipFile=ar.gob.recibosdesueldos.commons.utils.FileUtils.zipArchivos(archivos);

            //borro temporales
            final InputStream targetStream = new FileInputStream(zipFile.toFile());
            byte[] bytes = IOUtils.toByteArray(targetStream);
            targetStream.close();
            FileSystemUtils.deleteRecursively(zipFile);

            return bytes;

        } catch (Exception e) {
            throw new CustomServiceException("Error interno en getTemplateImagesZipByIdPlantilla.", e.getCause());
        }
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
        if (watermark!=null && !watermark.getContentType().equals("image/gif")) {
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
        if (watermark != null && !watermark.isEmpty()) {
            Files.copy(watermark.getInputStream(), rootImg.resolve(grupo + "/" + "marca_agua_" + grupo.toUpperCase() + "." + FilenameUtils.getExtension(watermark.getOriginalFilename())), REPLACE_EXISTING);
        }else{ // si no mandan la marca de agua, se borra la que existia
            File watermarkExistente = new File(pathImg + grupo.toUpperCase());
            if (watermarkExistente.exists()) {
                File[] files = watermarkExistente.listFiles();
                for (File file:files) {
                    if(!file.isDirectory() && file.getName().startsWith("marca_agua_" + grupo.toUpperCase() + "."))
                        file.delete();
                }
            }
        }
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
                File watermarkExistente = new File(pathImg + grupo.toUpperCase()+"/" + idPlantilla);
                if (watermarkExistente.exists()) {
                    File[] files = watermarkExistente.listFiles();
                    for (File file:files) {
                        if(!file.isDirectory() && file.getName().startsWith("marca_agua_" + grupo.toUpperCase() + "."))
                            file.delete();
                    }
                }
        }
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
