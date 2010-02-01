package com.plexobject.commons.metrics;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TimerTest {

    @Before
    public void setUp() throws Exception {
        final Metric metric = Metric.getMetric("test");
        for (int i = 0; i <= Metric.MINIMUM_METRICS; i++) {
            metric.finishedTimer(0, 0);
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testStop() throws InterruptedException {
        final Metric metric = Metric.getMetric("test");
        final Timer timer = metric.newTimer();
        Thread.sleep(10);
        timer.stop();
        Assert.assertEquals(1, metric.getTotalCalls());
        Assert.assertTrue(metric.getAverageDurationInNanoSecs() > 1000000);

    }

    @Test
    public final void testLapse() throws InterruptedException {
        final Metric metric = Metric.getMetric("test");
        final Timer timer = metric.newTimer();
        for (int i = 0; i < 3; i++) {
            Thread.sleep(10);
            timer.lapse();
        }
        timer.stop();

        Assert.assertEquals(5, metric.getTotalCalls());
        Assert.assertTrue(metric.getAverageDurationInNanoSecs() > 1000000);

    }

}
