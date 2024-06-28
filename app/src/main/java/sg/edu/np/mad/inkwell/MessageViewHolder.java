package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    RelativeLayout relativeLayout;

    TextView message;

    public MessageViewHolder(View view) {
        super(view);

        relativeLayout = view.findViewById(R.id.relativeLayout);

        message = view.findViewById(R.id.message);
    }
}
