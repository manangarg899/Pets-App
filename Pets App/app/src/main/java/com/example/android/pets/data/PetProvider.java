package com.example.android.pets.data;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import static android.R.attr.id;
import static com.example.android.pets.data.PetsContract.PetEntry;

/**
 * Created by Manan on 04-03-2017.
 */

public class PetProvider extends ContentProvider {

    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    private PetDbHelper mDbHelper;

    private static final int PETS = 100;

    private static final int PETS_ID = 101;

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/pets";

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PATH_PETS,PETS);
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PATH_PETS+"/#",PETS_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

            Cursor cursor = null;

            int match = sUriMatcher.match(uri);

            switch (match) {
                case PETS:
                    cursor = database.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);

                    break;
                case PETS_ID:
                    selection = PetEntry._ID + "=?";
                    selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                    cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                            null, null, sortOrder);
                    break;
                default:
                    throw new IllegalArgumentException("Cannot query unknown URI " + uri);
            }

            cursor.setNotificationUri(getContext().getContentResolver(),uri);

            return cursor;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri,values);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        int rowsDeleted;

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rowsDeleted;
            case PETS_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri,values,selection,selectionArgs);

            case PETS_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                getContext().getContentResolver().notifyChange(uri,null);

                return updatePet(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }


        private Uri insertPet(Uri uri, ContentValues values) {

            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }

            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);

            if(gender == null || !PetEntry.isValidGender(gender))
            {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if(weight != null && weight<0)
            {
                throw new IllegalArgumentException("Pet requires valid weight");
            }

            SQLiteDatabase database = mDbHelper.getWritableDatabase();
            long newRowId = database.insert(PetEntry.TABLE_NAME,null,values);

            if (newRowId == -1) {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
                return null;
            }

            getContext().getContentResolver().notifyChange(uri,null);


            return ContentUris.withAppendedId(uri, id);
        }


        private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs)
        {
            if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
                String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
                if (name == null) {
                    throw new IllegalArgumentException("Pet requires a name");
                }
            }

            if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
                Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
                if (gender == null || !PetEntry.isValidGender(gender)) {
                    throw new IllegalArgumentException("Pet requires valid gender");
                }
            }

            if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
                // Check that the weight is greater than or equal to 0 kg
                Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
                if (weight != null && weight < 0) {
                    throw new IllegalArgumentException("Pet requires valid weight");
                }
            }

            if (values.size() == 0) {
                return 0;
            }

            SQLiteDatabase database = mDbHelper.getWritableDatabase();

            int rowsUpdated = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);

            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return rowsUpdated;
        }

}
