package edu.jetbrains.plugin.lt.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static edu.jetbrains.plugin.lt.util.ListOps.hasSubSequence;
import static edu.jetbrains.plugin.lt.util.ListOps.removeInsipidSequences;
import static org.junit.Assert.*;

public class ListOpsTest {

    @Test
    public void testHasSubSequence1() throws Exception {
        List<String> sequence = Arrays.asList("a", "b", "c", "d", "e", "f");
        List<String> subSequence = Arrays.asList("b", "d", "e");
        assertTrue(hasSubSequence(sequence, subSequence));
    }

    @Test
    public void testHasSubSequence2() throws Exception {
        List<String> sequence = Arrays.asList("a", "b", "c", "d", "e", "f");
        List<String> subSequence = Arrays.asList("b", "d", "g");
        assertFalse(hasSubSequence(sequence, subSequence));
    }

    @Test
    public void testRemoveInsipidSequences1() throws Exception {
        List<String> lst = Arrays.asList("_", "(", "_", "(", "_", "<", "_", "[", "]", ">", "[", "]", "[", "]", "{", "}", ")", ")");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(1, res.size());
        assertEquals("_", res.get(0));
    }

    @Test
    public void testRemoveInsipidSequences2() throws Exception {
        List<String> lst = Arrays.asList("_", "(", "foo", "(", "_", "<", "_", "[", "]", ">", "[", "]", "[", "]", "{", "}", ")", ")");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(7, res.size());
        assertEquals("_", res.get(0));
        assertEquals("(", res.get(1));
        assertEquals("foo", res.get(2));
        assertEquals("(", res.get(3));
        assertEquals("_", res.get(4));
        assertEquals(")", res.get(5));
        assertEquals(")", res.get(6));
    }

    @Test
    public void testRemoveInsipidSequences3() throws Exception {
        List<String> lst = Arrays.asList("qqq", "(", "_", "(", "_", "<", "_", "[", "]", ">", "[", "]", "[", "]", "{", "}", ")", ")");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(4, res.size());
        assertEquals("qqq", res.get(0));
        assertEquals("(", res.get(1));
        assertEquals("_", res.get(2));
        assertEquals(")", res.get(3));
    }

    @Test
    public void testRemoveInsipidSequences4() throws Exception {
        List<String> lst = Arrays.asList("abc", ".", "_", "(", ")");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(3, res.size());
        assertEquals("abc", res.get(0));
        assertEquals(".", res.get(1));
        assertEquals("_", res.get(2));
    }

    @Test
    public void testRemoveInsipidSequences5() throws Exception {
        List<String> lst = Arrays.asList("_", ".", "_", "(", ")");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(1, res.size());
        assertEquals("_", res.get(0));
    }

    @Test
    public void testRemoveInsipidSequences6() throws Exception {
        List<String> lst = Arrays.asList("_", "(", ")", ".", "_");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(1, res.size());
        assertEquals("_", res.get(0));
    }

    @Test
    public void testRemoveInsipidSequences7() throws Exception {
        List<String> lst = Arrays.asList("_", "(", ")", ".", "zzz");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(3, res.size());
        assertEquals("_", res.get(0));
        assertEquals(".", res.get(1));
        assertEquals("zzz", res.get(2));
    }

    @Test
    public void testRemoveInsipidSequences8() throws Exception {
        List<String> lst = Arrays.asList("_", "(", "115", ")", ".", "_");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(lst, res);
    }

    @Test
    public void testRemoveInsipidSequences9() throws Exception {
        List<String> lst = Arrays.asList("_", " ", " ", "\n  ", "    ", "_", " ", "_", "_", " \n \n\n   ", "_");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(1, res.size());
        assertEquals("_", res.get(0));
    }

    @Test
    public void testRemoveInsipidSequences10() throws Exception {
        List<String> lst = Arrays.asList("foo", "(", "_", " ", " ", "\n  ", "    ", "_", " ", "_", "_", " \n \n\n   ", "_", " ", "+", " ", "1", ")");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(8, res.size());
        assertEquals("foo", res.get(0));
        assertEquals("(", res.get(1));
        assertEquals("_", res.get(2));
        assertEquals(" ", res.get(3));
        assertEquals("+", res.get(4));
        assertEquals(" ", res.get(5));
        assertEquals("1", res.get(6));
        assertEquals(")", res.get(7));
    }

    @Test
    public void testRemoveInsipidSequences11() throws Exception {
        List<String> lst = Arrays.asList("_", "(", "_", "(", "_", ")", ".", "_", "{", "}", ")", ".", "_");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(1, res.size());
        assertEquals("_", res.get(0));
    }

    @Test
    public void testRemoveInsipidSequences12() throws Exception {
        List<String> lst = Arrays.asList("foo", " ", "_", " ", "_", " ", "bar");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(5, res.size());
        assertEquals("foo", res.get(0));
        assertEquals(" ", res.get(1));
        assertEquals("_", res.get(2));
        assertEquals(" ", res.get(3));
        assertEquals("bar", res.get(4));
    }

    @Test
    public void testRemoveInsipidSequences13() throws Exception {
        List<String> lst = Arrays.asList(" ", "", "p", " ", "", "q");
        List<String> res = removeInsipidSequences(lst);
        assertEquals(4, res.size());
        assertEquals(" ", res.get(0));
        assertEquals("p", res.get(1));
        assertEquals(" ", res.get(2));
        assertEquals("q", res.get(3));
    }
}