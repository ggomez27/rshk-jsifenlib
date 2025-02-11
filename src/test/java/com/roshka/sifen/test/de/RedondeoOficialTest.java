package com.roshka.sifen.test.de;

import com.roshka.sifen.core.fields.util.RedondeoUtil;
import com.roshka.sifen.core.types.CMondT;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class RedondeoOficialTest {

    @Test
    public void testRedondeoGuaranies00() {
        BigDecimal ret = RedondeoUtil.redondeoOficialSET(CMondT.PYG, BigDecimal.valueOf(107437));
        Assert.assertEquals(BigDecimal.valueOf(107400), ret);
    }

    @Test
    public void testRedondeoGuaranies01() {
        BigDecimal ret = RedondeoUtil.redondeoOficialSET(CMondT.PYG, BigDecimal.valueOf(47789));
        Assert.assertEquals(BigDecimal.valueOf(47750), ret);
    }

    @Test
    public void testRedondeoGuaranies02() {
        BigDecimal ret = RedondeoUtil.redondeoOficialSET(CMondT.PYG, BigDecimal.valueOf(99999));
        Assert.assertEquals(BigDecimal.valueOf(99950), ret);
    }

    @Test
    public void testRedondeoDolares00() {
        BigDecimal ret = RedondeoUtil.redondeoOficialSET(CMondT.USD, BigDecimal.valueOf(1000));
        Assert.assertEquals(BigDecimal.valueOf(1000.0), ret);
    }

    @Test
    public void testRedondeoDolares01() {
        BigDecimal ret = RedondeoUtil.redondeoOficialSET(CMondT.USD, BigDecimal.valueOf(1000.23));
        Assert.assertEquals(BigDecimal.valueOf(1000.0), ret);
    }

    @Test
    public void testRedondeoDolares02() {
        BigDecimal ret = RedondeoUtil.redondeoOficialSET(CMondT.USD, BigDecimal.valueOf(1000.26));
        Assert.assertEquals(BigDecimal.valueOf(1000.50), ret);
    }

    @Test
    public void testRedondeoDolares03() {
        BigDecimal ret = RedondeoUtil.redondeoOficialSET(CMondT.USD, BigDecimal.valueOf(1000.56));
        Assert.assertEquals(BigDecimal.valueOf(1000.50), ret);
    }

    @Test
    public void testRedondeoDolares04() {
        BigDecimal ret = RedondeoUtil.redondeoOficialSET(CMondT.USD, BigDecimal.valueOf(1000.76));
        Assert.assertEquals(BigDecimal.valueOf(1001.0), ret);
    }



}
