/*
 * Copyright 2018 The Android Open Source Project (Licensed under the Apache License, Version 2.0)
 * Copyright 2021 by k3b under (Licensed under the GPL v3 (the "License"))
 */

package de.k3b.androidx.documentfile;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;

/**
 * This class contains changes made to TreeDocumentFileOriginal
 */
@RequiresApi(21)
class CachedTreeDocumentFile extends TreeDocumentFileOriginal {
    private DocumentFileCache cache;

    CachedTreeDocumentFile(@Nullable DocumentFileEx parent, Context context, Uri uri) {
        super(parent, context, uri);
        if (parent instanceof CachedTreeDocumentFile) {
            cache = ((CachedTreeDocumentFile) parent).cache;
        }
    }

    public TreeDocumentFile find(File file) {
        return cache.find(file);
    }
}
