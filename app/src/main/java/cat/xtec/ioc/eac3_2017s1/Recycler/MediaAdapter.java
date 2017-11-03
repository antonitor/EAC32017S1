package cat.xtec.ioc.eac3_2017s1.Recycler;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cat.xtec.ioc.eac3_2017s1.Data.MediaContract.MediaTable;
import cat.xtec.ioc.eac3_2017s1.R;


/**
 * Created by Toni on 26/10/2017.
 */

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private Cursor mCursor;
    private Context mContext;
    private final MediaAdapterOnClickHandler mClickHandler;

    public MediaAdapter(Context context, Cursor cursor, MediaAdapterOnClickHandler clickHandler) {
        this.mContext = context;
        this.mCursor = cursor;
        this.mClickHandler = clickHandler;
    }

    public interface MediaAdapterOnClickHandler {
        void onClick(String mediaName, String mediaPath, long latitude, long longitude);
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.media_item, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MediaViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }
        String mediaName = mCursor.getString(mCursor.getColumnIndex(MediaTable.COLUMN_FILE_NAME));
        int isVideo = mCursor.getInt(mCursor.getColumnIndex(MediaTable.COLUMN_IS_VIDEO));
        long id = mCursor.getLong(mCursor.getColumnIndex(MediaTable._ID));
        holder.nameTextView.setText(mediaName);
        if (isVideo == 0) {
            holder.mediaTypeTextView.setImageResource(R.drawable.image_icon);
        } else {
            holder.mediaTypeTextView.setImageResource(R.drawable.video_icon);
        }
        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) {
            this.notifyDataSetChanged();
        }
    }

    class MediaViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {

        TextView nameTextView;
        ImageView mediaTypeTextView;

        public MediaViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.media_name);
            mediaTypeTextView = (ImageView) itemView.findViewById(R.id.media_type_image);
        }

        @Override
        public void onClick(View view) {
            if (!mCursor.moveToPosition(getAdapterPosition())) {
                return;
            }
            String mediaName = mCursor.getString(mCursor.getColumnIndex(MediaTable.COLUMN_FILE_NAME));
            String mediaPath = mCursor.getString(mCursor.getColumnIndex(MediaTable.COLUMN_PATH));
            long latitude = mCursor.getLong(mCursor.getColumnIndex(MediaTable.COLUMN_LATITUDE));
            long longitude = mCursor.getLong(mCursor.getColumnIndex(MediaTable.COLUMN_LONGITUDE));
            mClickHandler.onClick(mediaName, mediaPath, latitude, longitude);
        }
    }
}
