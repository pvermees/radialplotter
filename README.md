# RadialPlotter
A Java program to visualise distributional data on radial plots and kernel density estimates

## Prerequisites

**RadialPlotter**/**DensityPlotter** requires Java 6 or higher (see [http://java.com](http://java.com))

**RadialPlotter** is built with Maven:

```sh
mvn verify
java -jar target/radialplotter-x.x.jar
```

**DensityPlotter** is built and run as follows:

```sh
mvn -Pdensity verify
java -jar target/densityplotter-x.x.jar
```

## Further information

* [http://radialplotter.london-geochron.com](http://radialplotter.london-geochron.com) for **RadialPlotter**

* [http://densityplotter.london-geochron.com](http://densityplotter.london-geochron.com) for **DensityPlotter**

Both programs offer the same functionality albeit with different presets.

## References

* Vermeesch, P., 2009, [RadialPlotter: a Java application for fission track, luminescence and other radial plots](http://www.ucl.ac.uk/~ucfbpve/papers/VermeeschRadMeas2009.pdf), *Radiation Measurements*, 44, 4, 409-410

* Vermeesch, P., 2012. [On the visualisation of detrital age distributions](http://www.ucl.ac.uk/~ucfbpve/papers/VermeeschChemGeol2012.pdf). *Chemical Geology*, v.312-313, 190-194

## Author

[Pieter Vermeesch](http://pieter.london-geochron.com)

## License

This project is licensed under the GPL-3 License
