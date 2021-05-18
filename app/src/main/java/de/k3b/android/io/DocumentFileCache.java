/*
 * Copyright (c) 2021 by k3b.
 *
 * This file is part of AndroFotoFinder / #APhotoManager.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package de.k3b.android.io;

import android.util.Log;

import java.io.File;
import java.util.HashMap;

import de.k3b.android.androFotoFinder.Global;
import de.k3b.androidx.Documentfile.DocumentFileEx;
import de.k3b.io.filefacade.FileFacade;
import de.k3b.media.PhotoPropertiesUtil;

public class DocumentFileCache {
    private static final String TAG = DocumentFileTranslator.TAG;

    private CurrentFileCache currentFileCache = null;

    public CurrentFileCache getCacheStrategy() {
        if (currentFileCache == null) {
            currentFileCache = new CurrentFileCache();
        }
        return currentFileCache;
    }

    public DocumentFileEx findFile(String debugContext, DocumentFileEx parentDoc, File parentFile, String displayName) {
        if (Global.android_DocumentFile_find_cache) {
            CurrentFileCache currentFileCache = getCacheStrategy();

            if (currentFileCache != null) {
                if (!parentFile.equals(currentFileCache.lastParentFile)) {
                    HashMap<String, DocumentFileEx> lastChildDocFiles = currentFileCache.lastChildDocFiles;
                    currentFileCache.lastParentFile = parentFile;
                    lastChildDocFiles.clear();
                    int count = 0;
                    DocumentFileEx[] childDocuments = parentDoc.listFiles();
                    for (DocumentFileEx childDoc : childDocuments) {
                        if (childDoc.isFile()) {
                            String childDocName = childDoc.getName().toLowerCase();
                            if (PhotoPropertiesUtil.isImage(childDocName, PhotoPropertiesUtil.IMG_TYPE_ALL | PhotoPropertiesUtil.IMG_TYPE_XMP)) {
                                lastChildDocFiles.put(childDocName, childDoc);
                                count++;
                            }
                        }
                    }
                    if (DocumentFileTranslator.debugLogSAFCache) {
                        Log.i(TAG,
                                ((debugContext == null) ? "" : debugContext)
                                        + this.getClass().getSimpleName()
                                        + ".reload cache from (" + parentFile + ") ==> " + count
                                        + " items");
                    }

                }

                if (PhotoPropertiesUtil.isImage(displayName, PhotoPropertiesUtil.IMG_TYPE_ALL | PhotoPropertiesUtil.IMG_TYPE_XMP)) {
                    return logFindFileResult(debugContext, parentFile, currentFileCache.lastChildDocFiles.get(displayName.toLowerCase()));
                }
            }
        }
        return logFindFileResult(debugContext, parentFile, parentDoc.findFile(displayName));
    }

    private DocumentFileEx logFindFileResult(String debugContext, File parentFile, DocumentFileEx documentFile) {
        if (FileFacade.debugLogSAFFacade || DocumentFileTranslator.debugLogSAFCache) {
            Log.i(TAG,
                    ((debugContext == null) ? "" : debugContext)
                            + this.getClass().getSimpleName()
                            + ".findFile(" + parentFile + ",cache="
                            + Global.android_DocumentFile_find_cache
                            + ") ==> " + documentFile);
        }
        return documentFile;
    }

    public void invalidateParentDirCache() {
        CurrentFileCache currentFileCache = getCacheStrategy();
        if (currentFileCache != null) currentFileCache.lastParentFile = null;
    }

    /**
     * Mapping from local file name inside lastParentFile to photo-related-DocumentFiles
     */
    private static class CurrentFileCache {
        private final HashMap<String, DocumentFileEx> lastChildDocFiles = new HashMap<>();
        private File lastParentFile = null;
    }

}
