jqassistant-plugins
===================
This repository contains plugins for buschmais' [jQAssistant](http://jqassistant.org).

How to install
==============
* add [jQAssistant](http://jqassistant.org) to dependencies in your project

* declare additional dependencies in your project:
 
    - groupId de.kontext-e.jqassistant.plugin
    - artifactId jqassistant.plugin.checkstyle or .findbugs or .jacoco or .git or .pmd
    - e.g. for gradle:   runtime("de.kontext-e.jqassistant.plugin:jqassistant.plugin.checkstyle:1.0.0")


* you may want to configure some properties in jqassistant.properties or as system property (java -Dpropertyname=propertyvalue):

    - jqassistant.plugin.checkstyle.basepackage=de.kontext_e.jqassistant.plugin
    - jqassistant.plugin.findbugs.filename=findbugs.xml
    - jqassistant.plugin.jacoco.filename=jacoco.xml
    - jqassistant.plugin.pmd.filename=pmd.xml
    - jqassistant.plugin.git.path=/usr/bin/git
    - jqassistant.plugin.git.range= - a valid git range if not the complete history should be imported -

* execute jQAssistant tasks

Plugin doc
==========
* [git](https://github.com/kontext-e/jqassistant-plugins/blob/master/git/src/main/asciidoc/git.adoc)
* [plantuml](https://github.com/kontext-e/jqassistant-plugins/blob/master/plantuml/src/main/asciidoc/plantuml.adoc)

Work in progress
================
* PlantUML class diagram packages; please note: although the plantuml plugin will become version 1.1.0, it is
  far from a complete PlantUML import; in fact, only the packages of class diagrams were imported for now
  because this is the only thing that is currently needed for architecture rules

Coming later
============
* Jenkins

## Continuous Build

[![Build Status](https://travis-ci.org/kontext-e/jqassistant-plugins.svg?branch=master)](https://travis-ci.org/kontext-e/jqassistant-plugins)
