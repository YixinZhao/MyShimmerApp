package edu.ucdavis.myshimmerapp.ml;

import java.util.List;

import edu.ucdavis.myshimmerapp.activities.MyShimmerDataList;
import android.util.Log;
import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignFisher;

public class Features {

	private final static String TAG = "MyShimmerApp.FeatureCalc";

	private final static IirFilterCoefficients lowPass = getCoef(
			FilterPassType.lowpass, 0.1, 0);
	private IirFilterCoefficients bandPass = getCoef(FilterPassType.bandpass,
			0.01, 0.9);
	private IirFilterCoefficients bandEnergy = getCoef(FilterPassType.bandpass,
			0.03, 0.35);
	private IirFilterCoefficients lowEnergy = getCoef(FilterPassType.lowpass,
			0.7, 0);
	private IirFilterCoefficients modvigEnergy = getCoef(
			FilterPassType.bandpass, 0.71, 0.9);

	public Features(List<MyShimmerDataList> input) {

	}

	
	
	private static IirFilterCoefficients getCoef(FilterPassType type,
			double fc1, double fc2) {
		int filterOrder = 2;
		IirFilterCoefficients coef = IirFilterDesignFisher.design(
				FilterPassType.lowpass, FilterCharacteristicsType.butterworth,
				filterOrder, 0, fc1, fc2);

		for (double d : coef.a) {
			Log.d(TAG, "coef.a:" + d);
		}
		for (double d : coef.b) {
			Log.d(TAG, "coef.b:" + d);
		}
		return coef;
	}
}
