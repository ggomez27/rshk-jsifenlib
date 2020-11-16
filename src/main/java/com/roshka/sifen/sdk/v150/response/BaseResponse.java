package com.roshka.sifen.sdk.v150.response;

import com.roshka.sifen.exceptions.SifenException;
import com.roshka.sifen.model.SifenObjectBase;
import com.roshka.sifen.util.ResponseUtil;
import org.w3c.dom.Node;

public abstract class BaseResponse extends SifenObjectBase {
    public static final String NOMBRE_ELEMENTO_DCODRES = "dCodRes";
    public static final String NOMBRE_ELEMENTO_DMSGRES = "dMsgRes";

    private String dCodRes;
    private String dMsgRes;

    @Override
    public void setValueFromChildNode(Node value) throws SifenException {
        if (value.getLocalName().equals(NOMBRE_ELEMENTO_DCODRES)) {
            dCodRes = ResponseUtil.getTextValue(value);
        } else if (value.getLocalName().equals(NOMBRE_ELEMENTO_DMSGRES)) {
            dMsgRes = ResponseUtil.getTextValue(value);
        }
    }

    public String getdCodRes() {
        return dCodRes;
    }

    public String getdMsgRes() {
        return dMsgRes;
    }
}
