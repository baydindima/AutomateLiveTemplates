package edu.jetbrains.plugin.lt.finder.miner

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import org.scalatest.Matchers

/**
  * Test for MB3 algorithm
  */
class MB3Spec extends LightCodeInsightFixtureTestCase with Matchers {

  val classText1 =
    """
        package edu.jetbrains.plugin.lt.finder.tree;

        class Test {

            public static void main(String[] args) {
                 String.format("some string", "val", 1, 2, 4, new Object());
                 String.format("some string", 1, "val",  2, 4, new Object());
                 String.format("some string", "val", 1, new Object());
                 String.format("some string", "val", 1, 2, 4);
                 String.format("some string",  1, 2, 4, new Object());
                 String.format("some string", "val", 12, 4, new Object());
                 String.format("some string", "val", 1, new Object());
                 String.format("some string", "val", 1, 2, 4, new Object(), 5 ,6);
            }
        }
         """
  val classText2 =
  """
     import javafx.scene.shape.Rectangle;
     import com.intellij.openapi.actionSystem.AnAction;
     import com.intellij.openapi.actionSystem.AnActionEvent;
     import com.intellij.openapi.project.Project;
     import com.intellij.openapi.roots.ProjectRootManager;
     import com.intellij.openapi.vfs.VirtualFile;
     import com.intellij.psi.PsiDirectory;
     import com.intellij.psi.PsiFile;
     import com.intellij.psi.PsiManager;
     import edu.jetbrains.plugin.lt.finder.Modes;
     import edu.jetbrains.plugin.lt.finder.Parameters;
     import edu.jetbrains.plugin.lt.finder.Template;
     import edu.jetbrains.plugin.lt.finder.element.ControlStructuresFinder;
     import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinder;
     import edu.jetbrains.plugin.lt.ui.ModesDialog;
     import edu.jetbrains.plugin.lt.ui.NoTemplatesDialog;
     import edu.jetbrains.plugin.lt.ui.ParametersDialog;
     import edu.jetbrains.plugin.lt.ui.TemplatesDialog;
     import edu.jetbrains.plugin.lt.util.Recursive;

     import java.util.Arrays;
     import java.util.LinkedList;
     import java.util.List;
     import java.util.function.Consumer;
    
    public class T1 {
    }
    
    """

  def testMB3Parenthesis(): Unit = {
    val nodes = buildJavaPsiFiles(classText1).map(_.getNode)
    new MB3(new MinerConfiguration(minSupportCoefficient = 0.5),
      JavaFileTypeTemplateFilter,
      JavaTemplateProcessor).getTemplates(nodes)
  }

  def testMB3Imports(): Unit = {
    val nodes = buildJavaPsiFiles(classText2).map(_.getNode)
    new MB3(new MinerConfiguration(minSupportCoefficient = 0.5),
      JavaFileTypeTemplateFilter,
      JavaTemplateProcessor).getTemplates(nodes) should have size 0
  }

  private def buildJavaPsiFiles(texts: String*): Seq[PsiFile] = {
    texts.map(
      createLightFile(JavaFileType.INSTANCE, _)
    )
  }

}
