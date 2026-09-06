package SRR.Utilidades;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


 // Genera los reportes PDF de todas las pantallas. Recibe los datos ya
 // convertidos a texto para no depender de ningun DTO

public class ReportePdf {

    private ReportePdf() {
    }

    //Reporte en hoja vertical. Sirve para tablas de pocas columnas.
    public static void generar(String titulo, String[] columnas,
                               List<String[]> filas, File destino) {
        escribir(titulo, columnas, filas, null, destino, PageSize.A4);
    }

    //Reporte en hoja horizontal, para tablas anchas como las matrices.
    public static void generarHorizontal(String titulo, String[] columnas,
                                         List<String[]> filas, File destino) {
        escribir(titulo, columnas, filas, null, destino, PageSize.A4.rotate());
    }

    // Reporte con tabla y grafico de barras, para las estadisticas.
    public static void generarConGrafico(String titulo, String[] columnas, List<String[]> filas,
                                         byte[] imagenPng, File destino) {
        escribir(titulo, columnas, filas, imagenPng, destino, PageSize.A4);
    }


    //  Para tablas con columnas dinamicas: lee los encabezados y los valores
    //  directamente del TableView, asi el reporte refleja lo que se ve en
    //  pantalla sin importar como esta armada la tabla.

    public static <S> void generarDesdeTabla(String titulo, TableView<S> tabla,
                                             File destino, boolean horizontal) {

        List<TableColumn<S, ?>> columnas = new ArrayList<>(tabla.getColumns());

        String[] encabezados = new String[columnas.size()];
        for (int i = 0; i < columnas.size(); i++) {
            encabezados[i] = columnas.get(i).getText();
        }

        List<String[]> filas = new ArrayList<>();
        for (S item : tabla.getItems()) {
            String[] fila = new String[columnas.size()];
            for (int i = 0; i < columnas.size(); i++) {
                Object valor = columnas.get(i).getCellObservableValue(item) == null
                        ? null : columnas.get(i).getCellObservableValue(item).getValue();
                fila[i] = valor == null ? "" : valor.toString();
            }
            filas.add(fila);
        }

        if (horizontal) {
            generarHorizontal(titulo, encabezados, filas, destino);
        } else {
            generar(titulo, encabezados, filas, destino);
        }
    }

    // imagenPng puede ser null: los reportes de lista no llevan grafico
    private static void escribir(String titulo, String[] columnas, List<String[]> filas,
                                 byte[] imagenPng, File destino, PageSize tamano) {

        File carpeta = destino.getParentFile();
        if (carpeta != null) {
            carpeta.mkdirs();
        }

        try (PdfWriter escritor = new PdfWriter(destino);
             PdfDocument pdf = new PdfDocument(escritor);
             Document documento = new Document(pdf, tamano)) {

            documento.add(new Paragraph(titulo)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            Table tabla = new Table(UnitValue.createPercentArray(columnas.length))
                    .useAllAvailableWidth();

            for (String columna : columnas) {
                tabla.addHeaderCell(new Cell()
                        .add(new Paragraph(columna))
                        .setBackgroundColor(ColorConstants.RED)
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            for (String[] fila : filas) {
                for (String valor : fila) {
                    tabla.addCell(new Cell()
                            .add(new Paragraph(valor == null ? "" : valor))
                            .setTextAlignment(TextAlignment.CENTER));
                }
            }

            documento.add(tabla);

            if (imagenPng != null) {
                Image imagen = new Image(ImageDataFactory.create(imagenPng));
                imagen.setAutoScale(true);
                documento.add(imagen);
            }

        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el PDF: " + destino, e);
        }
    }
}