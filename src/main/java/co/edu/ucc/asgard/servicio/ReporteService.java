package co.edu.ucc.asgard.servicio;

import co.edu.ucc.asgard.config.ParametrosSesiones;
import co.edu.ucc.asgard.modelo.Serie;
import co.edu.ucc.asgard.modelo.Socio;
import co.edu.ucc.asgard.motor.EstadoEjercicio;
import co.edu.ucc.asgard.motor.ResultadoProgresion;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

/** Genera el reporte PDF de progreso del socio para su entrenador (RF12, US014). */
@Service
public class ReporteService {

    private static final float MARGEN = 50f;
    private static final float ALTO_LINEA = 15f;
    private static final float[] COLUMNAS_PROGRESION = {50f, 210f, 280f, 350f, 420f, 480f};
    private static final String[] ENCABEZADOS_PROGRESION = {"Ejercicio", "Semaforo", "Decision", "Carga sug.",
        "Reps obj.", "Mejor 1RM"};
    private static final float[] COLUMNAS_SERIES = {50f, 150f, 330f, 390f, 440f, 480f};
    private static final String[] ENCABEZADOS_SERIES = {"Fecha y hora", "Ejercicio", "Peso", "Reps", "RPE",
        "1RM est."};

    private final SocioService socioService;
    private final EntrenadorService entrenadorService;
    private final ProgresionService progresionService;
    private final HistorialService historialService;
    private final EjercicioService ejercicioService;
    private final ZoneId zona;
    private final DateTimeFormatter formatoFecha;
    private final Clock reloj;

    public ReporteService(SocioService socioService, EntrenadorService entrenadorService,
            ProgresionService progresionService, HistorialService historialService, EjercicioService ejercicioService,
            ParametrosSesiones parametros, Clock reloj) {
        this.socioService = socioService;
        this.entrenadorService = entrenadorService;
        this.progresionService = progresionService;
        this.historialService = historialService;
        this.ejercicioService = ejercicioService;
        this.zona = parametros.zona();
        this.formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(zona);
        this.reloj = reloj;
    }

