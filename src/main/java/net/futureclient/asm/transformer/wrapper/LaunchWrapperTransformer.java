package net.futureclient.asm.transformer.wrapper;

import net.futureclient.asm.config.Config;
import net.futureclient.asm.config.ConfigManager;
import net.futureclient.asm.transformer.ClassTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public final class LaunchWrapperTransformer implements IClassTransformer {

    private static final Logger LOGGER = LogManager.getLogger("asmlib");

    public LaunchWrapperTransformer() {}


    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        List<ClassTransformer> classTransformers = this.getTransformers(name, transformedName);

        if (!classTransformers.isEmpty()) {
            ClassNode cn = new ClassNode();
            ClassReader cr = new ClassReader(basicClass);
            cr.accept(cn, 0);

            classTransformers.forEach(transformer -> {
                try {
                    transformer.transform(cn);
                    LOGGER.info("Successfully transformed class {}", transformer.getTargetClassName()); // TODO; success message for each method
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, "Error transforming \"{}\" with transformer \"{}\".", transformedName, transformer.getClass().getName());

                    if (transformer.isRequired())
                        throw new Error(e); // crash game
                    else
                        e.printStackTrace();
                }
            });

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return cw.toByteArray();
        }

        return basicClass;
    }

    public static void log(String fileName, byte[] bytes) {
        File f = new File(fileName);
        try {
            f.createNewFile();
            Files.write(f.toPath(), bytes);
        } catch (Exception e) {

        }
    }

    private List<ClassTransformer> getTransformers(String name, String transformedName) {
        return ConfigManager.INSTANCE
                .getConfigs().stream()
                .sorted(Config::compareTo)
                .map(Config::getClassTransformers)
                .flatMap(List::stream)
                .filter(classTransformer ->
                        classTransformer.getTargetClassName().equals(name) ||
                        classTransformer.getTargetClassName().equals(transformedName)
                )
                .collect(Collectors.toList());
    }

}
