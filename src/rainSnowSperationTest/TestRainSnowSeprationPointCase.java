/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
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

import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.jgrasstools.gears.io.shapefile.OmsShapefileFeatureReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;

import org.jgrasstools.hortonmachine.utils.HMTestCase;


import rainSnowSperataion.RainSnowSeparationPointCase;


/**
 * Test the {@link RainSnowSeprationPointCase} module.
 * 
 * @author Marialaura Bancheri
 */
public class TestRainSnowSeprationPointCase extends HMTestCase {


	public TestRainSnowSeprationPointCase() throws Exception {


		String startDate = "2007-10-17 00:00" ;
		String endDate = "2007-10-18 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		String inPathToPrecipitation ="resources/Input/Precip.csv";
		String inPathToAirT ="resources/Input/Temperature.csv";
		String pathToRainfall= "resources/Output/rainfall.csv";
		String pathToSnowfall=  "resources/Output/snowfall.csv";

		OmsTimeSeriesIteratorReader precipitationReader = getTimeseriesReader(inPathToPrecipitation, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader airTReader = getTimeseriesReader(inPathToAirT, fId, startDate, endDate, timeStepMinutes);


		OmsShapefileFeatureReader stationsReader = new OmsShapefileFeatureReader();
		stationsReader.file = "resources/Input/stations.shp";
		stationsReader.readFeatureCollection();
		SimpleFeatureCollection stationsFC = stationsReader.geodata;



		OmsTimeSeriesIteratorWriter writerRainfall = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerSnowfall = new OmsTimeSeriesIteratorWriter();


		writerRainfall.file = pathToRainfall;
		writerRainfall.tStart = startDate;
		writerRainfall.tTimestep = timeStepMinutes;
		writerRainfall.fileNovalue="-9999";

		writerSnowfall.file = pathToSnowfall;
		writerSnowfall.tStart = startDate;
		writerSnowfall.tTimestep = timeStepMinutes;
		writerSnowfall.fileNovalue="-9999";


		RainSnowSeparationPointCase separetor = new RainSnowSeparationPointCase();
		separetor.inStations = stationsFC;
		separetor.fStationsid = "Field2";

		while( airTReader.doProcess) { 


			separetor.alfa_r=1.12963980507173877;
			separetor.alfa_s= 1.07229882570334652;
			separetor.meltingTemperature=-0.64798915634369553;


			airTReader.nextRecord();	
			HashMap<Integer, double[]> id2ValueMap = airTReader.outData;
			separetor.inTemperatureValues= id2ValueMap;

			precipitationReader.nextRecord();
			id2ValueMap = precipitationReader.outData;
			separetor.inPrecipitationValues = id2ValueMap;


			separetor.pm = pm;

			separetor.process();


			HashMap<Integer, double[]> outHM = separetor.outRainfallHM;
			HashMap<Integer, double[]> outHMQ = separetor.outSnowfallHM;

			writerRainfall.inData = outHM;
			writerRainfall.writeNextLine();



			if (pathToRainfall != null) {
				writerRainfall.close();
			}

			writerSnowfall.inData = outHMQ;
			writerSnowfall.writeNextLine();

			if (pathToSnowfall != null) {
				writerSnowfall.close();
			}


		}

		airTReader.close();
		precipitationReader.close();


	}

	private OmsTimeSeriesIteratorReader getTimeseriesReader( String inPath, String id, String startDate, String endDate,
			int timeStepMinutes ) throws URISyntaxException {
		OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
		reader.file = inPath;
		reader.idfield = "ID";
		reader.tStart = startDate;
		reader.tTimestep = timeStepMinutes;
		reader.tEnd = endDate;
		reader.fileNovalue = "-9999";
		reader.initProcess();
		return reader;
	}

}
