package com.mckuai.imc.Widget.autoupdate;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.mckuai.imc.R;
import com.mckuai.imc.Widget.autoupdate.internal.FoundVersionDialog;
import com.mckuai.imc.Widget.autoupdate.internal.NetworkUtil;
import com.mckuai.imc.Widget.autoupdate.internal.ResponseCallback;
import com.mckuai.imc.Widget.autoupdate.internal.VerifyTask;
import com.mckuai.imc.Widget.autoupdate.internal.VersionDialogListener;
import com.mckuai.imc.Widget.autoupdate.internal.VersionPersistent;

import java.io.File;

public class AppUpdateService {

    private Context context;
    private DownloadManager downloader;
    private DownloadReceiver downloaderReceiver;
    private NetworkStateReceiver networkReceiver;

    private boolean updateDirectly = false;

    private boolean isRegistered = false;

    private boolean isQuiet = false;// 相同版本是否静默提示

    private long downloadTaskId = -12306;
    private static AutoUpgradeDelegate updateDelegate;

    class AutoUpgradeDelegate implements AppUpdate, ResponseCallback, VersionDialogListener {

        private Displayer customShowingDelegate;
        private Version latestVersion;

        @Override
        public void checkLatestVersion(String url, ResponseParser parser) {
            isQuiet = false;
            checkVersion(url, parser, false);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.lurencun.service.autoupdate.AppUpdate#checkLatestVersionQuiet
         * (java.lang.String, com.lurencun.service.autoupdate.ResponseParser)
         */
        @Override
        public void checkLatestVersionQuiet(String url, ResponseParser parser) {
            // TODO Auto-generated method stub
            isQuiet = true;
            checkVersion(url, parser, false);
        }

        @Override
        public void checkAndUpdateDirectly(String url, ResponseParser parser) {
            checkVersion(url, parser, true);
        }

        void checkVersion(String url, ResponseParser parser, boolean isUpdateDirectly) {
            updateDirectly = isUpdateDirectly;
            if (isNetworkActive()) {
                VerifyTask task = new VerifyTask(context, parser, this);
                task.execute(url);
            }
        }

        @Override
        public void downloadAndInstallCurrent() {
            downloadAndInstall(latestVersion);
        }

        @Override
        public void downloadAndInstall(Version latestVersion) {
            if (latestVersion == null || !isNetworkActive())
                return;
            downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Query query = new Query();
            query.setFilterById(downloadTaskId);
            Cursor cur = downloader.query(query);
            // 下载任务已经存在的话
            if (cur.moveToNext())
                return;
            DownloadManager.Request task = new DownloadManager.Request(Uri.parse(latestVersion.targetUrl));
            String apkName = extractName(latestVersion.targetUrl);
            String title = String.format("%s - v%s", apkName, latestVersion.name);
            task.setTitle(title);
            task.setDescription(latestVersion.feature);
            task.setVisibleInDownloadsUi(true);
            task.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            task.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
            downloadTaskId = downloader.enqueue(task);
        }

        boolean isNetworkActive() {
            return NetworkUtil.getNetworkType(context) != NetworkUtil.NOCONNECTION;
        }

        String extractName(String path) {
            String tempFileName = "_temp@" + path.hashCode();
            if (path != null) {
                boolean fileNameExist = path.substring(path.length() - 5, path.length()).contains(".");
                if (fileNameExist) {
                    tempFileName = path.substring(path.lastIndexOf(File.separator) + 1);
                }
            }
            return tempFileName;
        }

        @Override
        public void onFoundLatestVersion(Version version) {
            this.latestVersion = version;

            if (updateDirectly) {
                downloadAndInstall(latestVersion);
                String versionTipFormat = context.getResources().getString(R.string.update_latest_version_title);
                Toast.makeText(context, String.format(versionTipFormat, latestVersion.name), Toast.LENGTH_LONG).show();
                return;
            }

            if (customShowingDelegate != null) {
                customShowingDelegate.showFoundLatestVersion(latestVersion);
            } else {
                FoundVersionDialog dialog = new FoundVersionDialog(context, latestVersion, this);
                dialog.show();
            }
        }

        @Override
        public void onCurrentIsLatest() {
            if (customShowingDelegate != null) {
                customShowingDelegate.showIsLatestVersion();
            } else {
                if (!updateDirectly && !isQuiet) {
                    Toast.makeText(context, R.string.is_latest_version_label, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void callOnResume() {
            if (isRegistered)
                return;

            try {
                callOnPause();
            } catch (Exception e) {
                // TODO: handle exception
            }
            isRegistered = true;
            context.registerReceiver(downloaderReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            context.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        }

        @Override
        public void callOnPause() {
            if (!isRegistered)
                return;
            isRegistered = false;
            context.unregisterReceiver(downloaderReceiver);
            context.unregisterReceiver(networkReceiver);
        }

        @Override
        public void setCustomDisplayer(Displayer delegate) {
            customShowingDelegate = delegate;
        }

        @Override
        public Version getLatestVersion() {
            return latestVersion;
        }

        @Override
        public void doUpdate(boolean laterOnWifi) {
            if (!laterOnWifi) {
                downloadAndInstall(latestVersion);
            } else {
                new VersionPersistent(context).save(latestVersion);
            }
        }

        @Override
        public void doIgnore() {
        }

    }

    class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (downloader == null)
                return;
            long completeId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            if (completeId == downloadTaskId) {
                Query query = new Query();
                query.setFilterById(downloadTaskId);
                Cursor cur = downloader.query(query);
                if (cur.moveToFirst()) {
                    int columnIndex = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == cur.getInt(columnIndex)) {
                        // 下载任务已经完成，清除
                        new VersionPersistent(context).clear();
                        String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        File apkFile = new File(Uri.parse(uriString).getPath());
                        Intent installIntent = new Intent();
                        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        installIntent.setAction(Intent.ACTION_VIEW);
                        installIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                        context.startActivity(installIntent);

                    } else {
                        Toast.makeText(context, R.string.download_failure, Toast.LENGTH_SHORT).show();
                    }
                }
                cur.close();
            }

        }
    }

    class NetworkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                if (NetworkUtil.getNetworkType(context) == NetworkUtil.WIFI) {
                    Version versionTask = new VersionPersistent(context).load();
                    if (versionTask != null) {
                        Toast.makeText(context, R.string.later_update_tip, Toast.LENGTH_SHORT).show();
                        updateDelegate.downloadAndInstall(versionTask);
                    }
                }
            }
        }
    }

    private AppUpdate getAppUpdate() {
        if (updateDelegate == null) {
            updateDelegate = new AutoUpgradeDelegate();
        }
        return updateDelegate;
    }

    public static AppUpdateService updateServiceInstance = null;

    public static AppUpdate getAppUpdate(Context context) {
        if (null == updateServiceInstance) {
            updateServiceInstance = new AppUpdateService(context);
        }
        return updateServiceInstance.getAppUpdate();
    }

    private AppUpdateService(Context context) {
        this.context = context;
        downloaderReceiver = new DownloadReceiver();
        networkReceiver = new NetworkStateReceiver();
    }

}
