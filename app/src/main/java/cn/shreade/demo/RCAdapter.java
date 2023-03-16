package cn.shreade.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cn.shreade.sdk.chat.Message;

public class RCAdapter extends RecyclerView.Adapter<RCAdapter.RCViewHolder> {

    private final String TAG = "XXXLog_RC_activity";

    Context context;
    ArrayList<Message> data;

    public RCAdapter(Context context, ArrayList<Message> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public RCViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.rc_item, parent, false);
        return new RCViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RCViewHolder holder, int position) {
        Log.d(TAG, "bind: " + position + ", size: " + data.size());
        //Message msg = data.get(getItemCount() - position - 1);
        Message msg = data.get(position);
        holder.name.setText(msg.getName() + ":  ");
        if (!msg.getContext().equals(null)) {
            holder.txt.setText(msg.getContext().replaceAll("(<[/]*p>)",""));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class RCViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView name;
        private TextView txt;
        public RCViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.chat_list_av);
            this.name = itemView.findViewById(R.id.chat_list_name);
            this.txt = itemView.findViewById(R.id.chat_list_context);
            roundBitmap();
        }

        private void roundBitmap(){
            //如果是圆的时候，我们应该把bitmap图片进行剪切成正方形， 然后再设置圆角半径为正方形边长的一半即可
            Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.av);
            Bitmap bitmap = null;
            Log.d(TAG, "width: " + image.getWidth() + ", height: " + image.getHeight());

            //将长方形图片裁剪成正方形图片
            if (image.getWidth() == image.getHeight()) {
                bitmap = Bitmap.createBitmap(image, image.getWidth() / 2 - image.getHeight() / 2, 0, image.getHeight(), image.getHeight());
            } else if (image.getWidth() > image.getHeight()){
                bitmap = Bitmap.createBitmap(image, image.getWidth() / 2 - image.getHeight() / 2,0, image.getHeight(), image.getHeight());
            }else {
                bitmap = Bitmap.createBitmap(image, 0, image.getHeight() / 2 - image.getWidth() / 2, image.getWidth(), image.getWidth());
            }
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);

            //圆角半径为正方形边长的一半
            roundedBitmapDrawable.setCornerRadius(bitmap.getWidth() / 2);

            //抗锯齿
            roundedBitmapDrawable.setAntiAlias(true);
            imageView.setImageDrawable(roundedBitmapDrawable);
        }
    }




}
