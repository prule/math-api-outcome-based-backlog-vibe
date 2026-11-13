package com.paulrule.mathapi;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** STANDARDS.md §2 dependency rules. */
@AnalyzeClasses(packages = "com.paulrule.mathapi", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // shortcut: allowEmptyShould/withOptionalLayers let the rules pass while layers are empty, remove once every
    // layer has a class (item 2)
    @ArchTest
    static final ArchRule layersPointInwards = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .withOptionalLayers(true)
            .layer("Domain")
            .definedBy("..mathapi.domain..")
            .layer("Application")
            .definedBy("..mathapi.application..")
            .layer("Web")
            .definedBy("..mathapi.adapter.in.web..")
            .layer("Config")
            .definedBy("..mathapi.config..")
            .whereLayer("Web")
            .mayOnlyBeAccessedByLayers("Config")
            .whereLayer("Application")
            .mayOnlyBeAccessedByLayers("Web", "Config")
            .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers("Application", "Web", "Config");

    @ArchTest
    static final ArchRule domainIsPlainJava = noClasses()
            .that()
            .resideInAPackage("..mathapi.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta..", "com.fasterxml.jackson..", "tools.jackson..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule applicationHasNoWebOrAdapterTypes = noClasses()
            .that()
            .resideInAPackage("..mathapi.application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework.web..", "org.springframework.http..", "..mathapi.adapter..")
            .allowEmptyShould(true);
}
