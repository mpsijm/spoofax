package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.FileState;
import org.strategoxt.imp.runtime.services.OnSaveService;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class ForceOnSave extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, ForceOnSave> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, ForceOnSave>() {
		@Override
		public ForceOnSave makeBuilder(SpoofaxBuildContext context) { return new ForceOnSave(context); }
	};
	
	public ForceOnSave(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Void input) throws IOException {
		Log.log.beginInlineTask("Force on-save handlers for NaBL, TS, etc.", Log.CORE); 
		
		// XXX really need to delete old sdf3 files? Or is it sufficient to remove them from `paths` below?
		List<RelativePath> oldSdf3Paths = FileCommands.listFilesRecursive(context.basePath("src-gen"), new FileExtensionFilter("sdf3"));
		for (Path p : oldSdf3Paths)
			FileCommands.delete(p);
		
		List<RelativePath> paths = FileCommands.listFilesRecursive(
				context.baseDir, 
				new FileExtensionFilter("tmpl", "sdf3", "nab", "ts"));
		for (RelativePath p : paths) {
			result.addSourceArtifact(p);
			forceOnSave(p);
		}
		
//		String pathString = StringCommands.printListSeparated(paths, ";;;");
//		if (!paths.isEmpty())
//			AntForceOnSave.main(new String[]{pathString});
		
		Log.log.endTask();
	}

	private void forceOnSave(RelativePath p) {
		try {
			System.out.println("Calling on-save handler for: " + p);
			FileState fileState = FileState.getFile(new org.eclipse.core.runtime.Path(p.getAbsolutePath()), null);
			if (fileState == null) {
				Log.log.logErr("Could not call on-save handler: File state could not be retrieved for file " + p, Log.CORE);
				return;
			}
			IStrategoTerm ast = fileState.getAnalyzedAst();
			OnSaveService onSave = fileState.getDescriptor().createService(OnSaveService.class, fileState.getParseController());
			onSave.invokeOnSave(ast);
		} catch (Exception e) {
			Log.log.logErr("Could not call on-save handler.", e, Log.CORE);
		}
	}

}