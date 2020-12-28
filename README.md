# Memetic Strategy for solving irreversibly ill-conditioned inverse parametric problems

The Memetic Strategy (MS) is a framework for solving irreversibly ill-conditioned inverse parametric problems.
Such problems arise in numerous practical applications, including medical diagnosis, hydrocarbon prospecting
and defectoscopy.
MS attempts to detect areas of insensitivity of the objective function of such problems, giving practitioners in 
the field more insight into the particular instance of a problem.

The strategy consists of several phases: global, local and shape approximation.
The global phase determines separated sets of attraction of the insensitivity regions (i.e. lowlands).
Then, the local phase proceeds to probe the set of attraction, in preparation for the lowland shape approximation.

## Modules

The functionality is placed in module `ms-main`.
Examples of how to use the library are shown in `ms-examples`.

## Usage

Add the following dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>io.github.jswk.ms</groupId>
    <artifactId>ms-core</artifactId>
    <version>some-version</version>
</dependency>
```

## Upgrading dependencies

Run `./mvnw versions:display-dependency-updates -U` for dependencies updates and
`../mvnw versions:display-plugin-updates -U` for plugin updates.

## Releasing

Run `./mvnw clean deploy -Pgpg -Dchangelist=-RELEASE -Drevision=<version>`.
