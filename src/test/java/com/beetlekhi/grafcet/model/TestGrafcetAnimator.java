package com.beetlekhi.grafcet.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import com.beetlekhi.animator.GrafcetAnimator;

public class TestGrafcetAnimator {

    Grafcet grafcet;

    /*
      _____
     /     \
     |     |
     |   [[1]]
     |     |
     |    -+- 1
     |     |
     |    ===============
     |     |     |     |
     |    [2]   [3]   [4]
    /|\    |\    |     |
     |     | \   |     |
     |     |  \  |     |
     |     |   \ |     |
     |     |   ==========
     |    -+-2     |
     |     |       |
     |     |      -+- 3
     \_____/
      
     */

    @Before
    public void preloadGrafcet() throws JAXBException {
        File file = new File(getClass().getClassLoader().getResource("grafcetAnimatorTest.xml").getFile());
        grafcet = GrafcetUtils.readGrafcetFromXML(file);
    }

    @Test
    public void testGetIntegerVariable() {
        GrafcetAnimator animator = new GrafcetAnimator(grafcet);
        Integer value = animator.getIntegerVariable("x");
        assertNotNull(value);
        assertEquals(5, (int) value);
    }

    @Test
    public void testGetBooleanVariable() {
        GrafcetAnimator animator = new GrafcetAnimator(grafcet);
        Boolean value = animator.getBooleanVariable("toto");
        assertNotNull(value);
        assertEquals(Boolean.TRUE, value);
    }

    @Test
    public void testIsActive() {
        GrafcetAnimator animator = new GrafcetAnimator(grafcet);
        assertTrue(animator.isActive(1));
        assertFalse(animator.isActive(2));
        assertFalse(animator.isActive(3));
        assertFalse(animator.isActive(4));
    }

    @Test
    public void testIsEnabled() {
        GrafcetAnimator animator = new GrafcetAnimator(grafcet);
        assertTrue(animator.isEnabled(1));
        assertFalse(animator.isEnabled(2));
        assertFalse(animator.isEnabled(3));
    }

    @Test
    public void testAnimate() {
        GrafcetAnimator animator = new GrafcetAnimator(grafcet);
        animator.animate();
        assertFalse(animator.isActive(1));
        assertTrue(animator.isActive(2));
        assertTrue(animator.isActive(3));
        assertTrue(animator.isActive(4));
    }

    @Test
    public void testConditions() {
        GrafcetAnimator animator = new GrafcetAnimator(grafcet);
        animator.animate();
        animator.animate();
        assertTrue(animator.isActive(1));
        assertFalse(animator.isActive(2));
        assertFalse(animator.isActive(3));
        assertFalse(animator.isActive(4));
    }
}
