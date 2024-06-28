package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class FriendViewHolder extends RecyclerView.ViewHolder {

    ImageView friendProfileImage;

    TextView friendEmail;

    public FriendViewHolder(View view) {
        super(view);

        friendProfileImage = view.findViewById(R.id.friendProfileImage);

        friendEmail = view.findViewById(R.id.friendEmail);
    }
}
