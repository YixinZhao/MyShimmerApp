package edu.ucdavis.myshimmerapp.activities;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.ObjectCluster;

import edu.ucdavis.myshimmerapp.services.MyShimmerService;

public class RecogContinousActivity extends RecogActivity {
	private final static String TAG = "MyShimmerApp.RecogContinousActivity";

	private static MyShimmerDataList mWindowData = new MyShimmerDataList();
	private static MyShimmerDataList mWindowDataBak = new MyShimmerDataList();

	protected static Handler mActivityHandler = new Handler() {
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
				if ((msg.obj instanceof ObjectCluster)) {
					ObjectCluster datas = (ObjectCluster) msg.obj;

					double[] datatmp = MyShimmerDataList
							.parseShimmerObject(datas);

					mWindowData.add(datatmp);

					// test begin
					{
						log(mWindowData.getSingleStr(mWindowData.size() - 1));
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

					if (++mWindowCounter >= mWindowSize) {
						boolean isDetected = isWindowPositiveForSignal(mWindowData);
						Log.d(TAG, "isDetected:" + isDetected);

						if (mIsRecording == false) {
							if (isDetected) {
								Log.d(TAG, "******** Start Recording ********");
								mRecordData.clear();
								mEndingWindowCounter = 0;

								mIsRecording = true;

								/*
								 * begin with one extra previous window.
								 */
								mRecordData.addAll(mWindowDataBak);
								mRecordData.addAll(mWindowData);
								mEndingPoint = mRecordData.size();
							} else {
								mWindowDataBak.clear();
								mWindowDataBak.addAll(mWindowData);
							}
						} else {
							mRecordData.addAll(mWindowData);
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

			case MyShimmerService.Message_TimerCallBack:

				if (msg.obj instanceof String) {
					String timerName = (String) msg.obj;
					Log.d(TAG, "Message_TimerCallBack:" + timerName);

				}
				break;
			}
		}
	};

	public void onDestroy() {
		super.onDestroy();

		mWindowData.clear();
		mRecordData.clear();
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
		mWindowCounter = 0;
		mEndingWindowCounter = 0;
		mIsRecording = false;

		if (mService != null)
			mService.registerGraphHandler(mActivityHandler);
	}

}
