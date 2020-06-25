package ar.gob.recibosdesueldos.consumer.pdf;


import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.gob.recibosdesueldos.commons.utils.RdsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import com.itextpdf.text.DocumentException;

import ar.gob.recibosdesueldos.model.messaging.DetalleRecibo;
import ar.gob.recibosdesueldos.model.messaging.DetalleReciboForHtml;
import ar.gob.recibossueldos.consumer.constant.Constantes;

@Component
@Service
public class GeneratePDF {
	
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
    private static String pdfTemporal = "";
    
    private static SimpleDateFormat fileDateFormatNumRef = new SimpleDateFormat("dd/MM/yyyy");
 
    @SuppressWarnings("deprecation")
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
        
       pdfFinal.parseoHtmlPdf(templateEngine, variables, resourceLoader, htmlTemplateName,dirTemp,dirFinal,ponerMarca);
    }

   
}
