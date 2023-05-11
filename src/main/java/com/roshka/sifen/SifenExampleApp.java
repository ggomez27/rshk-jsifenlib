package com.roshka.sifen;

import com.roshka.sifen.core.SifenConfig;
import com.roshka.sifen.core.beans.DocumentoElectronico;
import com.roshka.sifen.core.beans.EventosDE;
import com.roshka.sifen.core.beans.ValidezFirmaDigital;
import com.roshka.sifen.core.beans.response.*;
import com.roshka.sifen.core.exceptions.SifenException;
import com.roshka.sifen.core.fields.response.TgResProc;
import com.roshka.sifen.core.fields.response.TxProtDe;
import com.roshka.sifen.core.fields.response.batch.TgResProcLote;
import com.roshka.sifen.core.fields.response.event.TgResProcEVe;
import com.roshka.sifen.core.fields.response.ruc.TxContRuc;
import com.roshka.sifen.internal.ctx.GenerationCtx;

import java.util.Collections;

public class SifenExampleApp {
    public static void main(String[] args) throws SifenException {

        // Se carga la Configuración del SIFEN
        SifenConfig config = SifenConfig.cargarConfiguracion("C:\\.AoC\\sifen.properties");
        config.setHabilitarNotaTecnica13(true);
        Sifen.setSifenConfig(config);
        GenerationCtx generationCtx = GenerationCtx.getDefaultFromConfig(config);

        // Consulta de RUC
        System.out.println(pruebaConsultaRUC("80089752"));

        // Creación de una Factura
        DocumentoElectronico DE = SifenExampleHelper.cargarDE("0000021", true, generationCtx);
        System.out.println("CDC del Documento Electrónico -> " + DE.obtenerCDC());

        // Se verifica que la Factura sea valida
        ValidezFirmaDigital validez = Sifen.validarFirmaDEDesdeXml(DE.generarXml(generationCtx));
        if (validez.isValido()) {
            System.out.println("El documento es valido");

            // Emisión de una Factura
            //    System.out.println(pruebaRecepcionDE(DE));

            // Emisión Lote de DE
            // System.out.println(pruebaRecepcionYConsultaLoteDE(DE));

            // Anulación de la Factura recien Emitida
            //  System.out.println(pruebaAnularFactura(DE));

            // Nominación de la Factura recien Emitida
             System.out.println(pruebaNominarFactura(DE));
        } else System.out.println(validez.getMotivoInvalidez());

    }

    /**
     * Metodo que realiza una consulta de RUC al Sifen y devuelve como resultado una cadena con los datos
     * recibidos de la consulta.
     *
     * @param ruc El RUC que se desea consultar
     * @return Una cadena formateada con la respuesta de la Consulta
     * @throws SifenException Si la configuración de Sifen no fue establecida o, si algún dato necesario para la
     *                        consulta no pudo ser encontrado o, si la consulta no pudo ser realizada.
     */
    public static String pruebaConsultaRUC(String ruc) throws SifenException {

        RespuestaConsultaRUC respuesta = Sifen.consultaRUC(ruc);

        //Un dCodRes con código 0500 indica que el RUC no existe (capítulo 12.3.5.3, Manual Técnico de Sifen)
        if (respuesta.getdCodRes().equals("0500"))
            return String.format("El RUC %s no existe", ruc);

        //Un dCodRes con código 0501 indica que el RUC no tiene permiso para utilizar el WS. (capítulo 12.3.5.3, Manual Técnico de Sifen)
        if (respuesta.getdCodRes().equals("0501"))
            return "El RUC no tiene permiso para utilizar el WS";

        //Caso contrario, dCodRes tendrá valor 0502, y existirá un xContRuc con los datos del resultado (Schema XML 16)
        TxContRuc xContRuc = respuesta.getxContRUC();

        return String.format("Consulta de RUC:\n" +
                        "\tRUC: %s\n" +
                        "\tRazon Social: %s\n" +
                        "\tEstado: %s\n" +
                        "\tEs facturador: %s\n" +
                        "Fin Consulta de RUC",
                xContRuc.getdRUCCons(),
                xContRuc.getdRazCons(),
                xContRuc.getdCodEstCons(),
                xContRuc.getdRUCFactElec());
    }

