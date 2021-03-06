package de.kontext_e.jqassistant.plugin.asciidoc.scanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import de.kontext_e.jqassistant.plugin.asciidoc.store.descriptor.AsciidocFileDescriptor;

import static java.util.Arrays.asList;

public class AsciidocFileScannerPlugin extends AbstractScannerPlugin<FileResource, AsciidocFileDescriptor> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsciidocFileScannerPlugin.class);
    private static final String JQASSISTANT_PLUGIN_ASCIIDOC_SUFFIXES = "jqassistant.plugin.asciidoc.suffixes";

    private static List<String> suffixes = asList("asciidoc", "adoc");

    @Override
    public boolean accepts(final FileResource item, final String path, final Scope scope) throws IOException {
        int beginIndex = path.lastIndexOf(".");
        if(beginIndex > 0) {
            final String suffix = path.substring(beginIndex + 1).toLowerCase();

            boolean accepted = suffixes.contains(suffix);
            if(accepted) {
                LOGGER.info("Asciidoc accepted path "+path);
            }

            return accepted;
        }

        return false;
    }

    @Override
    public AsciidocFileDescriptor scan(final FileResource item, final String path, final Scope scope, final Scanner scanner) throws IOException {
        final Store store = scanner.getContext().getStore();
        final AsciidocFileDescriptor asciidocFileDescriptor = store.create(AsciidocFileDescriptor.class);
        asciidocFileDescriptor.setFileName(path);

        new AsciidocImporter(item.getFile(), store, 20).importDocument(asciidocFileDescriptor);

        return asciidocFileDescriptor;
    }

    @Override
    protected void configure() {
        super.configure();

        if(getProperties().containsKey(JQASSISTANT_PLUGIN_ASCIIDOC_SUFFIXES)) {
            suffixes = new ArrayList<>();
            String serializedSuffixes = (String) getProperties().get(JQASSISTANT_PLUGIN_ASCIIDOC_SUFFIXES);
            for (String suffix : serializedSuffixes.split(",")) {
                suffixes.add(suffix.toLowerCase().trim());
            }
            LOGGER.info("Asciidoc accepts suffixes "+suffixes);
        }
    }

}
