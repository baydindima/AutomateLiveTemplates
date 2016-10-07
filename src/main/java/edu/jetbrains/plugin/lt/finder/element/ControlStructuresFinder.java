package edu.jetbrains.plugin.lt.finder.element;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import edu.jetbrains.plugin.lt.finder.Parameters;
import edu.jetbrains.plugin.lt.finder.Template;
import edu.jetbrains.plugin.lt.util.Recursive;
import edu.jetbrains.plugin.lt.util.StringOps;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static edu.jetbrains.plugin.lt.finder.element.ControlStructuresFinderParameters.Name.*;

public class ControlStructuresFinder {
    private List<PsiFile> psiFiles;
    private int matchesMinimum;
    private int lengthMinimum;
    private int templatesToShow;

    public ControlStructuresFinder(List<PsiFile> psiFiles, Parameters parameters) {
        this.psiFiles = psiFiles;
        matchesMinimum = parameters.getParameter(MATCHES_MINIMUM);
        lengthMinimum = parameters.getParameter(LENGTH_MINIMUM);
        templatesToShow = parameters.getParameter(TEMPLATES_TO_SHOW);
    }

    public List<Template> analyze() {
        Map<FileType, List<PsiFile>> psiFilesMap = new HashMap<>();
        psiFiles.forEach(psiFile -> {
            List<PsiFile> typedPsiFiles = psiFilesMap.get(psiFile.getFileType());
            if (typedPsiFiles == null) {
                typedPsiFiles = new LinkedList<>();
                psiFilesMap.put(psiFile.getFileType(), typedPsiFiles);
            }
            typedPsiFiles.add(psiFile);
        });
        psiFilesMap.remove(PlainTextFileType.INSTANCE);
        psiFilesMap.remove(UnknownFileType.INSTANCE);

        Map<Template, Integer> templateMap = new HashMap<>();
        Predicate<String> possibleTemplatePredicate = s ->
                s.length() >= lengthMinimum &&
                        s.matches(".*[a-zA-Z].*") &&
                        StringOps.isRightCode(s);

        psiFilesMap.forEach((fileType, typedPsiFiles) -> {
            Map<Pair<IElementType, Integer>, List<ASTNode>> astNodesMap = new HashMap<>();

            Recursive<Consumer<ASTNode>> recursiveAstNodeConsumer = new Recursive<>();
            recursiveAstNodeConsumer.function = node -> {
                List<ASTNode> children = Arrays.stream(node.getChildren(null)).
                        filter(child -> !child.getText().matches("\\s*")).
                        collect(Collectors.toList());
                if (children.size() != 0) {
                    Pair<IElementType, Integer> key = new Pair<>(node.getElementType(), children.size());
                    List<ASTNode> typedAstNodes = astNodesMap.get(key);
                    if (typedAstNodes == null) {
                        typedAstNodes = new LinkedList<>();
                        astNodesMap.put(key, typedAstNodes);
                    }
                    typedAstNodes.add(node);
                    children.forEach(recursiveAstNodeConsumer.function::accept);
                }
            };

            typedPsiFiles.forEach(psiFile -> {
                for (PsiElement element :
                        psiFile.getChildren()) {
                    recursiveAstNodeConsumer.function.accept(element.getNode());
                }
            });

            astNodesMap.forEach((elementTypeChildrenSizePair, typedAstNodes) -> {
                if (typedAstNodes.size() < matchesMinimum) return;

                int childrenSize = elementTypeChildrenSizePair.getSecond();
                ElementValue[] elementValues = new ElementValue[childrenSize];
                for (int i = 0; i < childrenSize; ++i) {
                    elementValues[i] = new ElementValue();
                }

                typedAstNodes.forEach(node -> {
                    List<ASTNode> children = Arrays.stream(node.getChildren(null)).
                            filter(child -> !child.getText().matches("\\s*")).
                            collect(Collectors.toList());
                    for (int i = 0; i < childrenSize; ++i) {
                        elementValues[i].add(children.get(i).getText());
                    }
                });

                ASTNode sample = typedAstNodes.get(0);
                ASTNode[] sampleChildren = sample.getChildren(null);
                int i = 0;
                List<String> tokens = new LinkedList<>();
                StringBuilder builder = new StringBuilder();
                for (ASTNode sampleChild : sampleChildren) {
                    String text = sampleChild.getText();
                    if (text.matches("\\s*")) {
                        builder.append(text);
                        continue;
                    }
                    boolean isPlaceholder = elementValues[i].isPlaceholder();
                    String value = elementValues[i].get();
                    builder.append(value);
                    if (!isPlaceholder) tokens.add(value);
                    ++i;
                }
                if (possibleTemplatePredicate.test(tokens.stream().collect(Collectors.joining()))) {
                    String body = builder.toString();
                    body = body.replaceAll("#_#(#_#|\\s)*#_#", "#_#");
                    Template template = new Template(body, typedAstNodes.size(), tokens, fileType);
                    templateMap.put(template, templateMap.getOrDefault(template, 0) + typedAstNodes.size());
                }
            });
        });

        List<Template> templateList = new ArrayList<>();
        templateMap.forEach((template, occurrences) -> {
            template.setOccurrences(occurrences);
            templateList.add(template);
        });
        templateList.sort((o1, o2) -> Integer.compare(o2.getOccurrences(), o1.getOccurrences()));

        return templateList.stream().limit(templatesToShow).collect(Collectors.toList());
    }
}