    public byte[] generarPdf(String socioId, Instant desde, Instant hasta) {
        Socio socio = socioService.obtener(socioId);
        ResultadoProgresion progresion = progresionService.evaluar(socio);
        List<Serie> series = historialService.consultar(socioId, null, desde, hasta);
        Map<String, String> nombres = ejercicioService.nombres();

        try (PDDocument documento = new PDDocument(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Escritor escritor = new Escritor(documento);
            escritor.texto("Asgard - Reporte de progreso de cargas", 16, true);
            escritor.texto("Generado: " + formatoFecha.format(Instant.now(reloj)) + " (" + zona + ")", 9, false);
            escritor.espacio();
            escritor.texto("Socio: " + socio.getNombre(), 11, true);
            escritor.texto("Documento: " + socio.getDocumentoIdentidad() + "   Nivel: " + socio.getNivel()
                    + "   Objetivo: " + socio.getObjetivo(), 10, false);
            if (socio.getDatosFisicos() != null) {
                escritor.texto(String.format(Locale.ROOT, "Peso corporal: %.1f kg   Estatura: %.0f cm   IMC: %.1f",
                        socio.getDatosFisicos().getPesoCorporal(), socio.getDatosFisicos().getEstatura(),
                        socio.getDatosFisicos().imc()), 10, false);
            }
            escritor.texto("Lesiones: " + (socio.getLesiones().isEmpty() ? "ninguna" : socio.getLesiones()), 10, false);
            if (socio.getEntrenadorId() != null) {
                escritor.texto("Entrenador: " + entrenadorService.obtener(socio.getEntrenadorId()).getNombre(), 10,
                        false);
            }
            escritor.texto("Periodo: " + (desde == null ? "inicio" : formatoFecha.format(desde)) + " a "
                    + (hasta == null ? "hoy" : formatoFecha.format(hasta)), 10, false);
            escritor.espacio();

            List<EstadoEjercicio> estados = progresion.lista();
            escritor.texto("Progresion por ejercicio (" + estados.size() + ")", 12, true);
            escritor.fila(ENCABEZADOS_PROGRESION, COLUMNAS_PROGRESION, true);
            for (EstadoEjercicio e : estados) {
                escritor.fila(new String[] {recortar(e.nombre(), 30), String.valueOf(e.semaforo()),
                    String.valueOf(e.decision()), String.format(Locale.ROOT, "%.1f kg", e.cargaSugerida()),
                    String.valueOf(e.repsObjetivo()), String.format(Locale.ROOT, "%.1f", e.mejorE1RM())},
                        COLUMNAS_PROGRESION, false);
                if (e.sustitutoNombre() != null) {
                    escritor.texto("    Sustituto sugerido: " + e.sustitutoNombre(), 9, false);
                }
            }
            if (estados.isEmpty()) {
                escritor.texto("Sin ejercicios para evaluar.", 10, false);
            }
            escritor.espacio();

            escritor.texto("Detalle de series (" + series.size() + ", de la mas reciente a la mas antigua)", 12, true);
            escritor.fila(ENCABEZADOS_SERIES, COLUMNAS_SERIES, true);
            for (Serie s : series) {
                escritor.fila(new String[] {formatoFecha.format(s.getFechaHora()),
                    recortar(nombres.getOrDefault(s.getEjercicioId(), s.getEjercicioId()), 34),
                    String.format(Locale.ROOT, "%.1f", s.getPeso()), String.valueOf(s.getRepeticiones()),
                    s.getRpe() == null ? "-" : String.format(Locale.ROOT, "%.1f", s.getRpe()),
                    String.format(Locale.ROOT, "%.1f", s.getE1RM())}, COLUMNAS_SERIES, false);
            }
            if (series.isEmpty()) {
                escritor.texto("Sin series registradas en el periodo.", 10, false);
            }
            escritor.espacio();
            escritor.texto("Reporte exportado por el socio. Asgard es un proyecto academico y no reemplaza el criterio "
                    + "del entrenador.", 8, false);
            escritor.cerrar();
            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el PDF", e);
        }
    }

    private static String recortar(String texto, int maximo) {
        if (texto == null) {
            return "-";
        }
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + ".";
    }

    /** Ayudante que escribe lineas y filas paginando automaticamente. */
    private static final class Escritor {

        private final PDDocument documento;
        private final PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private PDPageContentStream contenido;
        private float y;

        Escritor(PDDocument documento) throws IOException {
            this.documento = documento;
            nuevaPagina();
        }

        private void nuevaPagina() throws IOException {
            if (contenido != null) {
                contenido.close();
            }
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            contenido = new PDPageContentStream(documento, pagina);
            y = pagina.getMediaBox().getHeight() - MARGEN;
        }

        private void asegurarEspacio() throws IOException {
            if (y < MARGEN + ALTO_LINEA) {
                nuevaPagina();
            }
        }

        void texto(String linea, float tamano, boolean esNegrita) throws IOException {
            asegurarEspacio();
            escribir(linea, MARGEN, tamano, esNegrita);
            y -= ALTO_LINEA + (tamano > 11 ? 4 : 0);
        }

        void fila(String[] celdas, float[] columnas, boolean esEncabezado) throws IOException {
            asegurarEspacio();
            for (int i = 0; i < celdas.length; i++) {
                escribir(celdas[i], columnas[i], 9, esEncabezado);
            }
            y -= ALTO_LINEA;
        }

        void espacio() {
            y -= ALTO_LINEA / 2;
        }

        private void escribir(String texto, float x, float tamano, boolean esNegrita) throws IOException {
            contenido.beginText();
            contenido.setFont(esNegrita ? negrita : normal, tamano);
            contenido.newLineAtOffset(x, y);
            contenido.showText(limpiar(texto));
            contenido.endText();
        }

        /** Las fuentes estandar usan WinAnsi: se reemplazan caracteres fuera de Latin-1. */
        private static String limpiar(String texto) {
            if (texto == null) {
                return "";
            }
            StringBuilder limpio = new StringBuilder(texto.length());
            for (char c : texto.toCharArray()) {
                limpio.append(c >= 32 && c <= 255 ? c : '?');
            }
            return limpio.toString();
        }

        void cerrar() throws IOException {
            contenido.close();
        }
    }
}
