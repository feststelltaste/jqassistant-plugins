package de.kontext_e.jqassistant.plugin.plantuml.scanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.store.api.Store;
import de.kontext_e.jqassistant.plugin.plantuml.store.descriptor.PlantUmlFileDescriptor;
import de.kontext_e.jqassistant.plugin.plantuml.store.descriptor.PlantUmlPackageDescriptor;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.classdiagram.AbstractEntityDiagram;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.cucadiagram.Code;
import net.sourceforge.plantuml.cucadiagram.IGroup;
import net.sourceforge.plantuml.cucadiagram.ILeaf;
import net.sourceforge.plantuml.cucadiagram.Link;
import net.sourceforge.plantuml.cucadiagram.entity.EntityFactory;

class PumlLineParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PumlLineParser.class);

    private final Store store;
    private final Map<String, PlantUmlPackageDescriptor> mappingFromFqnToPackage = new HashMap<>();
    private final PlantUmlFileDescriptor plantUmlFileDescriptor;
    private ParsingState parsingState = ParsingState.ACCEPTING;
    private StringBuilder lineBuffer = new StringBuilder();

    public PumlLineParser(final Store store, final PlantUmlFileDescriptor plantUmlFileDescriptor, final ParsingState parsingState) {
        this.store = store;
        this.plantUmlFileDescriptor = plantUmlFileDescriptor;
        this.parsingState = parsingState;
    }

    protected void parseLine(final String line) {
        if(line == null) return;

        String normalizedLine = line.trim().toLowerCase();

        if(parsingState == ParsingState.IGNORING && normalizedLine.startsWith("[\"plantuml\"")) {
            parsingState = ParsingState.PLANTUMLFOUND;
        }

        if(parsingState == ParsingState.ACCEPTING && (normalizedLine.startsWith("----") || normalizedLine.startsWith("@enduml"))) {
            parsingState = ParsingState.IGNORING;
            lineBuffer.append("\n");
            lineBuffer.append("@enduml");

            SourceStringReader reader = new SourceStringReader(lineBuffer.toString());
            List<BlockUml> blocks = reader.getBlocks();
            if(blocks.isEmpty()) return; // something is wrong with the syntax?

            final Diagram diagram = blocks.get(0).getDiagram();
            if(!(diagram instanceof AbstractEntityDiagram)) {
                return;
            }

            AbstractEntityDiagram descriptionDiagram = (AbstractEntityDiagram) diagram;
            EntityFactory entityFactory = descriptionDiagram.getEntityFactory();
            final Map<Code, IGroup> groups = entityFactory.getGroups();
            for (Map.Entry<Code, IGroup> codeIGroupEntry : groups.entrySet()) {
                IGroup iGroup = codeIGroupEntry.getValue();
                PlantUmlPackageDescriptor packageNode = store.create(PlantUmlPackageDescriptor.class);
                packageNode.setFullQualifiedName(iGroup.getCode().getFullName());
                plantUmlFileDescriptor.getPlantUmlElements().add(packageNode);
                mappingFromFqnToPackage.put(iGroup.getCode().getFullName(), packageNode);
                if(iGroup.getParentContainer().getCode() != null) {
                    PlantUmlPackageDescriptor parent = mappingFromFqnToPackage.get(iGroup.getParentContainer().getCode().getFullName());
                    if(parent != null) {
                        parent.getContainedPackages().add(packageNode);
                    }
                }
            }

            for (Map.Entry<Code, ILeaf> leafEntry : entityFactory.getLeafs().entrySet()) {
                final ILeaf iLeaf = leafEntry.getValue();
                final String fullName = iLeaf.getCode().getFullName();
                PlantUmlPackageDescriptor packageNode = store.create(PlantUmlPackageDescriptor.class);
                packageNode.setFullQualifiedName(fullName);
                plantUmlFileDescriptor.getPlantUmlElements().add(packageNode);
                mappingFromFqnToPackage.put(fullName, packageNode);
            }


            for (Link link : entityFactory.getLinks()) {
                String lhs = link.getEntity1().getCode().getFullName();
                String rhs = link.getEntity2().getCode().getFullName();
                if("ARROW".equalsIgnoreCase(link.getType().getDecor2().name())) {
                    String swap = rhs;
                    rhs = lhs;
                    lhs = swap;
                }

                final PlantUmlPackageDescriptor lhsDescriptor = mappingFromFqnToPackage.get(lhs);
                if(lhsDescriptor == null) {
                    LOGGER.warn("No jQAssistant node created for left side of link from "+lhs+" to "+rhs);
                    continue;
                }

                final PlantUmlPackageDescriptor rhsDescriptor = mappingFromFqnToPackage.get(rhs);
                if(rhsDescriptor == null) {
                    LOGGER.warn("No jQAssistant node created for right side of link from "+lhs+" to "+rhs);
                    continue;
                }

                lhsDescriptor.getMayDependOnPackages().add(rhsDescriptor);
            }

        }
        if(parsingState == ParsingState.PLANTUMLFOUND && normalizedLine.startsWith("----")) {
            parsingState = ParsingState.ACCEPTING;
            lineBuffer = new StringBuilder();
            lineBuffer.append("@startuml");
            lineBuffer.append("\n");
            return;
        }

        if(parsingState == ParsingState.ACCEPTING) {
            lineBuffer.append(normalizedLine);
            lineBuffer.append("\n");
        }
    }
}
