/*
 * This file is part of Horton Machine (http://www.hortomachine.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
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
package rainSnowSperationTest;



import org.geotools.coverage.grid.GridCoverage2D;

import org.hortonmachine.gears.io.rasterreader.OmsRasterReader;
import org.hortonmachine.gears.io.rasterwriter.OmsRasterWriter;

import org.junit.Test;


import rainSnowSperataion.RainSnowSeparationRasterCase;

/**
 * Test the separetor module.
 * 
 * @author Marialaura Bancheri
 */
public class TestRainSnowSeprationRasterCase{

	GridCoverage2D outRainfallDataGrid = null;
	GridCoverage2D outSnowfallDataGrid = null;
	
	@Test
	public void Test() throws Exception {


		OmsRasterReader demReader = new OmsRasterReader();
		demReader.file = "resources/Input/pit.asc";
		demReader.process();
		GridCoverage2D dem = demReader.outRaster;
		
		OmsRasterReader precipitationGridReader = new OmsRasterReader();
		precipitationGridReader.file = "resources/Input/pit.asc";
		precipitationGridReader.process();
		GridCoverage2D precipitationGrid = precipitationGridReader.outRaster;
		
		OmsRasterReader temperatureGridReader = new OmsRasterReader();
		temperatureGridReader.file = "resources/Input/pit.asc";
		temperatureGridReader.process();
		GridCoverage2D temperatureGrid = temperatureGridReader.outRaster;
		
		


		RainSnowSeparationRasterCase separetor = new RainSnowSeparationRasterCase();
		separetor.inPrecipitationGrid=precipitationGrid;
		separetor.inTemperatureGrid=temperatureGrid;
		separetor.inDem = dem;


	
		separetor.alfa_r=1.12963980507173877;
		separetor.alfa_s= 1.07229882570334652;
		separetor.meltingTemperature=-0.64798915634369553;


		separetor.process();



		outRainfallDataGrid =separetor.outRainfallGrid;
		outSnowfallDataGrid = separetor.outSnowfallGrid;

		OmsRasterWriter writerRainfallRaster = new OmsRasterWriter();
		writerRainfallRaster.inRaster = outRainfallDataGrid;
		writerRainfallRaster.file = "resources/Output/mapRainfall.asc";
		writerRainfallRaster.process();

		OmsRasterWriter writerSnowfallRaster = new OmsRasterWriter();
		writerSnowfallRaster.inRaster = outSnowfallDataGrid;
		writerSnowfallRaster.file = "resources/Output/mapSnowfall.asc";
		writerSnowfallRaster.process();

	}


}
