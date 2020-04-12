/*
 * Copyright (c) 2017 by k3b.
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

package de.k3b.android.util;


import android.content.ContentValues;
import android.content.Context;

import java.io.IOException;

import de.k3b.geo.api.IGeoPointInfo;
import de.k3b.media.ExifInterfaceEx;
import de.k3b.media.IPhotoProperties;

/**
 * PhotoPropertiesMediaFilesScanner based on android ExifInterface.
 *
 * Created by k3b on 11.04.2017.
 */

public class PhotoPropertiesMediaFilesScannerExifInterface extends PhotoPropertiesMediaFilesScanner {
    public PhotoPropertiesMediaFilesScannerExifInterface(Context context) {
        super(context);
    }

    @Override
    protected IPhotoProperties loadNonMediaValues(ContentValues destinationValues, String absoluteJpgPath, IPhotoProperties xmpContent) {
        ExifInterfaceEx exif = null;
        try {
            exif = ExifInterfaceEx.create(absoluteJpgPath, null, xmpContent, "PhotoPropertiesMediaFilesScannerExifInterface.loadNonMediaValues");
            if (!exif.isValidJpgExifFormat()) exif = null;
        } catch (IOException ex) {
            // exif is null
        }

        if ((exif != null) && (destinationValues != null)) {
            int degree = exif.getOrientationInDegrees();
            destinationValues.put(DB_ORIENTATION, degree);
        }
        return exif;
    }

    @Override
    public IGeoPointInfo getPositionFromFile(String absoluteJpgPath, String id) {
        ExifInterfaceEx exif = null;
        try {
            exif = ExifInterfaceEx.create(absoluteJpgPath, null, null, "PhotoPropertiesMediaFilesScannerExifInterface.getPositionFromFile");
            if (!exif.isValidJpgExifFormat()) exif = null;
        } catch (IOException ex) {
            // exif is null
        }

        if (exif == null) return null;
        return getPositionFromMeta(absoluteJpgPath, id, exif);
    }
}
