package edu.jetbrains.plugin.lt.finder.tree;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import edu.jetbrains.plugin.lt.extensions.ep.FileTypeTemplateFilter;
import edu.jetbrains.plugin.lt.finder.Parameters;
import edu.jetbrains.plugin.lt.finder.Template;
import edu.jetbrains.plugin.lt.util.ListOps;
import edu.jetbrains.plugin.lt.util.Recursive;
import edu.jetbrains.plugin.lt.util.StringOps;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name.*;
import static edu.jetbrains.plugin.lt.util.ASTNodeOps.depth;
import static edu.jetbrains.plugin.lt.util.ASTNodeOps.nodes;

public class TreeTemplatesFinder {
    private List<PsiFile> psiFiles;
    private int lengthMinimum;
    private int lengthMaximum;
    private int nodesMinimum;
    private int depthMinimum;
    private int matchesMinimum;
    private int placeholdersMaximum;
    private int templatesToShow;

    public TreeTemplatesFinder(List<PsiFile> psiFiles, Parameters parameters) {
        this.psiFiles = psiFiles;
        lengthMinimum = parameters.getParameter(LENGTH_MINIMUM);
        lengthMaximum = parameters.getParameter(LENGTH_MAXIMUM);
        nodesMinimum = parameters.getParameter(NODES_MINIMUM);
        depthMinimum = parameters.getParameter(DEPTH_MINIMUM);
        matchesMinimum = parameters.getParameter(MATCHES_MINIMUM);
        placeholdersMaximum = parameters.getParameter(PLACEHOLDERS_MAXIMUM);
        templatesToShow = parameters.getParameter(TEMPLATES_TO_SHOW);
    }

    public List<Template> analyze() {
        FileTypeTemplateFilter[] filters = FileTypeTemplateFilter.EP_NAME.getExtensions();
        Predicate<String> defaultPredicate = s -> false;
        Map<FileType, Predicate<String>> notAnalyzePredicates = new HashMap<>();
        Map<FileType, Predicate<String>> notShowPredicates = new HashMap<>();

        // Загружаем все игнорируемые keywords
        for (FileTypeTemplateFilter filter :
                filters) {
            notAnalyzePredicates.put(filter.fileType(), StringOps.containsAnyPredicate(filter.keywordsNotAnalyze()));
            notShowPredicates.put(filter.fileType(), StringOps.containsAnyPredicate(filter.keywordsNotShow()));
        }

        // Строим связку тип файла -> файлы
        final double[] i = {0};
        Map<FileType, List<PsiFile>> psiFilesMap = new HashMap<>();
        psiFiles.forEach(psiFile -> {
            List<PsiFile> typedPsiFiles = psiFilesMap.computeIfAbsent(psiFile.getFileType(), k -> new LinkedList<>());
            typedPsiFiles.add(psiFile);
            if (++i[0] % 10 == 0) {
                System.out.println(i[0] / psiFiles.size());
            }
        });

        // Удаляем файлы с обычным текстом и неизвестным форматом
        psiFilesMap.remove(PlainTextFileType.INSTANCE);
        psiFilesMap.remove(UnknownFileType.INSTANCE);

        Predicate<ASTNode> possibleTemplatePredicate = node ->
                node.getTextLength() >= lengthMinimum &&
                        nodes(node) >= nodesMinimum &&
                        depth(node) >= depthMinimum;

        Predicate<Template> templatePredicate = template ->
                template.getOccurrences() != Integer.MAX_VALUE &&
                        template.getBody().length() >= lengthMinimum &&
                        template.getTokens().size() >= nodesMinimum &&
                        StringUtils.countMatches(template.getBody(), "#_#") <= placeholdersMaximum;

        Set<Template> templateSet = new HashSet<>();

        // Для каждого типа файла
        final double[] fileTypeNumber = {0.};
        psiFilesMap.forEach((fileType, typedPsiFiles) -> {

            Predicate<String> fileTypeNotAnalyzePredicate = notAnalyzePredicates.getOrDefault(fileType, defaultPredicate);
            Predicate<String> fileTypeNotShowPredicate = notShowPredicates.getOrDefault(fileType, defaultPredicate);

            Map<IElementType, List<ASTNode>> astNodesMap = new HashMap<>();

            // Строим рекурсивную функцию, для каждой ноды в аст-дереве
            // Если параметры ноды подходят, добавляем ее в мапу тип элемента -> ноды
            // вызываемся рекурсивано от детей
            // Если у ноды слишком длинный текст, просто вызываемся от детей
            Recursive<Consumer<ASTNode>> recursiveAstNodeConsumer = new Recursive<>();
            recursiveAstNodeConsumer.function = node -> {
                if (node.getTextLength() >= lengthMaximum) {
                    for (ASTNode child :
                            node.getChildren(null)) {
                        recursiveAstNodeConsumer.function.accept(child);
                    }
                } else if (possibleTemplatePredicate.test(node) &&
                        !fileTypeNotAnalyzePredicate.test(node.getText())) {
                    List<ASTNode> typedAstNodes = astNodesMap.computeIfAbsent(node.getElementType(), k -> new LinkedList<>());
                    typedAstNodes.add(node);
                    for (ASTNode child :
                            node.getChildren(null)) {
                        recursiveAstNodeConsumer.function.accept(child);
                    }
                }
            };

            // Вызываем recursiveAstNodeConsumer для каждого файла, кроме файлов содержащих фразу "GUI Designer"?
            typedPsiFiles.forEach(psiFile -> {
                if (!psiFile.getText().contains("GUI Designer")) {
                    for (PsiElement element :
                            psiFile.getChildren()) {
                        recursiveAstNodeConsumer.function.accept(element.getNode());
                    }
                }
            });


            // Для каждого типа элемента создаем SimilarityTree
            // Добавляем в это дерево каждую ноду с данным типом
            final double[] elementTypeNumber = {0.};
            astNodesMap.forEach((elementType, typedAstNodes) -> {
                SimilarityTree similarityTree = new SimilarityTree();
                typedAstNodes.forEach(similarityTree::add);

                // Для каждой ноды достаем template
                // Если template подходит под условия, добавляем его к множеству templateSet
                typedAstNodes.forEach(astNode -> {
                    Template template = similarityTree.getTemplate(astNode, matchesMinimum);
                    if (templatePredicate.test(template) &&
                            !fileTypeNotShowPredicate.test(template.getBody())) {
                        template.setFileType(fileType);
                        templateSet.add(template);
                    }
                });

                elementTypeNumber[0]++;
                System.out.println((fileTypeNumber[0] + elementTypeNumber[0] / astNodesMap.size()) / psiFilesMap.size());
            });

            fileTypeNumber[0]++;
        });

        // Сортируем templateList по количеству повторений
        List<Template> templateList = new ArrayList<>(templateSet);
        templateList.sort((o1, o2) -> Integer.compare(o2.getOccurrences(), o1.getOccurrences()));

        // Удаляем из списка все template, которые являются подпоследовательностями других template
        Set<Integer> toDeleteSet = new TreeSet<>(Comparator.reverseOrder());
        for (int is = 0; is < templateList.size(); ++is) {
            for (int iss = 0; iss < templateList.size(); ++iss) {
                if (is != iss && ListOps.hasSubSequence(templateList.get(is).getTokens(), templateList.get(iss).getTokens())) {
                    toDeleteSet.add(iss);
                }
            }
        }
        toDeleteSet.forEach(idx -> templateList.remove(idx.intValue()));

        // Возвращаем первые templatesToShow template
        return templateList.stream().limit(templatesToShow).collect(Collectors.toList());
    }
}
