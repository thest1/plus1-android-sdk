/**
 * Copyright (c) 2011, Alexander Klestov <a.klestov@co.wapstart.ru>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the "Wapstart" nor the names
 *     of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ru.wapstart.plus1.sdk;

import android.content.Context;
import android.location.LocationManager;
import android.os.Handler;
import android.telephony.TelephonyManager;

/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
public class Plus1BannerAsker implements Plus1BannerViewStateListener {
	private Plus1BannerRequest request						= null;
	private Plus1BannerView view							= null;
	private Handler handler									= null;
	private HtmlBannerDownloader downloaderTask				= null;
	private Runnable askerStopper							= null;
	
	private String deviceId									= null;
	private boolean disableDispatchIMEI						= false;
	private boolean disableAutoDetectLocation				= false;
	private int timeout										= 10;
	private int visibilityTimeout							= 0;
	
	private boolean initialized								= false;
	private boolean mCurrentlyStarted						= false;

	private LocationManager locationManager					= null;
	private Plus1LocationListener locationListener			= null;

	private Plus1BannerViewStateListener viewStateListener	= null;
	private Plus1BannerDownloadListener downloadListener	= null;
	
	public static Plus1BannerAsker create(
		Plus1BannerRequest request, Plus1BannerView view
	) {
		return new Plus1BannerAsker(request, view);
	}

	public Plus1BannerAsker(Plus1BannerRequest request, Plus1BannerView view) {
		this.request = request;
		this.view = view;

		view.setOnAutorefreshChangeListener(
			new Plus1BannerView.OnAutorefreshStateListener() {
				public void onAutorefreshStateChanged(Plus1BannerView view) {
					if (view.getAutorefreshEnabled() && !view.isExpanded())
						start();
					else
						stop();
				}
			}
		);
	}

	public void onPause() {
		stop();

		view.onPause();
	}

	public void onResume() {
		if (!view.isExpanded()) {
			if (view.getAutorefreshEnabled())
				start();
			else
				startOnce();
		}

		view.onResume();
	}

	public boolean isDisabledIMEIDispatch() {
		return disableDispatchIMEI;
	}

	public Plus1BannerAsker disableDispatchIMEI(boolean disable) {
		this.disableDispatchIMEI = disable;

		return this;
	}

	public boolean isDisabledAutoDetectLocation() {
		return disableAutoDetectLocation;
	}
	
	public Plus1BannerAsker disableAutoDetectLocation(boolean disable) {
		this.disableAutoDetectLocation = disable;

		return this;
	}

	public Plus1BannerAsker setTimeout(int timeout) {
		this.timeout = timeout;
		
		return this;
	}

	public Plus1BannerAsker setVisibilityTimeout(int visibilityTimeout) {
		this.visibilityTimeout = visibilityTimeout;

		return this;
	}

	public Plus1BannerAsker setViewStateListener(
		Plus1BannerViewStateListener viewStateListener
	) {
		this.viewStateListener = viewStateListener;

		return this;
	}

	public Plus1BannerAsker setDownloadListener(
		Plus1BannerDownloadListener downloadListener
	) {
		this.downloadListener = downloadListener;

		return this;
	}

	public Plus1BannerAsker init() {
		if (initialized)
			return this;
		
		if (!isDisabledAutoDetectLocation()) {
			this.locationManager = 
				(LocationManager) view.getContext().getSystemService(
					Context.LOCATION_SERVICE
				);
			
			this.locationListener = new Plus1LocationListener(request);
		}
		
		if (!isDisabledIMEIDispatch()) {
			TelephonyManager telephonyManager = 
				(TelephonyManager) view.getContext().getSystemService(
					Context.TELEPHONY_SERVICE
				);
			
			this.deviceId = telephonyManager.getDeviceId();
		}

		downloaderTask = new HtmlBannerDownloader(view);
		
		downloaderTask
			.setDeviceId(deviceId)
			.setRequest(request)
			.setTimeout(timeout);
		
		if (viewStateListener != null)
			view.setViewStateListener(viewStateListener);
		else
			view.setViewStateListener(this);

		if (visibilityTimeout == 0)
			visibilityTimeout = timeout * 3;
		
		handler = new Handler(); 

		initialized = true;
		
		return this;
	}

	// NOTE: for manual refreshing
	public void refreshBanner() {
		if (!view.isExpanded()) {
			stop();

			if (view.getAutorefreshEnabled())
				start();
			else
				startOnce();
		}
	}

	private void start() {
		if (request == null || view == null || mCurrentlyStarted)
			return;

		init();
		
		if (!isDisabledAutoDetectLocation()) {
			locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				timeout * 10000,
				500f,
				locationListener
			);
		}

		mCurrentlyStarted = true;

		downloaderTask = getDownloaderTask();		
		downloaderTask.execute();
	}

	private void stop() {
		if (!isDisabledAutoDetectLocation())
			locationManager.removeUpdates(locationListener);

		downloaderTask.stop();
		
		mCurrentlyStarted = false;
	}

	private void startOnce() {
		if (request == null || view == null || mCurrentlyStarted)
			return;

		init();

		downloaderTask.setRunOnce().execute();
	}

	public void onShowBannerView() {
		if (askerStopper != null)
			handler.removeCallbacks(askerStopper);
	}

	public void onHideBannerView() {
		if (askerStopper != null)
			return;

		askerStopper =
			new Runnable() {
				public void run() {
					stop();
				}
			};

		handler.postDelayed(askerStopper, visibilityTimeout * 1000);
	}

	public void onCloseBannerView() {
		stop();
	}
	
	protected HtmlBannerDownloader getDownloaderTask()
	{
		HtmlBannerDownloader task;
		
		task = new HtmlBannerDownloader(view);

		task
			.setDeviceId(deviceId)
			.setRequest(request)
			.setTimeout(timeout);

		if (downloadListener != null)
			task.setDownloadListener(downloadListener);
		
		return task;
	}
}
