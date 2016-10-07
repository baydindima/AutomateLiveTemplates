package edu.jetbrains.plugin.lt.extensions.ep;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface FileTypeTemplateFilter {
    ExtensionPointName<FileTypeTemplateFilter> EP_NAME = ExtensionPointName.create("edu.jetbrains.plugin.lt.fileTypeTemplateFilter");

    @NotNull
    FileType fileType();

    @NotNull
    Collection<String> keywordsNotAnalyze();

    @NotNull
    Collection<String> keywordsNotShow();
}
