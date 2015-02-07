package edu.ucdavis.myshimmerapp.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;

public class MyShimmerDataList {

	private final static int Serie_ACCL_X = 0;
	private final static int Serie_ACCL_Y = 1;
	private final static int Serie_ACCL_Z = 2;
	private final static int Serie_GYRO_X = 3;
	private final static int Serie_GYRO_Y = 4;
	private final static int Serie_GYRO_Z = 5;
	private final static int Serie_TimeStamp = 6;

	private final static int Serie_Max = 7;

	List<Double> timestamp = new ArrayList<Double>();
	List<Double> acclX = new ArrayList<Double>();
	List<Double> acclY = new ArrayList<Double>();
	List<Double> acclZ = new ArrayList<Double>();
	List<Double> gyroX = new ArrayList<Double>();
	List<Double> gyroY = new ArrayList<Double>();
	List<Double> gyroZ = new ArrayList<Double>();

	public double[] getSingle(int i) {
		double[] ret = { timestamp.get(i), acclX.get(i), acclY.get(i),
				acclZ.get(i), gyroX.get(i), gyroY.get(i), gyroZ.get(i) };
		return ret;
	}

	public String getSingleStr(int i) {
		String ret = Math.round(timestamp.get(i)) + ","
				+ String.format("%.2f", acclX.get(i)).toString() + ","
				+ String.format("%.2f", acclY.get(i)).toString() + ","
				+ String.format("%.2f", acclZ.get(i)).toString() + ","
				+ String.format("%.2f", gyroX.get(i)).toString() + ","
				+ String.format("%.2f", gyroY.get(i)).toString() + ","
				+ String.format("%.2f", gyroZ.get(i)).toString();
		return ret;
	}

	public List<Double> getSerie(int i) {
		List<Double> ret = null;
		switch (i) {
		case Serie_ACCL_X:
			ret = acclX;
			break;
		case Serie_ACCL_Y:
			ret = acclY;
			break;
		case Serie_ACCL_Z:
			ret = acclZ;
			break;
		case Serie_GYRO_X:
			ret = gyroX;
			break;
		case Serie_GYRO_Y:
			ret = gyroY;
			break;
		case Serie_GYRO_Z:
			ret = gyroZ;
			break;
		case Serie_TimeStamp:
			ret = timestamp;
			break;
		}
		return ret;
	}

	public void add(double[] input) {
		if (input != null && input.length == 6) {
			timestamp
					.add((double) Math.round((System.currentTimeMillis() % 100000)));
			acclX.add(input[Serie_ACCL_X]);
			acclY.add(input[Serie_ACCL_Y]);
			acclZ.add(input[Serie_ACCL_Z]);
			gyroX.add(input[Serie_GYRO_X]);
			gyroY.add(input[Serie_GYRO_Y]);
			gyroZ.add(input[Serie_GYRO_Z]);
		}
	}

	public void addAll(MyShimmerDataList list) {
		if (list != null && !list.isEmpty()) {
			timestamp.addAll(list.getSerie(Serie_TimeStamp));
			acclX.addAll(list.getSerie(Serie_ACCL_X));
			acclY.addAll(list.getSerie(Serie_ACCL_Y));
			acclZ.addAll(list.getSerie(Serie_ACCL_Z));
			gyroX.addAll(list.getSerie(Serie_GYRO_X));
			gyroY.addAll(list.getSerie(Serie_GYRO_Y));
			gyroZ.addAll(list.getSerie(Serie_GYRO_Z));
		}
	}

	public void clear() {
		timestamp.clear();
		acclX.clear();
		acclY.clear();
		acclZ.clear();
		gyroX.clear();
		gyroY.clear();
		gyroZ.clear();
	}

	public int size() {
		return timestamp.size();
	}

	public boolean isEmpty() {
		return timestamp.isEmpty();
	}

	private void addSerie(int i, List<Double> input) {
		if (input != null && !input.isEmpty()) {
			switch (i) {
			case Serie_ACCL_X:
				acclX.addAll(input);
				break;
			case Serie_ACCL_Y:
				acclY.addAll(input);
				break;
			case Serie_ACCL_Z:
				acclZ.addAll(input);
				break;
			case Serie_GYRO_X:
				gyroX.addAll(input);
				break;
			case Serie_GYRO_Y:
				gyroY.addAll(input);
				break;
			case Serie_GYRO_Z:
				gyroZ.addAll(input);
				break;
			case Serie_TimeStamp:
				timestamp.addAll(input);
				break;
			}
		}
	}

	public static MyShimmerDataList subList(MyShimmerDataList input,
			int startPos, int endPos) {
		MyShimmerDataList ret = new MyShimmerDataList();

		if (input != null && !input.isEmpty()) {
			for (int i = 0; i < Serie_Max; i++) {
				ret.addSerie(i, input.getSerie(i).subList(startPos, endPos));
			}
		}
		return ret;
	}

	public static double[] parseShimmerObject(ObjectCluster input) {

		String[] sensorName = new String[6];
		sensorName[0] = "Accelerometer X";
		sensorName[1] = "Accelerometer Y";
		sensorName[2] = "Accelerometer Z";
		sensorName[3] = "Gyroscope X";
		sensorName[4] = "Gyroscope Y";
		sensorName[5] = "Gyroscope Z";

		double[] data = new double[6];

		for (int i = 0; i < sensorName.length; i++) {
			Collection<FormatCluster> ofFormats = input.mPropertyCluster
					.get(sensorName[i]);
			FormatCluster formatCluster = ((FormatCluster) ObjectCluster
					.returnFormatCluster(ofFormats, "CAL"));
			if (formatCluster != null) {
				data[i] = formatCluster.mData;
			}
		}
		return data;
	}
}
