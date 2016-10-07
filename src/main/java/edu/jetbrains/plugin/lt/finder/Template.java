package edu.jetbrains.plugin.lt.finder;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;

import java.util.List;

public class Template {
    private String body;
    private int occurrences;
    private List<String> tokens;
    private FileType fileType;

    public Template(String body, int occurrences, List<String> tokens, FileType fileType) {
        this.body = body;
        this.occurrences = occurrences;
        this.tokens = tokens;
        this.fileType = fileType;
    }

    public Template(String body, int occurrences, List<String> tokens) {
        this(body, occurrences, tokens, FileTypes.UNKNOWN);
    }

    public String getBody() {
        return body;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return tokens.equals(((Template) o).tokens);
    }

    @Override
    public int hashCode() {
        return tokens.hashCode();
    }
}
