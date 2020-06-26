package ar.gob.recibosdesueldos.consumer;

import ar.gob.recibosdesueldos.model.messaging.DetalleRecibo;
import ar.gob.recibosdesueldos.model.messaging.Recibo;
import ar.gob.recibossueldos.consumer.constant.Constantes;

import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jms.Queue;

@SpringBootTest
@ActiveProfiles("dev")
class RdsConsumerApplicationTests {
    @Autowired
    private JmsTemplate jmsTemplate;
	private List<DetalleRecibo> detalles;
	Queue queue = new ActiveMQQueue("lotes.queue");
	private List<Recibo> listaRecibos = new ArrayList<Recibo>();

//    @Test
//    void contextLoads() {
//    }

    @Test
    void createRecibos() {
//    	Queue queue = new ActiveMQQueue("lotes.queue");
       	Recibo reciboGCBA=this.setRecibo(Constantes.GCBA);
//       	Recibo reciboBOMBEROS=this.setRecibo(Constantes.BOMBEROS);
//       	Recibo reciboISSP=this.setRecibo(Constantes.ISSP);
//    	Recibo reciboIVC=this.setRecibo(Constantes.IVC);
//    	Recibo reciboPDC=this.setRecibo(Constantes.PDC);
    	
       	listaRecibos.add(reciboGCBA);
//       	listaRecibos.add(reciboBOMBEROS);
//       	listaRecibos.add(reciboISSP);
//       	listaRecibos.add(reciboIVC);
//    	listaRecibos.add(reciboPDC);
       	for (Recibo recibo : listaRecibos) {
			
       		this.send(recibo,queue);
		}
    } 


    private void send(Recibo recibo,Queue queue ) {
    	
    	jmsTemplate.convertAndSend(queue, recibo);
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
    
    private Recibo setRecibo(String codigoGrupo) {
        DetalleRecibo detalle1 = new DetalleRecibo();
        DetalleRecibo detalle2 = new DetalleRecibo();
        DetalleRecibo detalle3 = new DetalleRecibo();
        DetalleRecibo detalle4 = new DetalleRecibo();
        DetalleRecibo detalle5 = new DetalleRecibo();
        DetalleRecibo detalle6 = new DetalleRecibo();
        this.setDetalle(detalle1, Long.valueOf("1"), "ASIG", "haberes1", BigDecimal.valueOf(111.00), BigDecimal.valueOf(11.00), Long.valueOf("1"));
        this.setDetalle(detalle2, Long.valueOf("2"), "ASIG", "haberes2", BigDecimal.valueOf(0), BigDecimal.valueOf(0.00), Long.valueOf("2"));
        this.setDetalle(detalle3, Long.valueOf("3"), "ASIG", "haberes3", BigDecimal.valueOf(113.03), BigDecimal.valueOf(13.03), Long.valueOf("3"));
       
        this.setDetalle(detalle4, Long.valueOf("4"), "DESC", "descuento1", BigDecimal.valueOf(100.00), BigDecimal.valueOf(0), Long.valueOf("4"));
        this.setDetalle(detalle5, Long.valueOf("5"), "DESC", "descuento2", BigDecimal.valueOf(115.05), BigDecimal.valueOf(0), Long.valueOf("5"));
        this.setDetalle(detalle6, Long.valueOf("6"), "DESC", "descuento3", BigDecimal.valueOf(116.06), BigDecimal.valueOf(16.06), Long.valueOf("6"));
        detalles = new ArrayList<DetalleRecibo>();
        detalles.add(detalle1);
        detalles.add(detalle2);
        detalles.add(detalle3);
        detalles.add(detalle4);
        detalles.add(detalle5);
        detalles.add(detalle6);
    	Recibo recibo = new Recibo();
        recibo.setIdRecibo(RandomUtils.nextLong(1,100));
        recibo.setApellidoNombre(RandomStringUtils.randomAlphabetic(10));
        recibo.setCuit("20-" + RandomStringUtils.randomNumeric(8) + "-" + RandomStringUtils.randomNumeric(1));
        recibo.setPeriodoMes("Ene");
        recibo.setPeriodoAnio(Long.valueOf("20"));
        recibo.setApellidoNombre("Cosme Fulanito");
        recibo.setHaberesTotalImporte(BigDecimal.valueOf(12345.67));
        recibo.setDescuentoTotalImporte(BigDecimal.valueOf(2345.53));
        recibo.setLiquidoCobrar(BigDecimal.valueOf(11234.00));
        recibo.setCodigoGrupo(codigoGrupo);
        recibo.setHaberesTotalAjuste(BigDecimal.valueOf(2345.50));
        recibo.setDescuentoTotalAjuste(BigDecimal.valueOf(0));
        recibo.setNumero("1234");
        recibo.setObraSocial("Obra social CABA");
        recibo.setSucursalCuenta("algun Banco");
        recibo.setDocumento(" Dni 22222278");
        recibo.setDetalles(detalles);
        recibo.setJurisdiccion("Jurisdiccion");
        recibo.setUnidad("unidad");
        recibo.setCargo("Empleado");
        recibo.setPuesto("Puesto");
        recibo.setFicha("Ficha");
        recibo.setNumeroComprobante("123456");
        recibo.setAntiguedad("4 meses");
        
        recibo.setFechaIngreso(new Date());
        return recibo;
    	
    }
}
