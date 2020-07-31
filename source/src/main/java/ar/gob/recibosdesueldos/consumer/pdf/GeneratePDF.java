package ar.gob.recibosdesueldos.consumer.pdf;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import ar.gob.recibosdesueldos.commons.exception.CustomException;
import ar.gob.recibosdesueldos.commons.model.Plantilla;
import ar.gob.recibosdesueldos.commons.service.GrupoService;
import ar.gob.recibosdesueldos.commons.service.PlantillaService;
import ar.gob.recibosdesueldos.commons.utils.RdsUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;

import com.itextpdf.text.DocumentException;
import static java.nio.file.StandardCopyOption.*;

import ar.gob.recibosdesueldos.model.messaging.DetalleRecibo;
import ar.gob.recibosdesueldos.model.messaging.DetalleReciboForHtml;
import ar.gob.recibosdesueldos.model.messaging.Recibo;
import ar.gob.recibossueldos.consumer.constant.Constantes;

@Component
@Service("generatePDF")
public class GeneratePDF {

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

	@Value("${app.out_dir_temp}")
	private String tempDir;



	@Value("${app.marca.agua.gcba}")
	private boolean marcaGcba;
	@Value("${app.marca.agua.ivc}")
	private boolean marcaIvc;
    @Value("${app.marca.agua.pdc}")
	private boolean marcaPdc;
	@Value("${app.marca.agua.boberos}")
	private boolean marcaBoberos;
	@Value("${app.marca.agua.issp}")
	private boolean marcaIssp;
	@Autowired
    private TemplateEngine templateEngine;

    @Autowired
    ResourceLoader resourceLoader;

    private boolean ponerMarca = false;

    private static SimpleDateFormat fileDateFormatNumRef = new SimpleDateFormat("dd/MM/yyyy");

