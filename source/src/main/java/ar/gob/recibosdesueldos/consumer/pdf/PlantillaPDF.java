package ar.gob.recibosdesueldos.consumer.pdf;

import ar.gob.recibosdesueldos.model.messaging.Recibo;

public class PlantillaPDF {

	private String plantillaNombre;

	private String htmlEncabezado;

	private String htmlPieDePagina;

	private Recibo recibo;

	public String getPlantillaNombre() {
		return plantillaNombre;
	}

	public void setPlantillaNombre(String plantillaNombre) {
		this.plantillaNombre = plantillaNombre;
	}

	public String getHtmlEncabezado() {
		return htmlEncabezado;
	}

	public void setHtmlEncabezado(String htmlEncabezado) {
		this.htmlEncabezado = htmlEncabezado;
	}

	public String getHtmlPieDePagina() {
		return htmlPieDePagina;
	}

	public void setHtmlPieDePagina(String htmlPieDePagina) {
		this.htmlPieDePagina = htmlPieDePagina;
	}

	public Recibo getRecibo() {
		return recibo;
	}

	public void setRecibo(Recibo recibo) {
		this.recibo = recibo;
	}

}
