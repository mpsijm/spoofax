package org.metaborg.spoofax.core.stratego;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.IFacet;
import org.metaborg.spoofax.core.analysis.StrategoAnalysisMode;

/**
 * Represents the Stratego runtime facet of a language.
 */
public class StrategoFacet implements IFacet {
    private final Iterable<FileObject> ctreeFiles;
    private final Iterable<FileObject> jarFiles;
    private final @Nullable String analysisStrategy;
    private final @Nullable StrategoAnalysisMode analysisMode;
    private final @Nullable String onSaveStrategy;
    private final @Nullable String resolverStrategy;
    private final @Nullable String hoverStrategy;
    private final @Nullable String completionStrategy;


    /**
     * Creates a Stratego facet from Stratego provider files, and strategy names.
     * 
     * @param ctreeFile
     *            CTree provider files.
     * @param jarFiles
     *            JAR provider files.
     * @param analysisStrategy
     *            Name of the analysis strategy, or null if none.
     * @param analysisMode
     *            Analysis mode
     * @param onSaveStrategy
     *            Name of the on-save strategy, or null if none.
     * @param resolverStrategy
     *            Name of the reference resolution strategy, or null if none.
     * @param hoverStrategy
     *            Name of the hover strategy, or null if none.
     * @param completionStrategy
     *            Name of the semantic completions strategy, or null if none.
     */
    public StrategoFacet(Iterable<FileObject> ctreeFile, Iterable<FileObject> jarFiles,
        @Nullable String analysisStrategy, StrategoAnalysisMode analysisMode, @Nullable String onSaveStrategy,
        @Nullable String resolverStrategy, @Nullable String hoverStrategy, @Nullable String completionStrategy) {
        this.ctreeFiles = ctreeFile;
        this.jarFiles = jarFiles;
        this.analysisStrategy = analysisStrategy;
        this.analysisMode = analysisMode;
        this.onSaveStrategy = onSaveStrategy;
        this.resolverStrategy = resolverStrategy;
        this.hoverStrategy = hoverStrategy;
        this.completionStrategy = completionStrategy;
    }


    /**
     * @return Iterable over the ctree provider files.
     */
    public Iterable<FileObject> ctreeFiles() {
        return ctreeFiles;
    }

    /**
     * @return Iterable over the JAR provider files.
     */
    public Iterable<FileObject> jarFiles() {
        return jarFiles;
    }

    /**
     * @return Name of the analysis strategy, or null if none.
     */
    public @Nullable String analysisStrategy() {
        return analysisStrategy;
    }

    /**
     * @return Analysis mode, or null if none.
     */
    public @Nullable StrategoAnalysisMode analysisMode() {
        return analysisMode;
    }

    /**
     * @return Name of the on-save strategy, or null if none.
     */
    public @Nullable String onSaveStrategy() {
        return onSaveStrategy;
    }

    /**
     * @return Name of the reference resolver strategy, or null if none.
     */
    public @Nullable String resolverStrategy() {
        return resolverStrategy;
    }

    /**
     * @return Name of the hover strategy, or null if none.
     */
    public @Nullable String hoverStrategy() {
        return hoverStrategy;
    }

    /**
     * @return Name of the semantic completions strategy, or null if none.
     */
    public @Nullable String completionStrategy() {
        return completionStrategy;
    }
}