    public void createPDF(PlantillaPDF plantillaPDF,String dirTemp,String dirFinal) throws IOException, DocumentException {
        Map<String, Object> variables = new HashMap<>();
        String codigoGrupo = plantillaPDF.getRecibo().getCodigoGrupo();
		String htmlTemplateName = "recibo_" + codigoGrupo;
        DecimalFormat df = new DecimalFormat("#0.00");


        // Seteo de variables de Recibo
        variables.put("idRecibo", plantillaPDF.getRecibo().getIdRecibo());
        variables.put("idlote", plantillaPDF.getRecibo().getIdLote());
        variables.put("periodoMes", plantillaPDF.getRecibo().getPeriodoMes());
        variables.put("periodoAnio", plantillaPDF.getRecibo().getPeriodoAnio());
        variables.put("cuit", plantillaPDF.getRecibo().getCuit());
        variables.put("apellidoNombre", plantillaPDF.getRecibo().getApellidoNombre());
        variables.put("haberesTotalImporte",df.format( plantillaPDF.getRecibo().getHaberesTotalImporte()).replace(",", "."));
        variables.put("descuentoTotalImporte",df.format( plantillaPDF.getRecibo().getDescuentoTotalImporte()).replace(",", "."));
        variables.put("liquidoCobrar", df.format( plantillaPDF.getRecibo().getLiquidoCobrar()).replace(",", "."));
        variables.put("cantidadDetalle", plantillaPDF.getRecibo().getCantidadDetalle());
        variables.put("codigoGrupo", codigoGrupo);
        variables.put("haberesTotalAjuste", df.format( plantillaPDF.getRecibo().getHaberesTotalAjuste()).replace(",", "."));
        variables.put("descuentoTotalAjuste",df.format(  plantillaPDF.getRecibo().getDescuentoTotalAjuste()).replace(",", "."));
        variables.put("numero", plantillaPDF.getRecibo().getNumero());
        variables.put("jurisdiccion", plantillaPDF.getRecibo().getJurisdiccion());
        variables.put("unidad", plantillaPDF.getRecibo().getUnidad());
        variables.put("cargo", plantillaPDF.getRecibo().getCargo());
        variables.put("puesto", plantillaPDF.getRecibo().getPuesto());
        variables.put("ficha", plantillaPDF.getRecibo().getFicha());
        variables.put("fechaIngreso", fileDateFormatNumRef.format(plantillaPDF.getRecibo().getFechaIngreso()));
        variables.put("numeroComprobante", plantillaPDF.getRecibo().getNumeroComprobante());
        variables.put("antiguedad", plantillaPDF.getRecibo().getAntiguedad());
        variables.put("obraSocial", plantillaPDF.getRecibo().getObraSocial());
        variables.put("sucursalCuenta", plantillaPDF.getRecibo().getSucursalCuenta());
        variables.put("documento", plantillaPDF.getRecibo().getDocumento());
        variables.put("comunicaciones", plantillaPDF.getRecibo().getComunicaciones());
        variables.put("detalles", plantillaPDF.getRecibo().getDetalles());
        variables.put("liquidoCobrarLetras", plantillaPDF.getRecibo().getLiquidoAcobrarLetras());

        List<DetalleReciboForHtml> listaHaberes = new ArrayList<DetalleReciboForHtml>();
        List<DetalleReciboForHtml> listaDescuentos = new ArrayList<DetalleReciboForHtml>();

        for (DetalleRecibo detalleRecibo : plantillaPDF.getRecibo().getDetalles()) {
        	DetalleReciboForHtml detalleHtml = new DetalleReciboForHtml();
        	detalleHtml.setConcepto(detalleRecibo.getConcepto());
        	if(detalleRecibo.getImporte().compareTo(BigDecimal.valueOf(0)) == 0) {
        		detalleHtml.setImporte("");
        	}else {
        		detalleHtml.setImporte(df.format(detalleRecibo.getImporte()).replace(",", "."));

        	}
        	if(detalleRecibo.getAjuste().compareTo(BigDecimal.valueOf(0)) == 0) {
        		detalleHtml.setAjuste("");

        	}else {
        		detalleHtml.setAjuste(df.format(detalleRecibo.getAjuste()).replace(",", "."));
        	}
        	if ("ASIG".equalsIgnoreCase(detalleRecibo.getTipo())) {

        		listaHaberes.add(detalleHtml);
        	} else if ("DESC".equalsIgnoreCase(detalleRecibo.getTipo())) {
        		listaDescuentos.add(detalleHtml);
        	}

		}

        if(Constantes.GCBA.equalsIgnoreCase(codigoGrupo)){
			ponerMarca=marcaGcba;
		}else if(Constantes.BOMBEROS.equalsIgnoreCase(codigoGrupo)){
			ponerMarca=marcaBoberos;
		}else if(Constantes.IVC.equalsIgnoreCase(codigoGrupo)){
			ponerMarca=marcaIvc;
		}else if(Constantes.PDC.equalsIgnoreCase(codigoGrupo)){
			ponerMarca=marcaPdc;
		}else if(Constantes.ISSP.equalsIgnoreCase(codigoGrupo)){
			ponerMarca=marcaIssp;
		}

        String cuil = plantillaPDF.getRecibo().getCuit().replace("-", "").trim();

        variables.put("listaHaberes", listaHaberes);
        variables.put("listaDescuentos", listaDescuentos);
        variables.put("pdfName",
        		cuil.substring(cuil.length() - 1) + "-" +
        		cuil + "-" +
        		cuil + "_" +
        		plantillaPDF.getRecibo().getPeriodoAnio() + String.format("%02d", RdsUtils.getInstance().monthStringToMonthInt(plantillaPDF.getRecibo().getPeriodoMes())) + "_" +
        		plantillaPDF.getRecibo().getIdRecibo()
        );

        HtmlToPdf pdfFinal = new HtmlToPdf();

        pdfFinal.parseoHtmlPdf(templateEngine, variables, resourceLoader, htmlTemplateName, dirTemp, dirFinal, ponerMarca, pathCss, pathImg+codigoGrupo.toUpperCase()+"/");
    }

