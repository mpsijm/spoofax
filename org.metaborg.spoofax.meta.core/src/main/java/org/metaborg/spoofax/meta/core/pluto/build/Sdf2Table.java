package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.meta.core.pluto.build.misc.PrepareNativeBundle;
import org.metaborg.spoofax.meta.core.pluto.build.misc.PrepareNativeBundle.Output;
import org.metaborg.util.cmd.Arguments;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;
import build.pluto.output.OutputTransient;

public class Sdf2Table extends SpoofaxBuilder<Sdf2Table.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final String sdfModule;
        public final Arguments sdfArgs;


        public Input(SpoofaxContext context, String sdfModule, Arguments sdfArgs) {
            super(context);
            this.sdfModule = sdfModule;
            this.sdfArgs = sdfArgs;
        }
    }


    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2Table> factory = SpoofaxBuilderFactoryFactory
        .of(Sdf2Table.class, Input.class);


    public Sdf2Table(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputPersisted<File>, Sdf2Table, SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2Table>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Compile grammar to parse table";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("sdf2table." + input.sdfModule + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        requireBuild(MakePermissive.factory, new MakePermissive.Input(context, input.sdfModule, input.sdfArgs));
        final OutputTransient<Output> commandsOutput = requireBuild(PrepareNativeBundle.factory, input);
        final Output commands = commandsOutput.val();

        final File inputPath = toFile(context.settings.getSdfCompiledPermissiveDefFile(input.sdfModule));
        final File outputPath = toFile(context.settings.getSdfCompiledTableFile(input.sdfModule));

        require(inputPath);

        final ExecutionResult result =
            commands.sdf2table.run("-t", "-i", inputPath.getAbsolutePath(), "-m", input.sdfModule, "-o",
                outputPath.getAbsolutePath());

        provide(outputPath);

        setState(State.finished(result.success));
        return OutputPersisted.of(outputPath);
    }
}
