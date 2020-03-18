package org.smartregister.chw.core.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TallyTest {
    private Tally tally = new Tally();
    private Hia2Indicator hia2Indicator = new Hia2Indicator();
    private ReportHia2Indicator reportHia2Indicator = new ReportHia2Indicator();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAndSetIndicator() {
        tally.setIndicator(hia2Indicator);
        Assert.assertEquals(tally.getIndicator(), hia2Indicator);
    }

    @Test
    public void testGetAndSetId() {
        long identifier = Mockito.anyLong();
        tally.setId(identifier);
        Assert.assertEquals(tally.getId(), identifier);
    }

    @Test
    public void testGetAndSetValue() {
        String value = Mockito.anyString();
        tally.setValue(value);
        Assert.assertEquals(tally.getValue(), value);
    }

    @Test
    public void getReportHia2Indicator() {
        reportHia2Indicator.setValue(Mockito.anyString());
        reportHia2Indicator.setHia2Indicator(hia2Indicator);
        Assert.assertEquals(reportHia2Indicator.getValue(), Mockito.anyString());
        Assert.assertTrue(tally.getReportHia2Indicator() != null);
    }
}