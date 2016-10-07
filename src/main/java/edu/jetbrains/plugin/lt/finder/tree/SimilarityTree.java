package edu.jetbrains.plugin.lt.finder.tree;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import edu.jetbrains.plugin.lt.finder.Template;

import java.util.*;
import java.util.stream.Collectors;

public class SimilarityTree {
    private static final String PLACEHOLDER_STRING = "#_#";

    private Map<String, Integer> leafValuesOccurrences;
    private Map<Pair<IElementType, Integer>, List<SimilarityTree>> childrenTreesMap;

    public SimilarityTree() {
        leafValuesOccurrences = new HashMap<>();
        childrenTreesMap = new HashMap<>();
    }

    public void add(ASTNode node) {
        // Достаем всех детей
        List<ASTNode> children = Arrays.stream(node.getChildren(null))
                .collect(Collectors.toList());
        int childrenSize = children.size();
        if (childrenSize == 0) {
            // Если это лист, нормализуем пробелы и добавляем этот текст в leafValuesOccurrences
            String text = node.getText();
            if (text.matches("\\s+")) text = " ";
            leafValuesOccurrences.put(text, leafValuesOccurrences.getOrDefault(text, 0) + 1);
        } else {
            // Иначе находим SimilarityTree с таким же типом элемента и количеством детей
            // Добавляем ноду в это дерево
            Pair<IElementType, Integer> key = new Pair<>(node.getElementType(), childrenSize);
            List<SimilarityTree> childrenTrees = childrenTreesMap.get(key);
            if (childrenTrees == null) {
                childrenTrees = new ArrayList<>();
                for (int i = 0; i < childrenSize; i++) {
                    childrenTrees.add(new SimilarityTree());
                }
                childrenTreesMap.put(key, childrenTrees);
            }
            for (int i = 0; i < childrenSize; i++) {
                childrenTrees.get(i).add(children.get(i));
            }
        }
    }

    public Template getTemplate(ASTNode node, int matchesMinimum) {
        // Находим количество детей у этой ноды
        List<ASTNode> children = Arrays.stream(node.getChildren(null))
                .collect(Collectors.toList());
        int childrenSize = children.size();
        if (childrenSize == 0) {
            // Если это лист
            // Возвращаем placeholder если количество повторений мало
            // Иначе возвращаем текст листа
            String text = node.getText();
            boolean isWS = false;
            if (text.matches("\\s*")) {
                isWS = true;
                if (text.length() != 0) text = " ";
            }
            int matches = leafValuesOccurrences.get(text);
            return (matches >= matchesMinimum) ?
                    isWS ?
                            new Template(node.getText(), matches, new LinkedList<>())
                            :
                            new Template(text, matches, Arrays.asList(text))
                    :
                    new Template(PLACEHOLDER_STRING, Integer.MAX_VALUE, new LinkedList<>());
        } else {
            // Иначе выбираем подходящии SimilarityTree
            // Вызываем рекурсивно от детей
            // Берем минимум повторяемости
            Pair<IElementType, Integer> key = new Pair<>(node.getElementType(), childrenSize);
            List<SimilarityTree> childrenTrees = childrenTreesMap.get(key);
            StringBuilder builder = new StringBuilder();
            List<String> tokens = new LinkedList<>();
            int occurrences = Integer.MAX_VALUE;
            for (int i = 0; i < childrenSize; i++) {
                Template childTemplate = childrenTrees.get(i).getTemplate(children.get(i), matchesMinimum);
                builder.append(childTemplate.getBody());
                occurrences = Integer.min(occurrences, childTemplate.getOccurrences());
                tokens.addAll(childTemplate.getTokens());
            }
            return new Template(builder.toString().replaceAll("#_#(#_#|\\s)*#_#", PLACEHOLDER_STRING), occurrences, tokens);
        }
    }
}
