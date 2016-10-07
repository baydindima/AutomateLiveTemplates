package edu.jetbrains.plugin.lt.finder.tree.parameter;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import edu.jetbrains.plugin.lt.finder.Parameters;
import edu.jetbrains.plugin.lt.finder.Template;
import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinder;
import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name.LENGTH_MINIMUM;


/**
 * Created by Dmitriy Baidin.
 */
public class LengthMinimumTest extends LightCodeInsightFixtureTestCase {
    private static final String FILE_PATH =
            "./src/test/resources/edu/jetbrains/plugin/lt/finder/tree/parameter/LengthMinimumTestFile.java";


    private PsiFile getPsiFile() throws IOException {
        return createLightFile(JavaFileType.INSTANCE, String.valueOf(Files.readAllLines(Paths.get(FILE_PATH))));
    }

    public void test1() throws IOException {
        Parameters parameters = new TreeTemplatesFinderParameters();
        parameters.setParameter(LENGTH_MINIMUM, 100);
        List<Template> templatesList = new TreeTemplatesFinder(
                Collections.singletonList(getPsiFile()), parameters).analyze();
        assertTrue("List of templates should be empty", templatesList.isEmpty());
    }

    public void test2() throws IOException {
        Parameters parameters = new TreeTemplatesFinderParameters();
        parameters.setParameter(LENGTH_MINIMUM, 0);
        List<Template> templatesList = new TreeTemplatesFinder(
                Collections.singletonList(getPsiFile()), parameters).analyze();
        assertTrue("List of templates should not be empty", !templatesList.isEmpty());
    }

}