    /**
     * Método que envía un DE al Sifen, y devuelve una cadena con los datos recibidos en la respuesta de la
     * Recepción del DE.
     * <p>
     * Este método hace uso del método Sifen.recepcionDE(de) de la librería RSHK-JSIFENLIB
     *
     * @param de El Documento Electrónico a enviar.
     * @return Una cadena formateada con el resultado de la Recepción de DE realizada
     * @throws SifenException Una cadena formateada con el resultado de la Recepción y Consulta de Lote de DE realizadas (en el caso
     *                        de la última, sólo si se pudo realizar la consulta)
     */
    public static String pruebaRecepcionDE(DocumentoElectronico de) throws SifenException {

        RespuestaRecepcionDE respuesta = Sifen.recepcionDE(de);

        // Protocolo de Procesamiento del DE (Schema XML 3)
        TxProtDe xProtDe = respuesta.getxProtDE();

        String resultado = String.format("Recepción de Factura\n" +
                        "\tCodigo de Estado: %d\n" +
                        "\tEstado Resultado: %s\n" +
                        "\tRespuestas de Procesamiento:\n",
                respuesta.getCodigoEstado(),
                xProtDe.getdEstRes());

        // Por cada Respuesta de Procesamiento en xProtDe
        for (TgResProc gResProc : xProtDe.getgResProc()) {

            String subString = String.format("\t* Cod %s: %s\n", gResProc.getdCodRes(), gResProc.getdMsgRes());
            resultado = resultado.concat(subString);
        }

        return resultado.concat("Fin Recepción de Factura");
    }

    /**
     * Método que envía un Evento de Lote de DE al Sifen, y si la recepción fue exitosa y se recibe el número de Lote,
     * realiza una Consulta de Lote de DE con dicho número. Devuelve una cadena con los datos recibidos en las
     * respuestas de Recepción y Consulta de Lote de DE (en el caso del último, solo si se pudo realizar la consulta)
     * <p>
     * Este método hace uso del método Sifen.recepcionLoteDE(deList) y Sifen.consultaLoteDE(nroLote)
     * de la librería RSHK-JSIFENLIB
     *
     * @param de El Documento Electrónico a enviar en el lote.
     * @return Una cadena formateada con el resultado de la Recepción y Consulta de Lote de DE realizadas (en el caso
     * de la última, sólo si se pudo realizar la consulta)
     * @throws SifenException Una cadena formateada con el resultado de la Recepción y Consulta de Lote de DE realizadas (en el caso
     *                        de la última, sólo si se pudo realizar la consulta)
     */
    public static String pruebaRecepcionYConsultaLoteDE(DocumentoElectronico de) throws SifenException {
        RespuestaRecepcionLoteDE respuestaRecepcion = Sifen.recepcionLoteDE(Collections.singletonList(de));
        String resultado = String.format("Recepción de Lote DE\n" +
                        "\tCodigo Resultado: %s\n" +
                        "\tNumero de Lote: %s\n",
                respuestaRecepcion.getdCodRes(),
                respuestaRecepcion.getdProtConsLote());
        //dCodRes puede ser 300 o 301 para recibido con exito o no encolado para procesamiento, respectivamente
        //dProtConsLote es el numero de lote recibido, generado solo si dCodRes=0300

        if (respuestaRecepcion.getdCodRes().equals("0301"))
            return resultado.concat("\tLote no encolado\nFin Recepción de Lote DE\nConsulta de Lote DE no realizada");

        else resultado = resultado.concat("Fin Recepción de Lote DE\nConsulta de Lote DE\n");

        RespuestaConsultaLoteDE respuestaConsulta = Sifen.consultaLoteDE(respuestaRecepcion.getdProtConsLote());

        // Por cada Respuesta de Procesamiento de Lote
        for (TgResProcLote gResProcLote : respuestaConsulta.getgResProcLoteList()) {

            String sub = String.format("\t%s: %s\n",
                    respuestaConsulta.getdCodResLot(), respuestaConsulta.getdMsgResLot());

            // Por cada Respuesta de Procesamiento:
            for (TgResProc gResProc : gResProcLote.getgResProc()) {
                sub = sub.concat(String.format("\t* Cod %s: %s\n", gResProc.getdCodRes(), gResProc.getdMsgRes()));
            }

            resultado = resultado.concat(sub);
        }

        return resultado.concat("Fin Consulta de Lote DE");
    }

