package edu.jetbrains.plugin.lt.extensions;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.FileType;
import edu.jetbrains.plugin.lt.extensions.ep.FileTypeTemplateFilter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class JavaFileTypeTemplateFilter implements FileTypeTemplateFilter {
    @NotNull
    @Override
    public FileType fileType() {
        return JavaFileType.INSTANCE;
    }

    @NotNull
    @Override
    public Collection<String> keywordsNotAnalyze() {
        return Arrays.asList("package", "import", "@Test");
    }

    @NotNull
    @Override
    public Collection<String> keywordsNotShow() {
        return Arrays.asList("@Override", "/**");
    }
}