    public void previsualizarPdf(String codigoGrupo,String dirTemp,String dirFinal,String pathImg1,String pathCss1) throws IOException, DocumentException {
    	Map<String, Object> variables = new HashMap<>();

 		String htmlTemplateName = "_recibo_" + codigoGrupo;
        DecimalFormat df = new DecimalFormat("#0.00");


        // Seteo de variables de Recibo
        Recibo recibo = this.setRecibo(codigoGrupo);
        variables.put("idRecibo", recibo.getIdRecibo());
        variables.put("idlote", recibo.getIdLote());
        variables.put("periodoMes", recibo.getPeriodoMes());
		variables.put("periodoAnio", recibo.getPeriodoAnio());
		variables.put("cuit", recibo.getCuit());
		variables.put("apellidoNombre", recibo.getApellidoNombre());
		variables.put("haberesTotalImporte",df.format( recibo.getHaberesTotalImporte()).replace(",", "."));
		variables.put("descuentoTotalImporte",df.format( recibo.getDescuentoTotalImporte()).replace(",", "."));
		variables.put("liquidoCobrar", df.format( recibo.getLiquidoCobrar()).replace(",", "."));
		variables.put("cantidadDetalle", recibo.getCantidadDetalle());
		variables.put("codigoGrupo", codigoGrupo);
		variables.put("haberesTotalAjuste", df.format( recibo.getHaberesTotalAjuste()).replace(",", "."));
		variables.put("descuentoTotalAjuste",df.format(  recibo.getDescuentoTotalAjuste()).replace(",", "."));
		variables.put("numero", recibo.getNumero());
		variables.put("jurisdiccion", recibo.getJurisdiccion());
		variables.put("unidad", recibo.getUnidad());
        variables.put("cargo", recibo.getCargo());
        variables.put("puesto", recibo.getPuesto());
        variables.put("ficha", recibo.getFicha());
        variables.put("fechaIngreso", fileDateFormatNumRef.format(recibo.getFechaIngreso()));
        variables.put("numeroComprobante", recibo.getNumeroComprobante());
        variables.put("antiguedad", recibo.getAntiguedad());
        variables.put("obraSocial", recibo.getObraSocial());
        variables.put("sucursalCuenta", recibo.getSucursalCuenta());
        variables.put("documento", recibo.getDocumento());
        variables.put("comunicaciones", recibo.getComunicaciones());
        variables.put("detalles", recibo.getDetalles());
        variables.put("liquidoCobrarLetras", recibo.getLiquidoAcobrarLetras());

        List<DetalleReciboForHtml> listaHaberes = new ArrayList<DetalleReciboForHtml>();
        List<DetalleReciboForHtml> listaDescuentos = new ArrayList<DetalleReciboForHtml>();

        for (DetalleRecibo detalleRecibo : recibo.getDetalles()) {
        	DetalleReciboForHtml detalleHtml = new DetalleReciboForHtml();
         	detalleHtml.setConcepto(detalleRecibo.getConcepto());
         	if(detalleRecibo.getImporte().compareTo(BigDecimal.valueOf(0)) == 0) {
         		detalleHtml.setImporte("");
         	}else {
         		detalleHtml.setImporte(df.format(detalleRecibo.getImporte()).replace(",", "."));

         	}
         	if(detalleRecibo.getAjuste().compareTo(BigDecimal.valueOf(0)) == 0) {
         		detalleHtml.setAjuste("");

         	}else {
         		detalleHtml.setAjuste(df.format(detalleRecibo.getAjuste()).replace(",", "."));
         	}
         	if ("ASIG".equalsIgnoreCase(detalleRecibo.getTipo())) {

         		listaHaberes.add(detalleHtml);
         	} else if ("DESC".equalsIgnoreCase(detalleRecibo.getTipo())) {
         		listaDescuentos.add(detalleHtml);
         	}

 		}

        if(Constantes.GCBA.equalsIgnoreCase(codigoGrupo)){
 			ponerMarca=marcaGcba;
 		}else if(Constantes.BOMBEROS.equalsIgnoreCase(codigoGrupo)){
 			ponerMarca=marcaBoberos;
 		}else if(Constantes.IVC.equalsIgnoreCase(codigoGrupo)){
 			ponerMarca=marcaIvc;
 		}else if(Constantes.PDC.equalsIgnoreCase(codigoGrupo)){
 			ponerMarca=marcaPdc;
 		}else if(Constantes.ISSP.equalsIgnoreCase(codigoGrupo)){
 			ponerMarca=marcaIssp;
 		}

        String cuil = recibo.getCuit().replace("-", "").trim();

        variables.put("listaHaberes", listaHaberes);
        variables.put("listaDescuentos", listaDescuentos);
        variables.put("pdfName",
        		cuil.substring(cuil.length() - 1) + "-" +
        		cuil + "-" +
         		cuil + "_" +
         		recibo.getPeriodoAnio() + String.format("%02d", RdsUtils.getInstance().monthStringToMonthInt(recibo.getPeriodoMes())) + "_" +
         		recibo.getIdRecibo()
         );

        HtmlToPdf pdfFinal = new HtmlToPdf();

        pdfFinal.parseoHtmlPdf(templateEngine, variables, resourceLoader, htmlTemplateName, dirTemp, dirFinal, ponerMarca, pathCss1, pathImg1+"/"+codigoGrupo+"/");
     }