    /**
     * Método que envía un Evento de Anulación al Sifen, y devuelve como resultado una cadena con el resultado
     * de la Recepción del Envío.
     * <p>
     * Este método hace uso del método Sifen.recepcionEvento(eventosDE) de la librería RSHK-JSIFENLIB
     *
     * @param de El Documento Electrónico a anular
     * @return Una cadena formateada con el resultado de la Recepción del Envío
     * @throws SifenException Si la configuración de Sifen no fue establecida o, si algún dato necesario para la
     *                        *                        consulta no pudo ser encontrado o, si la consulta no pudo ser realizada.
     */
    public static String pruebaAnularFactura(DocumentoElectronico de) throws SifenException {

        EventosDE eventosDE = SifenExampleHelper.crearEventoAnulacion(de.obtenerCDC());
        RespuestaRecepcionEvento respuesta = Sifen.recepcionEvento(eventosDE);

        String resultado = String.format("Anulación de Factura:\n" +
                        "\tCodigo de Estado: %s\n" +
                        "\tRespuesta Recepción Evento:\n",
                respuesta.getCodigoEstado());

        //Por cada Respuesta de Procesamiento de Evento:
        for (TgResProcEVe gResProcEVe : respuesta.getgResProcEVe()) {

            String sub = String.format("\t\tID %s: %s\n", gResProcEVe.getId(), gResProcEVe.getdEstRes());

            //Por cada Respuesta de Procesamiento:
            for (TgResProc gResProc : gResProcEVe.getgResProc()) {
                sub = sub.concat(String.format("\t\t* Cod %s: %s\n", gResProc.getdCodRes(), gResProc.getdMsgRes()));
            }

            resultado = resultado.concat(sub);
        }
        return resultado.concat("Fin Anulación de Factura");
    }

    /**
     * Método que envía un Evento de Nominacion al Sifen, y devuelve como resultado una cadena con el resultado
     * de la Recepción del Envío.
     * <p>
     * Este método hace uso del método Sifen.recepcionEvento(eventosDE) de la librería RSHK-JSIFENLIB
     *
     * @param de El Documento Electrónico a nominar
     * @return Una cadena formateada con el resultado de la Recepción del Envío
     * @throws SifenException Si la configuración de Sifen no fue establecida o, si algún dato necesario para la
     *                        *                        consulta no pudo ser encontrado o, si la consulta no pudo ser realizada.
     */
    public static String pruebaNominarFactura(DocumentoElectronico de) throws SifenException {

        EventosDE eventosDE = SifenExampleHelper.crearEventoNominacion(de.obtenerCDC());
        RespuestaRecepcionEvento respuesta = Sifen.recepcionEvento(eventosDE);

        String resultado = String.format("Nominacion de Factura:\n" +
                        "\tCodigo de Estado: %s\n" +
                        "\tRespuesta Recepción Evento:\n",
                respuesta.getCodigoEstado());

        //Por cada Respuesta de Procesamiento de Evento:
        for (TgResProcEVe gResProcEVe : respuesta.getgResProcEVe()) {

            String sub = String.format("\t\tID %s: %s\n", gResProcEVe.getId(), gResProcEVe.getdEstRes());

            //Por cada Respuesta de Procesamiento:
            for (TgResProc gResProc : gResProcEVe.getgResProc()) {
                sub = sub.concat(String.format("\t\t* Cod %s: %s\n", gResProc.getdCodRes(), gResProc.getdMsgRes()));
            }

            resultado = resultado.concat(sub);
        }
        return resultado.concat("Fin Nominacion de Factura");
    }

}
