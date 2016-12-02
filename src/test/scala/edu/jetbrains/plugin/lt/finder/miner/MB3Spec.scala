package edu.jetbrains.plugin.lt.finder.miner

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase

/**
  * Test for MB3 algorithm
  */
class MB3Spec extends LightCodeInsightFixtureTestCase {

  //  val classText1 =
  //    """
  //      package edu.jetbrains.plugin.lt.finder.tree;
  //      
  //      
  //      import java.util.ArrayList;
  //      import java.util.ArrayList;
  //      import java.util.ArrayList;
  //      import java.util.ArrayList;
  //      import java.util.ArrayList;
  //      import java.util.ArrayList;
  //      import java.util.ArrayList;
  //      import java.util.List;
  //      
  //      class Test {
  //      
  //          public static void main(String[] args) {
  //              List<Integer> ints = new ArrayList<>();
  //              for (String arg : args) {
  //                  ints.add(Integer.valueOf(arg));
  //              }
  //      
  //              List<Double> doubles = new ArrayList<>();
  //              for (String arg : args) {
  //                  doubles.add(Double.valueOf(arg));
  //              }
  //      
  //              List<Float> floats = new ArrayList<>();
  //              for (String arg : args) {
  //                  floats.add(Float.valueOf(arg));
  //              }
  //      
  //              List<Short> shorts = new ArrayList<>();
  //              for (String arg : args) {
  //                  shorts.add(Short.valueOf(arg));
  //              }
  //          }
  //      }
  //       """
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

  def testMB3(): Unit = {
    val nodes = buildJavaPsiFiles(classText2).map(_.getNode)
    new MB3().getTemplates(nodes, JavaFileTypeTemplateFilter)
  }

  private def buildJavaPsiFiles(texts: String*): Seq[PsiFile] = {
    texts.map(
      createLightFile(JavaFileType.INSTANCE, _)
    )
  }

}