    private Recibo setRecibo(String codigoGrupo) {
    	DetalleRecibo detalle1 = new DetalleRecibo();
	    DetalleRecibo detalle2 = new DetalleRecibo();
	    DetalleRecibo detalle3 = new DetalleRecibo();
	    DetalleRecibo detalle4 = new DetalleRecibo();
	    DetalleRecibo detalle5 = new DetalleRecibo();
	    DetalleRecibo detalle6 = new DetalleRecibo();
	    this.setDetalle(detalle1, Long.valueOf("1"), "ASIG", "haberes1", BigDecimal.valueOf(0), BigDecimal.valueOf(0), Long.valueOf("1"));
	    this.setDetalle(detalle2, Long.valueOf("2"), "ASIG", "haberes2", BigDecimal.valueOf(0), BigDecimal.valueOf(0), Long.valueOf("2"));
	    this.setDetalle(detalle3, Long.valueOf("3"), "ASIG", "haberes3", BigDecimal.valueOf(0), BigDecimal.valueOf(0), Long.valueOf("3"));

	    this.setDetalle(detalle4, Long.valueOf("4"), "DESC", "descuento1", BigDecimal.valueOf(0), BigDecimal.valueOf(0), Long.valueOf("4"));
	    this.setDetalle(detalle5, Long.valueOf("5"), "DESC", "descuento2", BigDecimal.valueOf(0), BigDecimal.valueOf(0), Long.valueOf("5"));
	    this.setDetalle(detalle6, Long.valueOf("6"), "DESC", "descuento3", BigDecimal.valueOf(0), BigDecimal.valueOf(0), Long.valueOf("6"));
	    ArrayList<DetalleRecibo> detalles = new ArrayList<DetalleRecibo>();
	    detalles.add(detalle1);
	    detalles.add(detalle2);
	    detalles.add(detalle3);
	    detalles.add(detalle4);
	    detalles.add(detalle5);
	    detalles.add(detalle6);
	 	Recibo recibo = new Recibo();
	    recibo.setIdRecibo(RandomUtils.nextLong(00,0));
	    recibo.setApellidoNombre(RandomStringUtils.randomAlphabetic(10));
	    recibo.setCuit("xx-" + RandomStringUtils.randomNumeric(0) + "-" + RandomStringUtils.randomNumeric(0));
	    recibo.setPeriodoMes("xxx");
	    recibo.setPeriodoAnio(Long.valueOf("0"));
	    recibo.setApellidoNombre("xxxxx");
	    recibo.setHaberesTotalImporte(BigDecimal.valueOf(0));
	    recibo.setDescuentoTotalImporte(BigDecimal.valueOf(0));
	    recibo.setLiquidoCobrar(BigDecimal.valueOf(0));
	    recibo.setCodigoGrupo(codigoGrupo);
	    recibo.setHaberesTotalAjuste(BigDecimal.valueOf(0));
	    recibo.setDescuentoTotalAjuste(BigDecimal.valueOf(0));
	    recibo.setNumero("xxxx");
	    recibo.setObraSocial("xxxxx");
	    recibo.setSucursalCuenta("xxxx");
	    recibo.setDocumento(" xxxxx");
	    recibo.setDetalles(detalles);
	    recibo.setJurisdiccion("xxxx");
	    recibo.setUnidad("xxxx");
	    recibo.setCargo("xxxx");
	    recibo.setPuesto("xxxx");
	    recibo.setFicha("xxxx");
	    recibo.setNumeroComprobante("xxxx");
	    recibo.setAntiguedad("xxxx");

     	recibo.setFechaIngreso(new Date());
     	return recibo;
    }

    private DetalleRecibo setDetalle(DetalleRecibo detalle, Long idDetalle, String tipo, String concepto,BigDecimal importe, BigDecimal ajuste, Long ordenTipo) {
     	detalle.setIdDetalle(idDetalle);
     	detalle.setTipo(tipo);
     	detalle.setConcepto(concepto);
     	detalle.setImporte(importe);
     	detalle.setAjuste(ajuste);
     	detalle.setOrdenTipo(ordenTipo);
     	return detalle;
    }


}
