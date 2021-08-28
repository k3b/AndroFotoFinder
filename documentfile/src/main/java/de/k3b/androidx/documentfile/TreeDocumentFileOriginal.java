/*
 * Copyright 2018 The Android Open Source Project (Licensed under the Apache License, Version 2.0)
 * Copyright 2021 by k3b under (Licensed under the GPL v3 (the "License"))
 */

package de.k3b.androidx.documentfile;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;

/**
 * Sourcecode taken from Android api-29/documentfile-1.0.0-sources.jar
 * * where DocumentFile is replaced by DocumentFileEx
 */
@RequiresApi(21)
class TreeDocumentFileOriginal extends DocumentFileEx {
    protected final Context mContext;
    protected Uri mUri;

    TreeDocumentFileOriginal(@Nullable DocumentFileEx parent, @NonNull Context context, Uri uri) {
        super(parent);
        mContext = context;
        mUri = uri;
    }

    @Nullable
    private static Uri createFile(Context context, Uri self, String mimeType,
                                  String displayName) {
        try {
            return DocumentsContract.createDocument(context.getContentResolver(), self, mimeType,
                    displayName);
        } catch (Exception e) {
            return null;
        }
    }

    private static void closeQuietly(@Nullable AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
                // ignored
            }
        }
    }

    @Override
    @Nullable
    public DocumentFileEx createFile(String mimeType, String displayName) {
        final Uri result = TreeDocumentFileOriginal.createFile(mContext, mUri, mimeType, displayName);
        return (result != null) ? new TreeDocumentFileOriginal(this, mContext, result) : null;
    }

    @Override
    @Nullable
    public DocumentFileEx createDirectory(String displayName) {
        final Uri result = createDirectoryUri(displayName);
        return (result != null) ? new TreeDocumentFileOriginal(this, mContext, result) : null;
    }

    protected Uri createDirectoryUri(String displayName) {
        return TreeDocumentFileOriginal.createFile(
                mContext, mUri, DocumentsContract.Document.MIME_TYPE_DIR, displayName);
    }

    @Override
    public Uri getUri() {
        return mUri;
    }

    @Override
    @Nullable
    public String getName() {
        return DocumentsContractApi19.getName(mContext, mUri);
    }

    @Override
    @Nullable
    public String getType() {
        return DocumentsContractApi19.getType(mContext, mUri);
    }

    @Override
    public boolean isDirectory() {
        return DocumentsContractApi19.isDirectory(mContext, mUri);
    }

    @Override
    public boolean isFile() {
        return DocumentsContractApi19.isFile(mContext, mUri);
    }

    @Override
    public boolean isVirtual() {
        return DocumentsContractApi19.isVirtual(mContext, mUri);
    }

    @Override
    public long lastModified() {
        return DocumentsContractApi19.lastModified(mContext, mUri);
    }

    @Override
    public long length() {
        return DocumentsContractApi19.length(mContext, mUri);
    }

    @Override
    public boolean canRead() {
        return DocumentsContractApi19.canRead(mContext, mUri);
    }

    @Override
    public boolean canWrite() {
        return DocumentsContractApi19.canWrite(mContext, mUri);
    }

    @Override
    public boolean delete() {
        try {
            return DocumentsContract.deleteDocument(mContext.getContentResolver(), mUri);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exists() {
        return DocumentsContractApi19.exists(mContext, mUri);
    }

    @Override
    public DocumentFileEx[] listFiles() {
        return listFiles(null, null);
    }

    protected DocumentFileEx[] listFiles(@Nullable String selection,
                                         @Nullable String[] selectionArgs) {
        final ContentResolver resolver = mContext.getContentResolver();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(mUri,
                DocumentsContract.getDocumentId(mUri));
        final ArrayList<Uri> results = new ArrayList<>();

        Cursor c = null;
        try {
            c = resolver.query(childrenUri, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID}, selection, selectionArgs, null);
            while (c.moveToNext()) {
                final String documentId = c.getString(0);
                final Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(mUri,
                        documentId);
                results.add(documentUri);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed query: " + e);
        } finally {
            closeQuietly(c);
        }

        final Uri[] result = results.toArray(new Uri[results.size()]);
        final DocumentFileEx[] resultFiles = new DocumentFileEx[result.length];
        for (int i = 0; i < result.length; i++) {
            resultFiles[i] = new TreeDocumentFileOriginal(this, mContext, result[i]);
        }
        return resultFiles;
    }

    @Override
    public boolean renameTo(String displayName) {
        try {
            final Uri result = DocumentsContract.renameDocument(
                    mContext.getContentResolver(), mUri, displayName);
            if (result != null) {
                mUri = result;
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
