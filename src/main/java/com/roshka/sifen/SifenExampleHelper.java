package com.roshka.sifen;

import com.roshka.sifen.core.beans.DocumentoElectronico;
import com.roshka.sifen.core.beans.EventosDE;
import com.roshka.sifen.core.exceptions.SifenException;
import com.roshka.sifen.core.fields.request.de.*;
import com.roshka.sifen.core.fields.request.event.TgGroupTiEvt;
import com.roshka.sifen.core.fields.request.event.TrGEveNom;
import com.roshka.sifen.core.fields.request.event.TrGeVeCan;
import com.roshka.sifen.core.fields.request.event.TrGesEve;
import com.roshka.sifen.core.types.*;
import com.roshka.sifen.internal.ctx.GenerationCtx;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SifenExampleHelper {

    /**
     * Método que genera y devuelve un Documento Electrónico. Este método se escribió con referencia al código
     * del archivo SOAPTests.java
     *
     * @param dNumDoc Una cadena con el Número de Documento para la factura
     * @return Un Documento Electrónico generado
     */
    private static DocumentoElectronico crearDE(String dNumDoc, boolean innominado) {
        LocalDateTime currentDate = LocalDateTime.now();

        // Grupo A: Creación del Documento Electrónico
        DocumentoElectronico DE = new DocumentoElectronico();
        DE.setdFecFirma(currentDate);
        DE.setdSisFact((short) 1);

        // Grupo B: Campo de Operación del Documento Electrónico
        TgOpeDE gOpeDE = new TgOpeDE();
        gOpeDE.setiTipEmi(TTipEmi.NORMAL);
        DE.setgOpeDE(gOpeDE);

        // Grupo C: Campo del Timbrado del Documento Electrónico
        TgTimb gTimb = new TgTimb();
        gTimb.setiTiDE(TTiDE.FACTURA_ELECTRONICA);
        gTimb.setdNumTim(12557605);
        gTimb.setdEst("001");
        gTimb.setdPunExp("002");
        gTimb.setdNumDoc(dNumDoc);
        gTimb.setdFeIniT(LocalDate.parse("2019-07-24"));
        DE.setgTimb(gTimb);

        // Grupo D: Datos Generales de la Operacion
        TdDatGralOpe dDatGralOpe = new TdDatGralOpe();
        dDatGralOpe.setdFeEmiDE(currentDate);

        // D1: Campos inherentes a la Operación Comercial
        TgOpeCom gOpeCom = new TgOpeCom();
        gOpeCom.setiTipTra(TTipTra.PRESTACION_SERVICIOS);
        gOpeCom.setiTImp(TTImp.IVA);
        gOpeCom.setcMoneOpe(CMondT.PYG);
        dDatGralOpe.setgOpeCom(gOpeCom);

        // D2: Campos que identifican al Emisor
        TgEmis gEmis = new TgEmis();
        gEmis.setdRucEm("80089752");
        gEmis.setdDVEmi("8");
        gEmis.setiTipCont(TiTipCont.PERSONA_JURIDICA);
        gEmis.setdNomEmi("DE generado en ambiente de prueba - sin valor comercial ni fiscal");
        gEmis.setdDirEmi("Calle 1");
        gEmis.setdNumCas("1234");
        gEmis.setcDepEmi(TDepartamento.CAPITAL);
        gEmis.setcCiuEmi(1);
        gEmis.setdDesCiuEmi("ASUNCION (DISTRITO)");
        gEmis.setdTelEmi("983169005");
        gEmis.setdEmailE("gabrielgzb97@gmail.com");

        // D2.1: Campos que describen la Actividad Economica del Emisor
        List<TgActEco> gActEcoList = new ArrayList<>();

        TgActEco gActEco = new TgActEco();
        gActEco.setcActEco("62010");
        gActEco.setdDesActEco("ACTIVIDADES DE PROGRAMACIÓN INFORMÁTICA");
        gActEcoList.add(gActEco);

        gEmis.setgActEcoList(gActEcoList);
        dDatGralOpe.setgEmis(gEmis);

        // D3: Campos que identifican al Receptor
        TgDatRec gDatRec = new TgDatRec();
        gDatRec.setiNatRec(TiNatRec.NO_CONTRIBUYENTE);
        gDatRec.setiTiOpe(TiTiOpe.B2C);
        gDatRec.setcPaisRec(PaisType.PRY);

        if(innominado)
            gDatRec.setiTipIDRec(TiTipDocRec.INNOMINADO);
        else {
            gDatRec.setiTipIDRec(TiTipDocRec.CEDULA_PARAGUAYA);
            gDatRec.setdNumIDRec("4184256");
            gDatRec.setdNomRec("Gabriel Gomez");

        }

        dDatGralOpe.setgDatRec(gDatRec);
        DE.setgDatGralOpe(dDatGralOpe);

        // Grupo E: Campos específicos por tipo de Documento Electrónico
        TgDtipDE gDtipDE = new TgDtipDE();

        // E1: Campos que componen la Factura Electrónica
        TgCamFE gCamFE = new TgCamFE();
        gCamFE.setiIndPres(TiIndPres.OPERACION_ELECTRONICA);
        gDtipDE.setgCamFE(gCamFE);

        // E7: Campos que describen la condición de la operación
        TgCamCond gCamCond = new TgCamCond();
        gCamCond.setiCondOpe(TiCondOpe.CONTADO);

        TgPaConEIni gPaConEIni = new TgPaConEIni();
        gPaConEIni.setiTiPago(TiTiPago.EFECTIVO);
        gPaConEIni.setdDesTiPag("Efectivo");
        gPaConEIni.setdMonTiPag(BigDecimal.valueOf(1100000));
        gPaConEIni.setcMoneTiPag(CMondT.PYG);

        gCamCond.setgPaConEIniList(Collections.singletonList(gPaConEIni));
        gDtipDE.setgCamCond(gCamCond);

        // E8: Campos que describen los items de la operación
        List<TgCamItem> gCamItemList = new ArrayList<>();

        TgCamItem gCamItem = new TgCamItem();

        gCamItem.setdCodInt("001");
        gCamItem.setdDesProSer("Servicios Personales Prestados");
        gCamItem.setcUniMed(TcUniMed.UNI);
        gCamItem.setdCantProSer(BigDecimal.valueOf(1));

        TgValorItem gValorItem = new TgValorItem();
        gValorItem.setdPUniProSer(BigDecimal.valueOf(1000000));

        TgValorRestaItem gValorRestaItem = new TgValorRestaItem();
        gValorItem.setgValorRestaItem(gValorRestaItem);
        gCamItem.setgValorItem(gValorItem);

        TgCamIVA gCamIVA = new TgCamIVA();
        gCamIVA.setiAfecIVA(TiAfecIVA.GRAVADO);
        gCamIVA.setdPropIVA(BigDecimal.valueOf(100));
        gCamIVA.setdTasaIVA(BigDecimal.valueOf(10));

        gCamItem.setgCamIVA(gCamIVA);

        gCamItemList.add(gCamItem);

        gDtipDE.setgCamItemList(gCamItemList);
        DE.setgDtipDE(gDtipDE);

        // Grupo F: Campos que describen los subtotales y totales de la transacción documentada
        DE.setgTotSub(new TgTotSub());

        return DE;
    }


    /**
     * Método para generar un Documento Electrónico y a su vez, generar el archivo xml correspondiente.
     *
     * @param rutaArchivo La ruta donde se guardará el archivo
     * @param dNumDoc     El Número de Documento para su generación
     * @return Un Documento Electrónico creado, y se genera el archivo xml correspondiente.
     * @throws SifenException Una cadena formateada con el resultado de la Recepción y Consulta de Lote de DE realizadas (en el caso
     *                        de la última, sólo si se pudo realizar la consulta)
     */
    private static DocumentoElectronico generarDE(Path rutaArchivo, String dNumDoc, boolean innominado, GenerationCtx generationCtx) throws SifenException {

        DocumentoElectronico DE = crearDE(dNumDoc, innominado);
        try {
            Files.write(rutaArchivo, DE.generarXml(generationCtx).getBytes());
            System.out.println("Archivo creado");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return DE;
    }

    /**
     * Método que carga o genera un Documento Electrónico con el Numero de Documento recibido.
     * <p>
     * Este método verífica si existe un archivo DE*******.xml en el directorio ../ donde * representa
     * los dígitos de dNumDoc. Si existe, lo carga y lo retorna como DE.
     * <p>
     * Si no existe el archivo, genera el DE y guarda su xml en el directorio especificado.
     *
     * @param dNumDoc Número de Documento de la factura, debe ser una cadena de 7 dígitos.
     * @return Un Documento Electrónico generado o cargado.
     * @throws SifenException Una cadena formateada con el resultado de la Recepción y Consulta de Lote de DE realizadas (en el caso
     *                        de la última, sólo si se pudo realizar la consulta)
     */
    public static DocumentoElectronico cargarDE(String dNumDoc, boolean innominado, GenerationCtx generationCtx) throws SifenException {
        Path rutaArchivo = Paths.get(String.format("../DE%s.xml", dNumDoc));
        DocumentoElectronico DE = null;
        if (Files.exists(rutaArchivo)) {
            try {
                String xml = new String(Files.readAllBytes(rutaArchivo));
                System.out.println("Factura cargada de archivo");
                DE = new DocumentoElectronico(xml);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Creando factura\n");
            DE = generarDE(rutaArchivo, dNumDoc, innominado, generationCtx);
        }
        return DE;
    }

    /**
     * Método que crea un Evento de Anulación con el CDC de un DTE.
     *
     * @param cdc El CDC de un DTE a Anular.
     * @return Un Evento de Anulación con el CDC de un DTE.
     */
    public static EventosDE crearEventoAnulacion(String cdc) {
        LocalDateTime currentDate = LocalDateTime.now();

        TrGeVeCan rGeVeCan = new TrGeVeCan();
        rGeVeCan.setId(cdc);
        rGeVeCan.setmOtEve("Prueba de cancelación de documento electrónico");

        TgGroupTiEvt gGroupTiEvt = new TgGroupTiEvt();
        gGroupTiEvt.setrGeVeCan(rGeVeCan);

        TrGesEve rGesEve = new TrGesEve();
        rGesEve.setId("1");
        rGesEve.setdFecFirma(currentDate);
        rGesEve.setgGroupTiEvt(gGroupTiEvt);

        EventosDE eventosDE = new EventosDE();
        eventosDE.setrGesEveList(Collections.singletonList(rGesEve));

        return eventosDE;
    }

    /**
     * Método que crea un Evento de Nominacion con el CDC de un DTE.
     *
     * @param cdc El CDC de un DTE a Nominar.
     * @return Un Evento de Nominacion con el CDC de un DTE.
     */
    public static EventosDE crearEventoNominacion(String cdc) {
        LocalDateTime currentDate = LocalDateTime.now();

        TrGEveNom rGEveNom = new TrGEveNom();
        rGEveNom.setId(cdc);
        rGEveNom.setmOtEve("Prueba de nominación de documento electrónico");
        rGEveNom.setiNatRec(TiNatRec.NO_CONTRIBUYENTE);
        rGEveNom.setcPaisRec(PaisType.PRY);
        rGEveNom.setiTipIDRec(TiTipDocRec.CEDULA_PARAGUAYA);
        rGEveNom.setdNumIDRec("4184256");
        rGEveNom.setdNomRec("Gabriel Gomez");

        TgGroupTiEvt gGroupTiEvt = new TgGroupTiEvt();
        gGroupTiEvt.setrGEveNom(rGEveNom);

        TrGesEve rGesEve = new TrGesEve();
        rGesEve.setId("1");
        rGesEve.setdFecFirma(currentDate);
        rGesEve.setgGroupTiEvt(gGroupTiEvt);

        EventosDE eventosDE = new EventosDE();
        eventosDE.setrGesEveList(Collections.singletonList(rGesEve));

        return eventosDE;
    }

}
