package edu.jetbrains.plugin.lt;

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

public class TemplatesFindAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) return;

//        TemplateSettings.getInstance().addTemplate(
//                new TemplateImpl("test template key", "test template string", TemplateSettings.USER_GROUP_NAME)
//        );

        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        VirtualFile[] sourceRoots = projectRootManager.getContentSourceRoots();

        List<PsiFile> psiFiles = new LinkedList<>();

        Recursive<Consumer<PsiDirectory>> psiDirectoryConsumer = new Recursive<>();
        psiDirectoryConsumer.function = psiDirectory -> {
            psiFiles.addAll(Arrays.asList(psiDirectory.getFiles()));
            for (PsiDirectory subdirectory : psiDirectory.getSubdirectories())
                psiDirectoryConsumer.function.accept(subdirectory);
        };

        PsiManager psiManager = PsiManager.getInstance(project);
        Arrays.stream(sourceRoots)
                .map(psiManager::findDirectory)
                .forEach(psiDirectoryConsumer.function::accept);

        Modes.Name mode = ModesDialog.showDialogAndGetMode(project);
        if (mode == null) return;

        Parameters parameters = ParametersDialog.showDialogAndGetParameters(project, mode);
        if (parameters == null) return;

        List<Template> templates;
        switch (mode) {
            case CONTROL_STRUCTURES_FINDER:
                templates = new ControlStructuresFinder(psiFiles, parameters).analyze();
                break;
            case TEMPLATES_FINDER:
                templates = new TreeTemplatesFinder(psiFiles, parameters).analyze();
                break;
            default:
                return;
        }
        if (templates.size() != 0) {
            new TemplatesDialog(project, templates).show();
        } else {
            new NoTemplatesDialog(project).show();
        }
    }
}
