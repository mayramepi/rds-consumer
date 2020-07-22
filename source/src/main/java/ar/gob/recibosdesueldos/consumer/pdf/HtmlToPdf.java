package ar.gob.recibosdesueldos.consumer.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.AbstractImageProvider;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

@Component
public class HtmlToPdf {

	public void parseoHtmlPdf(TemplateEngine templateEngine, Map<String, Object> variables,
			ResourceLoader resourceLoader, String htmlTemplateName, String dirTemp, String dirFinal, boolean ponerMarca,
			String pathCss, String pathImg) {

		try {
			Document document = new Document(PageSize.A4);
			File pdfTemporalFile;
			pdfTemporalFile = this.createTempFile((String) variables.get("pdfName"), "temp.pdf", dirTemp);
			String codigoGrupo = (String) variables.get("codigoGrupo");
			FileOutputStream ficheroPdf = new FileOutputStream(pdfTemporalFile);
			PdfWriter writer;
			writer = PdfWriter.getInstance(document, ficheroPdf);

			document.open();

			IContext context = new Context(Locale.getDefault(), variables);
			StringWriter out = new StringWriter();
			templateEngine.process(htmlTemplateName, context, out);
			out.flush();

		    final HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);

	        htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());

	        htmlContext.setImageProvider(
	        		new AbstractImageProvider() {
	                    @Override
	                    public Image retrieve(String s) {
	                        try {
	                        	return Image.getInstance(resourceLoader.getResource("file:" + pathImg + s).getURL());
	                        } catch (Exception e) {
	                            return null;
	                        }
	                    }

	        			public String getImageRootPath() {
	                        try {
	                            return resourceLoader.getResource("file:" + pathImg).getURL().getPath();
	                        } catch (IOException e) {
	                            return null;
	                        }
	                    }
	        		}
	        );



			CSSResolver cssResolver = new StyleAttrCSSResolver();
			CssFile cssFile = XMLWorkerHelper
					.getCSS(resourceLoader.getResource("file:" + pathCss).getInputStream());
			cssResolver.addCss(cssFile);

			final Pipeline<?> pipeline = new CssResolverPipeline(cssResolver,
					new HtmlPipeline(htmlContext, new PdfWriterPipeline(document, writer)));
			final XMLWorker worker = new XMLWorker(pipeline, true);
			final XMLParser parser = new XMLParser(worker);

			parser.parse(new StringReader(out.toString()));

			document.close();
			if(ponerMarca) {
				ponerMarcaAgua(variables, dirTemp, pdfTemporalFile, codigoGrupo, dirFinal, pathImg);
			}else {
				Files.move(Paths.get(pdfTemporalFile.toURI()), Paths.get(dirFinal + variables.get("pdfName") + ".pdf"));
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (DocumentException e1) {
			e1.printStackTrace();
		}

	}

	private void ponerMarcaAgua(Map<String, Object> variables, String dirTemp, File pdfTemporalFile,String codigoGrupo, String dirFinal,
			String pathImg)
			throws IOException, DocumentException, FileNotFoundException, BadElementException, MalformedURLException {

		
		PdfReader reader = new PdfReader(pdfTemporalFile.getAbsolutePath());

		int number_of_pages = reader.getNumberOfPages();

		String pdfFinalPath = dirFinal +(String) variables.get("pdfName") + ".pdf";
		PdfStamper pdfStamper = new PdfStamper(reader,new FileOutputStream(pdfFinalPath));
		String htmlTemplateName = "marca_agua_" + codigoGrupo;
		Image watermark_image = Image.getInstance(ResourceUtils.getURL("file:" + pathImg + htmlTemplateName +".gif"));
		int i = 0;
		watermark_image.setAbsolutePosition(210, 385);
		watermark_image.scaleToFit(450, 230);
		PdfContentByte add_watermark;
		while (i < number_of_pages) {
			i++;
			add_watermark = pdfStamper.getUnderContent(i);
			add_watermark.addImage(watermark_image);
		}

		pdfStamper.close();
		reader.close();
		this.removeTemporal(dirTemp +(String) variables.get("pdfName") + "temp.pdf");


	}

	private File createTempFile(String prefix, String suffix, String dir) {
		String tempDir = dir;
		String fileName = (prefix != null ? prefix : "") + (suffix != null ? suffix : "");
		return new File(tempDir, fileName);
	}

	private void removeTemporal(String pdfTemporal) {
		File ruta = new File(pdfTemporal);
		ruta.delete();

	}
}
