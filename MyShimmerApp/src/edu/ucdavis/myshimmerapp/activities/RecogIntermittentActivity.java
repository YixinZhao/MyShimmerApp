package edu.ucdavis.myshimmerapp.activities;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.ObjectCluster;

import edu.ucdavis.myshimmerapp.services.MyShimmerService;

public class RecogIntermittentActivity extends RecogActivity {

	private static final String TAG = "MyShimmerApp.RecogIntermittentActivity";

	public final static String Extra_Window_Sizes = "Extra_WindowSizes";

	private static int mWrapWindowCounter = 0;
	private final static int mWrapWindowMax = 4;

	private static List<ObjectCluster> mWindowData = new ArrayList<ObjectCluster>();
	private static List<ObjectCluster> mWrapWindowData = new ArrayList<ObjectCluster>();

	public static Handler mActivityHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MyShimmerService.Message_ShimmerStatusChange:
				Log.d(TAG, "State Change:" + msg.arg1);
				switch (msg.arg1) {
				case Shimmer.MSG_STATE_STOP_STREAMING:
				case Shimmer.STATE_NONE:
					break;
				}
				break;

			case MyShimmerService.Message_ServiceConnected:
				Log.d(TAG, "Message_ServiceConnected");
				break;

			case MyShimmerService.Message_ShimmerRead:
				if (msg.obj instanceof ObjectCluster) {
					ObjectCluster data = (ObjectCluster) msg.obj;

					// test begin
					{
						double[] datatmp = MyShimmerDataList
								.parseShimmerObject(data);
						log(datatmp);
						if (mIsRecording) {
							drawGraph(dynamicPlot_accl_realtime,
									mPlotAcclDataMap, mPlotAcclSeriesMap,
									SENSOR_TYPE_ACCL, datatmp);
							drawGraph(dynamicPlot_gyro_realtime,
									mPlotGyroDataMap, mPlotGyroSeriesMap,
									SENSOR_TYPE_GYRO, datatmp);
						}
					}
					// test end

					mWindowData.add(data);

					if (++mWindowCounter >= mWindowSize) {
						boolean isDetected = false;
						if (mIsRecording == false) {
							mWrapWindowData.addAll(mWindowData);

							/* intermittently inspect while not recording */
							if (++mWrapWindowCounter >= mWrapWindowMax) {

								isDetected = isWindowPositiveForSignal(convertShimmerDataList(mWindowData));
								Log.d(TAG, "isDetected:" + isDetected);

								if (isDetected) {
									mRecordData.clear();

									Log.d(TAG,
											"******** Start Recording ********");
									mIsRecording = true;
									mRecordData = addRecordData(convertShimmerDataList(mWrapWindowData));
									mEndingPoint = mRecordData.size();
								}

								mWrapWindowData.clear();
								mWrapWindowCounter = 0;
							}
						} else {
							/* inspect every window while recording */

							isDetected = isWindowPositiveForSignal(convertShimmerDataList(mWindowData));
							Log.d(TAG, "isDetected:" + isDetected);

							mRecordData
									.addAll(convertShimmerDataList(mWindowData));

							if (!isDetected) {
								mEndingWindowCounter++;
							} else {
								mEndingWindowCounter = 0;// mEndingWindowMax
															// consecutive
															// non-detected
															// windows is
															// considered to
															// be ended.
							}

							if (mEndingWindowCounter == 0)
								mEndingPoint = mRecordData.size();

							if (mEndingWindowCounter >= mEndingWindowMax) {

								mIsRecording = false;

								mEndingWindowCounter = 0;

								mWrapWindowCounter = 0;
								mWrapWindowData.clear();

								Log.d(TAG, "******** End Recording ********");

								/*
								 * end with one extra previous window.
								 */
								if (mRecordData.size() > 8 * mWindowSize
										&& mRecordData.size() < 20 * mWindowSize) {
									MyShimmerDataList toMatchData = MyShimmerDataList
											.subList(mRecordData, 0,
													mEndingPoint + mWindowSize);
									logWindow(toMatchData);
									calcFeatures(toMatchData);
								}

								// TODO: calculate features and matching trained
								// models.
							}
						}

						mWindowData.clear();
						mWindowCounter = 0;
					}
				}
				break;
			}
		}
	};

	public void onDestroy() {
		super.onDestroy();

		mWindowData.clear();
		mRecordData.clear();
		mWrapWindowData.clear();
		mWrapWindowCounter = 0;
		mWindowCounter = 0;
		mEndingWindowCounter = 0;
		mIsRecording = false;

		if (mService != null)
			mService.deRegisterGraphHandler(mActivityHandler);
	}

	public void onPause() {
		super.onPause();

		mWindowData.clear();
		mRecordData.clear();
		mWrapWindowData.clear();
		mWrapWindowCounter = 0;
		mWindowCounter = 0;
		mEndingWindowCounter = 0;
		mIsRecording = false;

		if (mService != null)
			mService.deRegisterGraphHandler(mActivityHandler);
	}

	public void onResume() {
		super.onResume();

		mWindowData.clear();
		mRecordData.clear();
		mWrapWindowData.clear();
		mWrapWindowCounter = 0;
		mWindowCounter = 0;
		mEndingWindowCounter = 0;
		mIsRecording = false;

		if (mService != null)
			mService.registerGraphHandler(mActivityHandler);
	}

	private static MyShimmerDataList convertShimmerDataList(
			List<ObjectCluster> input) {

		MyShimmerDataList ret = new MyShimmerDataList();

		if (input != null && input.size() != 0) {
			for (ObjectCluster obj : input) {
				ret.add(MyShimmerDataList.parseShimmerObject(obj));
			}
		}

		return ret;
	}

	private static MyShimmerDataList addRecordData(MyShimmerDataList tmpAll) {
		MyShimmerDataList ret = new MyShimmerDataList();
		if (tmpAll != null && tmpAll.size() != 0) {

			int dataSize = tmpAll.size();

			Log.d(TAG, "addRecordData:" + dataSize);

			int index = mWrapWindowMax - 1;// default set to the last

			// int lo = 0;
			// int hi = mWrapWindowMax - 2;
			// int mid = (lo + hi) / 2;
			//
			// while (lo <= hi) {
			// Log.d(TAG, "low:" + lo + ";high:" + hi);
			// Log.d(TAG, "low:" + mid * mWindowSize + ";high:" + mWindowSize
			// * (mid + 1));
			// List<MyShimmerData> tmp = tmpAll.subList(mWindowSize * mid,
			// mWindowSize * (mid + 1));
			// if (isWindowPositiveForSignal(tmp)) {
			// hi = mid - 1;
			// index = mid;
			// } else {
			// lo = mid + 1;
			// }
			// mid = (lo + hi) / 2;
			// }

			int lo = 0;
			int hi = mWrapWindowMax - 2;
			while (lo <= hi) {
				Log.d(TAG, "low:" + lo + ";high:" + hi);
				MyShimmerDataList tmp = MyShimmerDataList.subList(tmpAll,
						mWindowSize * lo, mWindowSize * (lo + 1));
				if (isWindowPositiveForSignal(tmp)) {
					index = lo;
				}
				lo++;
			}

			/*
			 * begin with one extra previous window. loses this when 1st window
			 * in wrapping window is positive
			 */
			if (index != 0)
				ret = MyShimmerDataList.subList(tmpAll, (index - 1)
						* mWindowSize, dataSize);
			else
				ret = MyShimmerDataList.subList(tmpAll, 0, dataSize);
			Log.d(TAG, "ret:" + ret.size());
		}
		return ret;
	}
}
