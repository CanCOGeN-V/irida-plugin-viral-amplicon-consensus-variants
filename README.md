[![Build Status](https://travis-ci.org/Public-Health-Bioinformatics/irida-plugin-viral-amplicon-consensus-variants.svg?branch=master)](https://travis-ci.org/Public-Health-Bioinformatics/irida-plugin-viral-amplicon-consensus-variants)
[![codecov](https://codecov.io/gh/Public-Health-Bioinformatics/irida-plugin-viral-amplicon-consensus-variants/branch/master/graph/badge.svg)](https://codecov.io/gh/Public-Health-Bioinformatics/irida-plugin-viral-amplicon-consensus-variants)
[![Current Release Version](https://img.shields.io/github/release/Public-Health-Bioinformatics/irida-plugin-viral-amplicon-consensus-variants.svg)](https://github.com/Public-Health-Bioinformatics/irida-plugin-viral-amplicon-consensus-variants/releases)

# IRIDA Viral Amplicon Consensus & Variants Pipeline Plugin

![galaxy-workflow-diagram.png][]

This project contains a pipeline implemented as a plugin for the [IRIDA][] bioinformatics analysis system. 
This can be used to construct a consensus sequence for a viral genome that has been sequenced using a tiling amplicon
strategy such as [Primal Scheme](http://primal.zibraproject.org/).

# Table of Contents

   * [IRIDA Viral Amplicon Consensus & Variants Pipeline Plugin](#irida-viral-amplicon-consensus-variants-pipeline-plugin)
   * [Installation](#installation)
      * [Installing Galaxy Dependencies](#installing-galaxy-dependencies)
      * [Installing to IRIDA](#installing-to-irida)
      * [Setting up your primer scheme file(s)](#setting-up-your-primer-scheme-files)
   * [Usage](#usage)
      * [Analysis Results](#analysis-results)
      * [Metadata Table](#metadata-table)
   * [Building](#building)
      * [Installing IRIDA to local Maven repository](#installing-irida-to-local-maven-repository)
      * [Building the plugin](#building-the-plugin)
   * [Dependencies](#dependencies)

# Installation

## Installing Galaxy Dependencies

In order to use this pipeline, you will also have to install the following Galaxy tools and data 
managers within your Galaxy instance. These can be found at:

| Name                               | Version         | Owner                          | Metadata Revision | Galaxy Toolshed Link                                                                                                      |
|------------------------------------|-----------------|------------------------------- |-------------------|---------------------------------------------------------------------------------------------------------------------------|
| trim_galore                        | `0.6.3`         | `bgruening`                    | 15 (2019-07-30)   | [trim_galore-15:084bbd8ba7b8](https://toolshed.g2.bx.psu.edu/view/iuc/trim_galore/084bbd8ba7b8)                           |
| bwa                                | `0.7.17`        | `devteam`                      | 22 (2019-05-08)   | [bwa-22:01ac0a5fedc3](https://toolshed.g2.bx.psu.edu/view/iuc/bwa/01ac0a5fedc3)                                           |
| suite_ivar                         | `0.9.8`         | `iuc`                          | 7 (2019-10-29)    | [suite_ivar:4b5c86ac057f](https://toolshed.g2.bx.psu.edu/view/iuc/suite_ivar/4b5c86ac057f)                                |
| quast                              | `5.0.2`         | `iuc`                          | 5 (2018-12-04)    | [quast-5:81df4950d65b](https://toolshed.g2.bx.psu.edu/view/iuc/quast/81df4950d65b)                                        |

## Installing to IRIDA

Please download the provided `irida-plugin-resistance-screen-[version].jar` from the [releases][] page and copy to your 
`/etc/irida/plugins` directory.  Now you may start IRIDA and you should see the pipeline appear in your list of pipelines.

*Note:* This plugin requires you to be running IRIDA version >= `19.01`. Please see the [IRIDA][] documentation for more details.

## Setting up your primer scheme file(s)

TBD

# Usage

The plugin should now show up in the **Analyses > Pipelines** section of IRIDA.

![plugin-pipeline.png][]
![pipeline-parameters.png][]

## Analysis Results

You should be able to run a pipeline with this plugin and get analysis results. The results include a full `abricate` 
report, and a screened `abricate` report that includes only your genes of interest.

![plugin-results-1.png][]
![plugin-results-2.png][]
![plugin-results-3.png][]

## Metadata Table

And, you should be able to save and view these results in the IRIDA metadata table. The following fields are written to
the IRIDA 'Line List':

| Field Name                                 | Description                                                                                                |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------|
| viral-amplicons-cv/                        |                                                                                                            |
| viral-amplicons-cv/                        |                                                                                                            |


![plugin-metadata.png][]

# Building

Building and packaging this code is accomplished using [Apache Maven][maven]. However, you will first need to install [IRIDA][] to your local Maven repository. The version of IRIDA you install will have to correspond to the version found in the `irida.version.compiletime` property in the [pom.xml][] file of this project. Right now, this is IRIDA version `19.01.3`.

## Installing IRIDA to local Maven repository

To install IRIDA to your local Maven repository please do the following:

1. Clone the IRIDA project

```bash
git clone https://github.com/phac-nml/irida.git
cd irida
```

2. Checkout appropriate version of IRIDA

```bash
git checkout 19.01.3
```

3. Install IRIDA to local repository

```bash
mvn clean install -DskipTests
```

## Building the plugin

Once you've installed IRIDA as a dependency, you can proceed to building this plugin. Please run the following commands:

```bash
cd irida-plugin-viral-amplicon-consensus-variants

mvn clean package
```

Once complete, you should end up with a file `target/irida-plugin-viral-amplicon-consensus-variants-<version>.jar` which can be installed as a plugin to IRIDA.

# Dependencies

The following dependencies are required in order to make use of this plugin.

* [IRIDA][] >= 0.23.0
* [Java][] >= 1.8 and [Maven][maven] (for building)

[maven]: https://maven.apache.org/
[IRIDA]: http://irida.ca/
[Galaxy]: https://galaxyproject.org/
[Java]: https://www.java.com/
[irida-pipeline]: https://irida.corefacility.ca/documentation/developer/tools/pipelines/
[irida-pipeline-galaxy]: https://irida.corefacility.ca/documentation/developer/tools/pipelines/#galaxy-workflow-development
[irida-wf-ga2xml]: https://github.com/phac-nml/irida-wf-ga2xml
[pom.xml]: pom.xml
[workflows-dir]: src/main/resources/workflows
[workflow-structure]: src/main/resources/workflows/0.1.0/irida_workflow_structure.ga
[example-plugin-java]: src/main/java/ca/corefacility/bioinformatics/irida/plugins/ExamplePlugin.java
[irida-plugin-java]: https://github.com/phac-nml/irida/tree/development/src/main/java/ca/corefacility/bioinformatics/irida/plugins/IridaPlugin.java
[irida-updater]: src/main/java/ca/corefacility/bioinformatics/irida/plugins/ExamplePluginUpdater.java
[irida-setup]: https://irida.corefacility.ca/documentation/administrator/index.html
[properties]: https://en.wikipedia.org/wiki/.properties
[messages]: src/main/resources/workflows/0.1.0/messages_en.properties
[maven-min-pom]: https://maven.apache.org/guides/introduction/introduction-to-the-pom.html#Minimal_POM
[pf4j-start]: https://pf4j.org/doc/getting-started.html
[plugin-results-1.png]: doc/images/plugin-results-1.png
[plugin-results-2.png]: doc/images/plugin-results-2.png
[plugin-results-3.png]: doc/images/plugin-results-3.png
[plugin-pipeline.png]: doc/images/plugin-pipeline.png
[plugin-metadata.png]: doc/images/plugin-metadata.png
[pipeline-parameters.png]: doc/images/pipeline-parameters.png
[example-plugin-save-results.png]: doc/images/example-plugin-save-results.png
[galaxy-workflow-diagram.png]: doc/images/galaxy-workflow-diagram.png