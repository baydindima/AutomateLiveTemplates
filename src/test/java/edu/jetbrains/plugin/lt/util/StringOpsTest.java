package edu.jetbrains.plugin.lt.util;

import org.junit.Assert;
import org.junit.Test;

public class StringOpsTest {

    @Test
    public void testRemoveInsipidSequences1() throws Exception {
        Assert.assertEquals("_", StringOps.removeInsipidSequences("_(_(_<_[]>[][]{}))"));
    }

    @Test
    public void testRemoveInsipidSequences2() throws Exception {
        Assert.assertEquals("_(f(_))", StringOps.removeInsipidSequences("_(f(_<_[]>[][]{}))"));
    }

    @Test
    public void testRemoveInsipidSequences3() throws Exception {
        Assert.assertEquals("_", StringOps.removeInsipidSequences("_(_(_<_[]>[][]{_ _    _\n })   _   )"));
    }

    @Test
    public void testRemoveInsipidSequences4() throws Exception {
        Assert.assertEquals("q(_)", StringOps.removeInsipidSequences("q(_(_<_[]>[][]{}))"));
    }

    @Test
    public void testRemoveInsipidSequences5() throws Exception {
        Assert.assertEquals("a._", StringOps.removeInsipidSequences("a._()"));
    }

    @Test
    public void testRemoveInsipidSequences6() throws Exception {
        Assert.assertEquals("_", StringOps.removeInsipidSequences("_._()"));
    }

    @Test
    public void testRemoveInsipidSequences7() throws Exception {
        Assert.assertEquals("_", StringOps.removeInsipidSequences("_()._"));
    }

    @Test
    public void testRemoveInsipidSequences8() throws Exception {
        Assert.assertEquals("_.z", StringOps.removeInsipidSequences("_().z"));
    }

    @Test
    public void testRemoveInsipidSequences9() throws Exception {
        Assert.assertEquals("_(1)._", StringOps.removeInsipidSequences("_(1)._"));
    }

    @Test
    public void testRemoveInsipidSequences10() throws Exception {
        Assert.assertEquals("_(1)._", StringOps.removeInsipidSequences("_(_(_<_[]>[][]{_ _    _\n  })   _   )(1)._._( _._ \n  )._(_(_<_[]>[][]{_ _    _\n  })   _   )"));
    }

    @Test
    public void testRemoveInsipidSequences11() throws Exception {
        Assert.assertEquals("_", StringOps.removeInsipidSequences("_   ____  __  \n_\n_____"));
    }

    @Test
    public void testRemoveInsipidSequences12() throws Exception {
        Assert.assertEquals("_", StringOps.removeInsipidSequences("_(_()._{})._"));
    }

    @Test
    public void testIsRightSequence1() throws Exception {
        Assert.assertTrue(StringOps.isRightCode("(aba)(()cab())a"));
    }

    @Test
    public void testIsRightSequence2() throws Exception {
        Assert.assertTrue(StringOps.isRightCode("la<{([la])}>la"));
    }

    @Test
    public void testIsRightSequence3() throws Exception {
        Assert.assertFalse(StringOps.isRightCode(")("));
    }

    @Test
    public void testIsRightSequence4() throws Exception {
        Assert.assertFalse(StringOps.isRightCode("()(()<)"));
    }
}