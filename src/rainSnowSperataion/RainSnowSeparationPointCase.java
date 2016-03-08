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


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

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


import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;

import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;


@Description("The component separates the precipitation into rainfalla nd snowfall,"
		+ "accordinf to Kavetski et al. (2006)")
@Author(name = "Marialaura Bancheri and Giuseppe Formetta", contact = "maryban@hotmail.it")
@Keywords("Hydrology, Rain-snow separation point case")
@Label(JGTConstants.HYDROGEOMORPHOLOGY)
@Name("Rain-snow separation point case")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class RainSnowSeparationPointCase extends JGTModel {

	@Description("The Hashmap with the time series of the precipitation values")
	@In
	public HashMap<Integer, double[]> inPrecipitationValues;

	@Description("The double value of the precipitation, once read from the HashMap")
	double precipitation;

	@Description("Alfa_r is the adjustment parameter for the precipitation measurements errors")
	@In
	public double alfa_r;

	@Description("Alfa_s is the adjustment parameter for the snow measurements errors")
	@In
	public double alfa_s;

	@Description("m1 is the smoothing parameter, for the detecting ot the rainfall in "
			+ "the total precipitation")
	@In
	public double m1 = 1.0;

	@Description("The Hashmap with the time series of the temperature values")
	@In
	public HashMap<Integer, double[]> inTemperatureValues;

	@Description("The double value of the  temperature, once read from the HashMap")
	double temperature;

	@Description("The melting temperature")
	@In
	@Unit("C")
	public double meltingTemperature;

	@Description("The shape file with the station measuremnts")
	@In
	public SimpleFeatureCollection inStations;

	@Description("The name of the field containing the ID of the station in the shape file")
	@In
	public String fStationsid;

	@Description(" The vetor containing the id of the station")
	Object []idStations;

	@Description("the linked HashMap with the coordinate of the stations")
	LinkedHashMap<Integer, Coordinate> stationCoordinates;

	
	@Description(" The output rainfall HashMap")
	@Out
	public HashMap<Integer, double[]> outRainfallHM= new HashMap<Integer, double[]>();;

	@Description(" The output snowfall HashMap")
	@Out
	public HashMap<Integer, double[]> outSnowfallHM= new HashMap<Integer, double[]>();;



	@Execute
	public void process() throws Exception { 

		// starting from the shp file containing the stations, get the coordinate
		//of each station
		stationCoordinates = getCoordinate(inStations, fStationsid);

		//create the set of the coordinate of the station, so we can 
		//iterate over the set
		Set<Integer> stationCoordinatesIdSet = stationCoordinates.keySet();


		// trasform the list of idStation into an array
		idStations= stationCoordinatesIdSet.toArray();


		// iterate over the list of the stations
		for (int i=0;i<idStations.length;i++){

			// read the input data for the given station
			temperature=inTemperatureValues.get(idStations[i])[0];
			precipitation=inPrecipitationValues.get(idStations[i])[0];


			// compute the rainfall and the snowfall according to Kavetski et al. (2006)
			double rainfall=alfa_r*((precipitation/ Math.PI)* Math.atan((temperature - meltingTemperature) / m1)+precipitation/2);
			double snowfall=alfa_s*(precipitation-rainfall);
			
			storeResult_series((Integer)idStations[i],rainfall,  snowfall);

		}
	}

	/**
	 * Gets the coordinate given the shp file and the field name in the shape with the coordinate of the station.
	 *
	 * @param collection is the shp file with the stations
	 * @param idField is the name of the field with the id of the stations 
	 * @return the coordinate of each station
	 * @throws Exception the exception in a linked hash map
	 */
	private LinkedHashMap<Integer, Coordinate> getCoordinate(SimpleFeatureCollection collection, String idField)
			throws Exception {
		LinkedHashMap<Integer, Coordinate> id2CoordinatesMap = new LinkedHashMap<Integer, Coordinate>();
		FeatureIterator<SimpleFeature> iterator = collection.features();
		Coordinate coordinate = null;
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				int stationNumber = ((Number) feature.getAttribute(idField)).intValue();
				coordinate = ((Geometry) feature.getDefaultGeometry()).getCentroid().getCoordinate();
				id2CoordinatesMap.put(stationNumber, coordinate);
			}
		} finally {
			iterator.close();
		}

		return id2CoordinatesMap;
	}
	
	/**
	 * Store result_series stores the results in the hashMaps .
	 *
	 * @param ID is the id of the station 
	 * @param rainfall is the output rainfall
	 * @param snowfall is the output snow
	 * @throws SchemaException 
	 */
	
	private void storeResult_series(Integer ID,double rainfall, double snowfall) throws SchemaException {


		outRainfallHM.put(ID, new double[]{rainfall});

		outSnowfallHM.put(ID, new double[]{snowfall});


	}

}
