package com.whiuk.philip.opensmime;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;

public class PathConverter {
    public static class FileInformation implements Closeable {
        private final File file;
        private final String displayName;
        private final String mimeType;

        public FileInformation(File file, String displayName, String mimeType) {
            this.file = file;
            this.displayName = displayName;
            this.mimeType = mimeType;
        }

        @Override
        protected void finalize() throws Throwable {
            this.file.delete();
        }

        @Override
        public void close() throws IOException {
            this.file.delete();
        }

        public File getFile() {
            return file;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getMimeType() {
            return mimeType;
        }
    }

    public static FileInformation getFileInformation(final Context context, final Uri uri) {
        final String uriScheme = uri.getScheme();
        if("file".equals(uriScheme)) {
            return handleFileScheme(context, uri);
        }

        if("content".equals(uriScheme)) {
            return handleContentScheme(context, uri);
        }

        return null;
    }

    private static FileInformation handleFileScheme(Context context, Uri uri) {
        FileInputStream fileInputStream = null;
        final String filePath = uri.getPath();
        try {
            File srcFile = new File(filePath);
            fileInputStream = new FileInputStream(srcFile);
            File tmpFile = copyToTempFile(context, fileInputStream);
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            String mimeType = fileNameMap.getContentTypeFor(filePath);
            String fileName = FilenameUtils.getName(filePath);

            return new FileInformation(tmpFile, fileName, mimeType);
        } catch (IOException e) {
            Log.e(SMileCrypto.LOG_TAG, "error acquiring FileInforamtion in handleFileScheme", e);
        }

        return null;
    }

    private static FileInformation handleContentScheme(Context context, Uri uri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();

            // all fields for one document
            Cursor cursor = contentResolver.query(uri, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                String mimeType = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                InputStream stream = contentResolver.openInputStream(uri);
                File tmpFile = copyToTempFile(context, stream);
                return new FileInformation(tmpFile, displayName, mimeType);
            }

        } catch (IOException e) {
            Log.e(SMileCrypto.LOG_TAG, "error in PathConverter.handleContentScheme", e);
        }

        return null;
    }

    private static File copyToTempFile(Context context, InputStream inputStream) throws IOException {
        File outputDir = context.getCacheDir(); // context being the Activity pointer
        File outputFile = File.createTempFile("import", ".tmp", outputDir);
        FileUtils.copyInputStreamToFile(inputStream, outputFile);
        return outputFile;
    }

    /*The following methods are from FileUtils.java at https://github.com/iPaulPro/aFileChooser
    *
    * Project is under Apache License 2.0
    *
    * found on http://stackoverflow.com/a/20559175/2319481
    *
    * TODO: remove unused parts
    * */

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


}
