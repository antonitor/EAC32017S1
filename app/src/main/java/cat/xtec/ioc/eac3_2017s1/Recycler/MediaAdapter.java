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
 *  Classe que proporciona un Adapter al recyclerView que hi ha a MainActivity
 *
 *  Omple el recyclerView amb les dades que extreu de un cursor
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

    /**
     * Interfície que ens ajuda a pasar les dades que ens interesen quan fem clic a un
     * item del recyclerView
     */
    public interface MediaAdapterOnClickHandler {
        void onClick(int isVideo, String mediaPath, float latitude, float longitude);
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
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (!mCursor.moveToPosition(getAdapterPosition())) {
                return;
            }
            int idVideo = mCursor.getInt(mCursor.getColumnIndex(MediaTable.COLUMN_IS_VIDEO));
            String mediaPath = mCursor.getString(mCursor.getColumnIndex(MediaTable.COLUMN_PATH));
            float latitude = mCursor.getFloat(mCursor.getColumnIndex(MediaTable.COLUMN_LATITUDE));
            float longitude = mCursor.getFloat(mCursor.getColumnIndex(MediaTable.COLUMN_LONGITUDE));
            mClickHandler.onClick(idVideo, mediaPath, latitude, longitude);
        }
    }
}
