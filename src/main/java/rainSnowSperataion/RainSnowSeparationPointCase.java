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
import java.util.Set;
import java.util.Map.Entry;

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

import org.geotools.feature.SchemaException;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.libs.modules.HMModel;



@Description("The component separates the precipitation into rainfalla nd snowfall,"
		+ "according to Kavetski et al. (2006)")
@Author(name = "Marialaura Bancheri and Giuseppe Formetta", contact = "maryban@hotmail.it")
@Keywords("Hydrology, Rain-snow separation point case")
@Label(HMConstants.HYDROGEOMORPHOLOGY)
@Name("Rain-snow separation point case")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class RainSnowSeparationPointCase extends HMModel {

	@Description("The Hashmap with the time series of the precipitation values")
	@In
	public HashMap<Integer, double[]> inPrecipitationValues;

	@Description("The double value of the precipitation, once read from the HashMap")
	double precipitation;

	@Description("Alfa_r is the adjustment parameter for the rainfall measurements errors")
	@In
	public double alfa_r;

	@Description("Alfa_s is the adjustment parameter for the snow measurements errors")
	@In
	public double alfa_s;

	@Description("m1 is the parameter controling the degree of smoothing")
	@In
	public double m1 = 1.0;

	@Description("The Hashmap with the time series of the temperature values")
	@In
	public HashMap<Integer, double[]> inTemperatureValues;

	@Description("The double value of the  temperature, once read from the HashMap")
	double temperature;

	@Description("The melting temperature")
	@In
	@Out
	@Unit("C")
	public double meltingTemperature;

	
	@Description(" The output rainfall HashMap")
	@Out
	public HashMap<Integer, double[]> outRainfallHM= new HashMap<Integer, double[]>();;

	@Description(" The output snowfall HashMap")
	@Out
	public HashMap<Integer, double[]> outSnowfallHM= new HashMap<Integer, double[]>();;



	@Execute
	public void process() throws Exception { 
		checkNull(inPrecipitationValues);


		// reading the ID of all the stations 
		Set<Entry<Integer, double[]>> entrySet = inPrecipitationValues.entrySet();

		for (Entry<Integer, double[]> entry : entrySet) {
			
			Integer ID = entry.getKey();

			// read the input data for the given station
			temperature=inTemperatureValues.get(ID)[0];
			precipitation=inPrecipitationValues.get(ID)[0];


			// compute the rainfall and the snowfall according to Kavetski et al. (2006)
			double rainfall=alfa_r*((precipitation/ Math.PI)* Math.atan((temperature - meltingTemperature) / m1)+precipitation/2);
			double snowfall=alfa_s*(precipitation-rainfall);
			snowfall=(snowfall<0)?0:snowfall;
			
			storeResult_series((Integer)ID,rainfall,  snowfall);

		}
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
