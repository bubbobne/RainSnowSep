/*
 * GNU GPL v3 License
 *
 * Copyright 2015 Marialaura Bancheri
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package rainSnowSperataion;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.LinkedHashMap;

import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.Unit;

import org.geotools.coverage.grid.GridCoverage2D;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.libs.modules.HMModel;
import org.hortonmachine.gears.utils.RegionMap;
import org.hortonmachine.gears.utils.coverage.CoverageUtilities;

import com.vividsolutions.jts.geom.Coordinate;

@Description("The component separates the precipitation into rainfalla nd snowfall,"
		+ "accordinf to Kavetski et al. (2006)")
@Author(name = "Marialaura Bancheri and Giuseppe Formetta", contact = "maryban@hotmail.it")
@Keywords("Hydrology, Rain-snow separation")
@Label(HMConstants.HYDROGEOMORPHOLOGY)
@Name("Rain-snow separation raster case")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class RainSnowSeparationRasterCase extends HMModel {

	@Description("The map of the interpolated temperature.")
	@In
	public GridCoverage2D inTemperatureGrid;

	@Description("The double value of the  temperature, once read from the HashMap")
	double temperature;

	@Description("The map of the the interpolated precipitation.")
	@In
	public GridCoverage2D inPrecipitationGrid;

	@Description("The double value of the precipitation, once read from the HashMap")
	double precipitation;

	@Description("Alfa_r is the adjustment parameter for the precipitation measurements errors")
	@In
	public double alfa_r;

	@Description("Alfa_s is the adjustment parameter for the snow measurements errors")
	@In
	public double alfa_s;

	@Description("m1 is the smoothing parameter, for the detecting ot the rainfall in " + "the total precipitation")
	@In
	public double m1 = 1.0;

	@Description("The melting temperature")
	@In
	@Unit("C")
	public double meltingTemperature;

	@Description("the linked HashMap with the coordinate of the stations")
	LinkedHashMap<Integer, Coordinate> stationCoordinates;

	@Description("The digital elevation model.")
	@In
	public GridCoverage2D inDem;

	@Description("The output rainfall map")
	@Out
	public GridCoverage2D outRainfallGrid = null;

	@Description(" The output snowfall map")
	@Out
	public GridCoverage2D outSnowfallGrid = null;

	@Execute
	public void process() throws Exception {

		// transform the GrifCoverage2D maps into writable rasters
		WritableRaster temperatureMap = mapsReader(inTemperatureGrid);
		WritableRaster precipitationMap = mapsReader(inPrecipitationGrid);

		// get the dimension of the maps
		RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(inDem);
		int cols = regionMap.getCols();
		int rows = regionMap.getRows();

		// create the output maps with the right dimensions
		WritableRaster outRainfallWritableRaster = CoverageUtilities.createWritableRaster(cols, rows, null, null, null);
		WritableRaster outSnowfallWritableRaster = CoverageUtilities.createWritableRaster(cols, rows, null, null, null);

		WritableRandomIter RainIter = RandomIterFactory.createWritable(outRainfallWritableRaster, null);
		WritableRandomIter SnowIter = RandomIterFactory.createWritable(outSnowfallWritableRaster, null);

		// iterate over the entire domain and compute for each pixel the SWE
		for (int r = 1; r < rows - 1; r++) {
			for (int c = 1; c < cols - 1; c++) {

				// get the exact value of the variable in the pixel i, j
				precipitation = precipitationMap.getSampleDouble(c, r, 0);
				temperature = temperatureMap.getSampleDouble(c, r, 0);

				// compute the rainfall and the snowfall according to Kavetski et al. (2006)
				double rainfall = ((precipitation / Math.PI) * Math.atan((temperature - meltingTemperature) / m1)
						+ precipitation / 2);
				rainfall = (rainfall < 0) ? 0 : rainfall;
				double snowfall = alfa_s * (precipitation - rainfall);
				snowfall = (snowfall < 0) ? 0 : snowfall;
				rainfall = alfa_r * rainfall;

				RainIter.setSample(c, r, 0, rainfall);
				SnowIter.setSample(c, r, 0, snowfall);

			}
		}

		CoverageUtilities.setNovalueBorder(outRainfallWritableRaster);
		CoverageUtilities.setNovalueBorder(outSnowfallWritableRaster);
		outRainfallGrid = CoverageUtilities.buildCoverage("Rain", outRainfallWritableRaster, regionMap,
				inDem.getCoordinateReferenceSystem());
		outSnowfallGrid = CoverageUtilities.buildCoverage("Snow", outSnowfallWritableRaster, regionMap,
				inDem.getCoordinateReferenceSystem());

	}

	/**
	 * Maps reader transform the GrifCoverage2D in to the writable raster and
	 * replace the -9999.0 value with no value.
	 *
	 * @param inValues: the input map values
	 * @return the writable raster of the given map
	 */
	private WritableRaster mapsReader(GridCoverage2D inValues) {
		RenderedImage inValuesRenderedImage = inValues.getRenderedImage();
		WritableRaster inValuesWR = CoverageUtilities.replaceNovalue(inValuesRenderedImage, -9999.0);
		inValuesRenderedImage = null;
		return inValuesWR;
	}

}
