package edu.jetbrains.plugin.lt.tree;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import edu.jetbrains.plugin.lt.finder.Parameters;
import edu.jetbrains.plugin.lt.finder.Template;
import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinder;
import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TreeTemplatesFinderTest extends LightCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "./src/test/resources";
    }

    private List<PsiFile> getPsiFiles(String... names) throws Exception {
        List<PsiFile> psiFiles = new LinkedList<>();
        for (String name : names) {
            String testPath = getTestDataPath() + "/" + name + ".java";
            List<String> lines = Files.readAllLines(Paths.get(testPath));
            String text = lines.stream().collect(Collectors.joining("\n"));
            PsiFile psiFile = createLightFile(JavaFileType.INSTANCE, text);
            psiFiles.add(psiFile);
        }
        return psiFiles;
    }

    private List<String> getBodies(List<Template> simpleTemplates) {
        return simpleTemplates
                .parallelStream()
                .map(simpleTemplate -> simpleTemplate.getBody().replaceAll("\\s+", " "))
                .collect(Collectors.toList());
    }

    public void test1() throws Exception {
        List<PsiFile> psiFiles = getPsiFiles("T1");
        Parameters parameters = new TreeTemplatesFinderParameters();
        List<Template> templatesList = new TreeTemplatesFinder(psiFiles, parameters).analyze();
        assertEquals(1, templatesList.size());
        List<String> bodiesList = getBodies(templatesList);
        assertContainsElements(
                bodiesList,
                "Rectangle #_# = new Rectangle(0, 0, #_#, #_#);"
        );
    }

    public void test2() throws Exception {
        List<PsiFile> psiFiles = getPsiFiles("T2");
        Parameters parameters = new TreeTemplatesFinderParameters();
        List<Template> templatesList = new TreeTemplatesFinder(psiFiles, parameters).analyze();
        assertEquals(1, templatesList.size());
        List<String> bodiesList = getBodies(templatesList);
        assertContainsElements(
                bodiesList,
                "Map<String, #_#> #_# = new HashMap<>(69, .69f);"
        );
    }

    public void test3() throws Exception {
        List<PsiFile> psiFiles = getPsiFiles("T3");
        Parameters parameters = new TreeTemplatesFinderParameters();
        List<Template> templatesList = new TreeTemplatesFinder(psiFiles, parameters).analyze();
        assertEquals(1, templatesList.size());
        List<String> bodiesList = getBodies(templatesList);
        assertContainsElements(
                bodiesList,
                "static { " +
                        "#_# = new HashMap<>(); " +
                        "#_#.put(\"default\", 0); " +
                        "}"
        );
    }
}